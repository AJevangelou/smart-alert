package com.example.smartalert

import android.graphics.drawable.Drawable
import com.google.firebase.database.DataSnapshot
import java.net.URL

data class FirebaseData (
        val count: Long? = null,
        val description: List<String>,
        val latitude: Double? = null,
        val longitude: Double? = null,
        val timestamp: String? = null,
        val image: Int,
        val photoUrl: List<String>,
        val country: String? = null,
        val city: String? = null,
        val key: String? = null,
        val path: String? = null
    )



