package com.example.smartalert

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import com.airbnb.lottie.LottieAnimationView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.makeramen.roundedimageview.RoundedImageView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class MainActivity : AppCompatActivity() {

    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var userLocation: Location? = null
    private var countryCityPair: Pair<String?, String?> = Pair(null, null)
    private lateinit var snapKey: String
    private var snapCount: Long? = 0
    private val database = FirebaseDatabase.getInstance().reference
    private lateinit var storageRef: StorageReference
    private val PICK_IMAGE_REQUEST = 1
    private var imageUrl: String = ""
    private var imageUri: Uri? = null
    private lateinit var imageBtn: ImageButton
    private lateinit var statisticsBtn: ImageButton
    private lateinit var imageUploaded: RoundedImageView
    private val userId = auth.currentUser!!.uid
    private val adminRef = database.child("admin").child(userId)
    private val notificationRef = database.child("notification")
    private lateinit var timeoutRunnable : Runnable
    private lateinit var timeoutHandler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        storageRef = FirebaseStorage.getInstance().reference
        val fireBtn = findViewById<ConstraintLayout>(R.id.fireBtn)
        val floodBtn = findViewById<ConstraintLayout>(R.id.floodBtn)
        val earthquakeBtn = findViewById<ConstraintLayout>(R.id.earthquakeBtn)
        val otherBtn = findViewById<ConstraintLayout>(R.id.otherBtn)
        val inboxBtn = findViewById<ImageButton>(R.id.inboxBtn)
        val animationView = findViewById<LottieAnimationView>(R.id.lottie_animation_view_main)
        val roleText = findViewById<TextView>(R.id.roleTextView)

        val floodImage = floodBtn.findViewById<ImageView>(R.id.alert_btn_image)
        floodImage.setImageResource(R.drawable.flood_button)
        val floodText = floodBtn.findViewById<TextView>(R.id.alert_btn_textview)
        floodText.text = resources.getString(R.string.flood_alert)

        val earthquakeImage = earthquakeBtn.findViewById<ImageView>(R.id.alert_btn_image)
        earthquakeImage.setImageResource(R.drawable.earthquake_button)
        val earthquakeText = earthquakeBtn.findViewById<TextView>(R.id.alert_btn_textview)
        earthquakeText.text = resources.getString(R.string.earthquake_alert)

        val otherImage = otherBtn.findViewById<ImageView>(R.id.alert_btn_image)
        otherImage.setImageResource(R.drawable.other_button)
        val otherText = otherBtn.findViewById<TextView>(R.id.alert_btn_textview)
        otherText.text = resources.getString(R.string.other_alert)

        statisticsBtn = findViewById<ImageButton>(R.id.statisticsBtn)
        statisticsBtn.setOnClickListener {
            val intent = Intent(this, StatisticsActivity::class.java)
            startActivity(intent)
        }


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        fireBtn.setOnClickListener {
            animationView.visibility = View.VISIBLE
            getLocation(it, database, "fire", animationView)
        }
        floodBtn.setOnClickListener {
            animationView.visibility = View.VISIBLE
            getLocation(it, database, "flood", animationView)
        }
        earthquakeBtn.setOnClickListener {
            animationView.visibility = View.VISIBLE
            getLocation(it, database, "earthquake", animationView)
        }
        otherBtn.setOnClickListener {
            animationView.visibility = View.VISIBLE
            getLocation(it, database, "other", animationView)
        }
        adminRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {


                dataSnapshot.key?.let { Log.d("Admin key", it) }

                if (dataSnapshot.value != null) {
                   inboxBtn.visibility = View.VISIBLE
                    //ADMIN NOTIFICATION LISTENER
                    startService(Intent(applicationContext, AdminListener::class.java))
                    Log.d("User Listener", "Listening")
                    roleText.text = getString(R.string.civil_protection_worker)
                    startService(Intent(applicationContext, UserListener::class.java))
                }else {
                    startService(Intent(applicationContext, UserListener::class.java))
                    roleText.text = getString(R.string.civilian)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("Firebase Error", "Error fetching admin data: ${databaseError.message}")
            }
        })
        notificationRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val hasChildren = dataSnapshot.hasChildren()
                Log.d("Notification", dataSnapshot.toString())

                if (hasChildren) {
                    Log.d("Notification", "tHERE IS NOTIF")
                    inboxBtn.setImageResource(R.drawable.inbox_filled)

                } else {
                    Log.d("Notification", "NO NOTIF")
                    inboxBtn.setImageResource(R.drawable.inbox_admin_empty)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("Firebase Error", "Error fetching admin data: ${databaseError.message}")
            }
        })
        inboxBtn.setOnClickListener {
            val intent = Intent(this, AdminActivity::class.java)
            startActivity(intent)
        }


    }

    private fun getLocation(view: View, database: DatabaseReference, path: String, animationView: LottieAnimationView) {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request permission for location
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 500 // Update interval in milliseconds
            fastestInterval = 5000 // Fastest update interval in milliseconds
        }

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                fusedLocationClient.removeLocationUpdates(this) // Stop receiving location updates

                if (p0.locations.isNotEmpty()) {
                    val location = p0.locations.first()
                    userLocation = location

                    countryCityPair = getAddress(userLocation!!)

                    loadData(database.child("pending").child(path).child(countryCityPair.first!!).child(countryCityPair.second!!), view, path, animationView)
                } else {
                    // Throw an error or show a message indicating that the location could not be obtained
                    Toast.makeText(this@MainActivity, "Unable to get current location", Toast.LENGTH_SHORT).show()
                }
            }
        }
        // Timer to stop the function if 5 seconds have passed
        timeoutHandler = Handler(Looper.getMainLooper())
        timeoutRunnable = Runnable {
            // Throw an error or show a message indicating that the location could not be obtained within the timeout
            Toast.makeText(this@MainActivity, "Timeout: Unable to get current location", Toast.LENGTH_SHORT).show()
            fusedLocationClient.removeLocationUpdates(locationCallback) // Stop location updates
            return@Runnable
        }
        timeoutHandler.postDelayed(timeoutRunnable, 5000) // 5 seconds timeout

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }




    fun showAlertDialogButtonClicked(view: View?, database: DatabaseReference, snapKey: String, path:String) {
        // Inflate the custom alert layout
        val inflater = layoutInflater
        val customLayout = inflater.inflate(R.layout.custom_alert, null)
        val positiveBtn = customLayout.findViewById<Button>(R.id.positiveBtn)
        val negativeBtn = customLayout.findViewById<Button>(R.id.negativeBtn)
        val descriptionText = customLayout.findViewById<EditText>(R.id.commentsEditText)
        imageBtn = customLayout.findViewById<ImageButton>(R.id.imageBtn)
        imageUploaded = customLayout.findViewById(R.id.imageUploaded)

        // Create and show the alert dialog
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setView(customLayout)
        alertDialogBuilder.setCancelable(false)
        val alertDialog = alertDialogBuilder.create()

        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            positiveBtn.setOnClickListener {

                val timestamp = Date() // Get the current timestamp as a Date object

                // Format the timestamp as a human-readable string
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val timestampString = dateFormat.format(timestamp)

                val data = HashMap<String, Any>()
                val notificationData = HashMap<String, Any>()
                if (userLocation != null && snapKey == "") {
                    data["longitude"] = userLocation!!.longitude
                    data["latitude"] = userLocation!!.latitude
                    data["description"] = descriptionText.text.toString()
                    data["count"] = 1
                    data["timestamp"] = timestampString

                    val dataRef =
                        database.child("pending").child(path).child(countryCityPair.first!!)
                            .child(countryCityPair.second!!).push()
                    dataRef.setValue(data)

                    notificationData["longitude"] = userLocation!!.longitude
                    notificationData["latitude"] = userLocation!!.latitude
                    notificationData["description"] = descriptionText.text.toString()
                    notificationData["count"] = 1
                    notificationData["timestamp"] = timestampString
                    notificationData["country"] = countryCityPair.first!!
                    notificationData["city"] = countryCityPair.second!!
                    notificationData["type"] = path
                    val notificationRef = database.child("notification").push()
                    notificationRef.setValue(notificationData)


                    notificationRef.child("description").setValue(data["description"])

                    dataRef.child("description").push().setValue(data["description"])
                    dataRef.child("key").setValue(notificationRef.key)
                    if (imageUrl.isNotEmpty()) {
                        dataRef.child("photos").push().setValue(imageUrl)

                        notificationRef.child("photos").push().setValue(imageUrl)
                    } else if (imageUrl.isEmpty()) {

                    }
                    sendDialogDataToActivity(getString(R.string.uploaded_successfully))
                    alertDialog.dismiss()
            }else if (userLocation != null && snapKey.isNotEmpty() && descriptionText.text.toString() != ""){
                    snapCount = snapCount?.plus(1) ?: 1

                    database.child("pending").child(path).child(countryCityPair.first!!).child(countryCityPair.second!!)
                        .child(snapKey)
                        .child("description").push().setValue(descriptionText.text.toString())
                    database.child("pending").child(path).child(countryCityPair.first!!).child(countryCityPair.second!!)
                        .child(snapKey)
                        .child("count").setValue(snapCount)
                    if (imageUrl.isNotEmpty()){
                        database.child("pending").child(path).child(countryCityPair.first!!).child(countryCityPair.second!!)
                            .child(snapKey)
                            .child("photos").setValue(imageUrl)
                    }
                    alertDialog.dismiss()
                    sendDialogDataToActivity("Uploaded successfully!")

                }
                else {
                    Toast.makeText(this, "Please try again later", Toast.LENGTH_LONG).show()
                }
            }


        negativeBtn.setOnClickListener {
                alertDialog.dismiss()
        }
        imageBtn.setOnClickListener {
            uploadPhoto(view!!)
        }
        // Set the background of imageBtn
        if (imageUri != null) {
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
            val drawable = BitmapDrawable(resources, bitmap)
            imageBtn.background = drawable
        }

        alertDialog.show()

    }

    private fun loadData(databaseRef: DatabaseReference, view: View, path:String, animationView: LottieAnimationView) {
        var nearestDistance = Double.MAX_VALUE
        var nearestChildSnapshot: DataSnapshot? = null
        timeoutHandler.removeCallbacks(timeoutRunnable)
        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (childSnapshot in dataSnapshot.children) {
                    val childKey: String = childSnapshot.key ?: ""
                    val childValue: Any? = childSnapshot.value

                    val latitude: Double? = childSnapshot.child("latitude").getValue(Double::class.java)
                    val longitude: Double? = childSnapshot.child("longitude").getValue(Double::class.java)
                    val time: String? = childSnapshot.child("timestamp").getValue(String::class.java)

                    val position = LatLng(latitude!!, longitude!!)
                    val distance = calculateDistance(userLocation!!, position)

                    if (distance < 2000 && calculateTimeDifference(time!!) < 7.2e+6) {
                        if (distance < nearestDistance) {
                            nearestDistance = distance.toDouble()
                            nearestChildSnapshot = childSnapshot
                        }
                    }

                    Log.d("Distance", distance.toString())
                    Log.d("Time Difference", calculateTimeDifference(time!!).toString())
                }

                // Invoke showAlertDialogButtonClicked with the nearest child snapshot, if available
                if (nearestChildSnapshot != null) {
                    snapKey = nearestChildSnapshot!!.key ?: ""
                    snapCount = nearestChildSnapshot!!.child("count").getValue(Long::class.java)
                    Log.d("SnapKey", snapKey)

                    animationView.visibility = View.GONE
                    showAlertDialogButtonClicked(view,  database, snapKey, path)
                }else{
                    animationView.visibility = View.GONE
                    showAlertDialogButtonClicked(view,  database, "", path)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle any errors that occur
                Log.e("Database Error", databaseError.message)
            }
        })
    }

    private fun getAddress(location: Location): Pair<String?, String?>  {
        val geocoder = Geocoder(this, Locale.ENGLISH)
        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)

        if (addresses != null) {
            if (addresses.isNotEmpty()) {
                val address = addresses.get(0)
                val country = address?.countryName
                val city = address?.locality

                return Pair(country, city)
            }
        }

        return Pair(null, null)
    }

    // Do something with the data coming from the AlertDialog
    private fun sendDialogDataToActivity(data: String) {
        Toast.makeText(this, data, Toast.LENGTH_SHORT).show()
    }
    // Function to calculate the distance between two positions
    private fun calculateDistance(userLocation: Location, position: LatLng): Float {
        val targetLocation = Location("")
        targetLocation.latitude = position.latitude
        targetLocation.longitude = position.longitude

        return userLocation.distanceTo(targetLocation)
    }
    // Function to calculate the time difference between the current time and a given timestamp
    private fun calculateTimeDifference(timestamp: String): Long {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val currentTime = System.currentTimeMillis()
        val targetTime = dateFormat.parse(timestamp)?.time ?: 0

        return currentTime - targetTime
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
    fun uploadPhoto(view: View) {
        // Create an intent to pick an image from the gallery
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            val imageUri: Uri? = data.data
            if (imageUri != null) {
                // Upload the image to Firebase Storage
                val fileRef = storageRef.child("images/${imageUri.lastPathSegment}")
                fileRef.putFile(imageUri)
                    .addOnSuccessListener { taskSnapshot ->
                        // Image upload successful
                        // Get the download URL of the uploaded image
                        fileRef.downloadUrl.addOnSuccessListener { downloadUri ->
                            // Handle the download URL (e.g., save it in the database)
                            imageUrl = downloadUri.toString()
                            // Save the image URL in your Firebase Realtime Database
                            // Set the background of imageBtn as a circular shape
                            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                            val drawable = BitmapDrawable(resources, bitmap)
                            imageUploaded.background = drawable
                            imageBtn.visibility = View.GONE
                            imageUploaded.visibility = View.VISIBLE

                        }

                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(this, "Failed to upload photo", Toast.LENGTH_LONG).show()
                    }
            }
        }

    }
}