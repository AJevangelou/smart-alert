package com.example.smartalert

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
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
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.android.gms.maps.model.LatLng
import java.util.Locale

class AdminListener : Service() {

    private lateinit var databaseReference: DatabaseReference
    private lateinit var childEventListener: ChildEventListener
    private var isInitialDataRetrieved = false


    override fun onCreate() {
        super.onCreate()
        Log.d("Admin", "Admin Listening")
        val currentLocale = getCurrentLocale()
        val databaseReference = FirebaseDatabase.getInstance().reference.child("notification")
        val childEventListener = object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                if (isInitialDataRetrieved) {
                    Log.d("Child Added", dataSnapshot.toString())
                    var type: String? = dataSnapshot.child("type").getValue(String::class.java)
                    val city: String? = dataSnapshot.child("city").getValue(String::class.java)
                    val country: String? = dataSnapshot.child("country").getValue(String::class.java)
                    val latitude: Double? = dataSnapshot.child("latitude").getValue(Double::class.java)
                    val longitude: Double? = dataSnapshot.child("longitude").getValue(Double::class.java)
                    val time: String? = dataSnapshot.child("timestamp").getValue(String::class.java)
                    val description: String? = dataSnapshot.child("description").getValue(String::class.java)
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
                        val message: String = "Νέα κατάσταση κινδύνου $type στην $city, $country. " +
                                "Σε θέση πλάτους: $latitude και μήκους: $longitude για χρόνο: $time. Με περιγραφή: $description"
                        createNotification(applicationContext, message, "Νέα προειδοποίηση $type")
                    }else{
                        val message: String = "New incident of type: $type in $city, $country. " +
                                "In position latitude: $latitude and longitude: $longitude for time: $time. Described as: $description"
                        createNotification(applicationContext, message, "New $type alert")
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
    }
    fun createNotification(context: Context, message: String, title: String) {
        val channelId = "your_channel_id"
        val notificationId = 1

        // Create an Intent
        val intent = Intent(context, AdminActivity::class.java)
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
}

