package com.example.smartalert


import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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

class InfoActivity : AppCompatActivity() {

    private lateinit var infoRecycler: RecyclerView
    private lateinit var recyclerViewAdapter: InfoAdapter
    private lateinit var fireDataList: MutableList<InfoData>
    private val database = FirebaseDatabase.getInstance().reference
    private var imageUrl: String = ""
    private var imageUri: Uri? = null
    private lateinit var imageBtn: ImageButton
    private lateinit var imageUploaded: RoundedImageView
    private val PICK_IMAGE_REQUEST = 1
    private lateinit var storageRef: StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info)

        infoRecycler = findViewById(R.id.infoRecycler)
        infoRecycler.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        fireDataList = mutableListOf()
        recyclerViewAdapter = InfoAdapter(fireDataList)
        infoRecycler.adapter = recyclerViewAdapter
        val toolbar: Toolbar = findViewById(R.id.toolbar_info)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""



        val acceptBtn = findViewById<Button>(R.id.acceptBtn)
        val declineBtn = findViewById<Button>(R.id.declineBtn)
        val country = intent.getStringExtra("country")
        val city = intent.getStringExtra("city")
        val key = intent.getStringExtra("key")
        val path = intent.getStringExtra("path")
        val dataRef = database.child("pending").child(path!!).child(country!!).child(city!!).child(key!!)
        val notificationRef = database.child("notification")
        storageRef = FirebaseStorage.getInstance().reference
        getData(recyclerViewAdapter, fireDataList, R.drawable.fire_alert, path!!, country!!, city!!, key!!)


        acceptBtn.setOnClickListener {
            showAlertDialogButtonClickedAccept(this, it, dataRef, path, notificationRef)

        }
        declineBtn.setOnClickListener {
            showAlertDialogButtonClickedDecline(dataRef, notificationRef)
        }

    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                val intent = Intent(this, AdminActivity::class.java)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }
    fun showAlertDialogButtonClickedAccept(context: Context, view: View, dataRef: DatabaseReference, typeOfEvent: String, notificationRef: DatabaseReference) {
        // Inflate the custom alert layout
        val inflater = layoutInflater
        val customLayout = inflater.inflate(R.layout.alert_accept, null)
        val positiveBtn = customLayout.findViewById<Button>(R.id.alert_acceptBtn)
        val negativeBtn = customLayout.findViewById<Button>(R.id.alert_dismissBtn)
        val descriptionTextEn : EditText = customLayout.findViewById(R.id.acceptCommentEng)
        val descriptionTextGr : EditText = customLayout.findViewById(R.id.acceptCommentGr)
        imageBtn = customLayout.findViewById<ImageButton>(R.id.acceptImgBtn)
        imageUploaded = customLayout.findViewById(R.id.acceptImgUploaded)

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

            dataRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {

                    val latitude: Double? = dataSnapshot.child("latitude").value as? Double?
                    val longitude: Double? = dataSnapshot.child("longitude").value as? Double?
                    val notificationKey: String = dataSnapshot.child("key").value as String
                    val data = HashMap<String, Any>()
                    data["GR"] = descriptionTextGr.text.toString()
                    data["EN"] = descriptionTextEn.text.toString()
                    data["timestamp"] = timestampString
                    data["latitude"] = latitude!!.toDouble()
                    data["longitude"] = longitude!!.toDouble()
                    data["timestamp"] = timestampString
                    data["type"] = typeOfEvent

                    val dataRefActive = database.child("active").push()
                    val statisticsRef = database.child("statistics").push()
                    dataRefActive.setValue(data)
                    statisticsRef.setValue(data)
                    if (imageUrl.isNotEmpty()){
                        dataRefActive.child("photos").setValue(imageUrl)
                    }else if(imageUrl.isEmpty()){

                    }
                    notificationRef.child(notificationKey).removeValue().addOnSuccessListener {
                        dataRef.removeValue().addOnSuccessListener {
                        }
                    }

                    Toast.makeText(context, "Successfully created the event", Toast.LENGTH_LONG).show()
                    val intent = Intent(context, AdminActivity::class.java)
                    startActivity(intent)
                    alertDialog.dismiss()
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle any errors that occur
                    Log.e("Database Error", databaseError.message)
                }
            })
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


    fun showAlertDialogButtonClickedDecline(database: DatabaseReference, notificationRef: DatabaseReference) {
        // Inflate the custom alert layout
        val inflater = layoutInflater
        val customLayout = inflater.inflate(R.layout.alert_decline, null)
        val alertReject = customLayout.findViewById<Button>(R.id.alert_rejectBtn)
        val alertCancel = customLayout.findViewById<Button>(R.id.alert_cancelBtn)

        // Create and show the alert dialog
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setView(customLayout)
        alertDialogBuilder.setCancelable(false)
        val alertDialog = alertDialogBuilder.create()

        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertReject.setOnClickListener {
            database.removeValue()
                .addOnSuccessListener {
                    notificationRef.child(fireDataList[0].notificationKey!!).removeValue().addOnSuccessListener {
                        alertDialog.dismiss()
                        Toast.makeText(this, "Successfully discarded the event", Toast.LENGTH_LONG).show()
                        val intent = Intent(this, AdminActivity::class.java)
                        startActivity(intent)
                    }
                }
                .addOnFailureListener { error ->
                    Toast.makeText(this, "Error while trying to discard incident", Toast.LENGTH_LONG).show()
                }

        }


        alertCancel.setOnClickListener {
            alertDialog.dismiss()
        }

        alertDialog.show()

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

private fun getData(recyclerViewAdapter: InfoAdapter, fireDataList: MutableList<InfoData>, imageDrawable: Int, path: String, country: String, city: String, key:String) {
    val database = FirebaseDatabase.getInstance().reference
    fireDataList.clear() // Clear the list before populating
    database.child("pending").child(path).child(country).child(city).child(key).addListenerForSingleValueEvent(object :
        ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {

////                      Parse the values from the fireSnapshot
                        val count = dataSnapshot.child("count").getValue(Long::class.java)
                        val latitude = dataSnapshot.child("latitude").getValue(Double::class.java)
                        val longitude = dataSnapshot.child("longitude").getValue(Double::class.java)
                        val timestamp = dataSnapshot.child("timestamp").getValue(String::class.java)
                        val notificationKey = dataSnapshot.child("key").getValue(String::class.java)
                        val descriptionList: MutableList<String> = mutableListOf()
                        val photosList: MutableList<String> = mutableListOf()
                        for (childSnapshot in dataSnapshot.child("description").children) {
                            val description = childSnapshot.getValue(String::class.java)
                            description?.let {
                                descriptionList.add(it)
                            }
                        }
                        for (childSnapshot in dataSnapshot.child("photos").children) {
                            if(childSnapshot.exists()){
                                val description = childSnapshot.getValue(String::class.java)
                                description?.let {
                                    photosList.add(it)
                                }
                            }else{
                                photosList.add(R.drawable.fire_alert.toString())
                            }

                        }
                        for (i in descriptionList.indices) {
                            val description = descriptionList[i]
                            var photo: String
                            if(i < photosList.count()){
                                photo = photosList[i]
                                val fireData = InfoData(count, description, latitude, longitude, timestamp, imageDrawable, photo, path = path, notificationKey = notificationKey)
                                fireDataList.add(fireData)

                            }else{
                                //val photo = "0"
                                val fireData = InfoData(count, description, latitude, longitude, timestamp, imageDrawable, path = path, notificationKey = notificationKey)
                                fireDataList.add(fireData)
                            }
                        }


            recyclerViewAdapter.notifyDataSetChanged()
                    }

            // Notify the RecyclerView adapter that the data has changed


        override fun onCancelled(error: DatabaseError) {
            Log.e("Firebase Error", error.message)
        }

    })
}