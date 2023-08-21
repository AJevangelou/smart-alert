package com.example.smartalert

import android.Manifest
import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.maps.model.LatLng
import java.util.Locale

class UserListener : Service() {

    private lateinit var databaseReference: DatabaseReference
    private lateinit var childEventListener: ChildEventListener
    private var isInitialDataRetrieved = false
    private lateinit var locationManager: LocationManager
    private lateinit var userLocationListener: UserLocationListener


    override fun onCreate() {
        super.onCreate()
        val currentLocale = getCurrentLocale()
        databaseReference = FirebaseDatabase.getInstance().reference.child("active")
        // Create the location manager and listener
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        userLocationListener = UserLocationListener()

        // Request location updates
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                0L,
                0F,
                userLocationListener
            )
        }

        childEventListener = object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                Log.d("User", "started listening")
                if (isInitialDataRetrieved) {
                    Log.d("Snapshot", dataSnapshot.toString())
                    var type: String? = dataSnapshot.child("type").getValue(String::class.java)
                    val latitude: Double? = dataSnapshot.child("latitude").getValue(Double::class.java)
                    val longitude: Double? = dataSnapshot.child("longitude").getValue(Double::class.java)
                    val time: String? = dataSnapshot.child("timestamp").getValue(String::class.java)
                    val description: String?
                    val position = LatLng(latitude!!, longitude!!)
                    val userLocation = getUserLocation()
                    val distance = calculateDistance(userLocation!!, position)
                    Log.d("User", "Distance : $distance")
                        if(distance < 10000) {
                            if (currentLocale.language == "el"){
                                if (type == "fire"){
                                    type = "φωτιάς"
                                }else if (type == "flood"){
                                    type = "πλημμύρας"
                                }else if (type == "earthquake"){
                                    type = "σεισμού"
                                }else{
                                    type = "άλλη"
                                }

                                description = dataSnapshot.child("GR").getValue(String::class.java)
                                val message: String = "Νέα κατάσταση κινδύνου $type" +
                                        " σε θέση πλάτους: $latitude και μήκους: $longitude για χρόνο: $time. Με περιγραφή: $description"

                                Log.d("LATlon", latitude.toString() + longitude.toString() +LatLng(latitude, longitude).toString() )
                                createNotification(applicationContext, message, "Νέα προειδοποίηση $type", LatLng(latitude, longitude), LatLng(userLocation.latitude, userLocation.longitude))

                            }else{
                                description = dataSnapshot.child("EN").getValue(String::class.java)
                                val message: String = "New incident of type: $type" +
                                        " In position latitude: $latitude and longitude: $longitude for time: $time. Described as: $description"
                                Log.d("LATlon", message)

                                createNotification(applicationContext, message, "New $type alert", LatLng(latitude, longitude), LatLng(userLocation.latitude, userLocation.longitude))
                            }
                        }
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                // Handle child changed event
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                // Handle child removed event
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                // Handle child moved event
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
            }
        }

        databaseReference.addChildEventListener(childEventListener)

        // Attach a ValueEventListener to listen for initial data retrieval
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Set the flag to indicate initial data retrieval is complete
                isInitialDataRetrieved = true
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
            }
        }

        databaseReference.addListenerForSingleValueEvent(valueEventListener)

    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        // Remove the value event listener when the service is destroyed
        databaseReference.removeEventListener(childEventListener)
        // Stop receiving location updates
        locationManager.removeUpdates(userLocationListener)
    }
    fun createNotification(context: Context, message: String, title: String, incidentLatLng: LatLng, userPosition: LatLng) {
        val channelId = "your_channel_id"
        val notificationId = 1

        // Create an Intent
        val intent = Intent(context, MapsActivity::class.java)
        intent.putExtra("incidentLatLng", incidentLatLng)
        intent.putExtra("userPosition", userPosition)
        intent.putExtra("message", message)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        // Add any extras to the intent if needed

        // Create a PendingIntent with FLAG_UPDATE_CURRENT
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        // Create a notification channel for Android Oreo and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Channel Name",
                NotificationManager.IMPORTANCE_HIGH
            )
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // Build the notification with BigTextStyle and set the content intent
        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.alert_svgrepo_com)
            .setContentTitle(title)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // Show the notification
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    private fun getCurrentLocale(): Locale {
        val configuration = resources.configuration
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.locales[0]
        } else {
            configuration.locale
        }
    }
    fun getUserLocation(): Location? {
        Log.d("Position", "getting user position")
        return userLocationListener.getUserLocation()
    }
    // Function to calculate the distance between two positions
    private fun calculateDistance(userLocation: Location, position: LatLng): Float {
        val targetLocation = Location("")
        targetLocation.latitude = position.latitude
        targetLocation.longitude = position.longitude

        return userLocation.distanceTo(targetLocation)
    }
}
