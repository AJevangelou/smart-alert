package com.example.smartalert

import android.location.Location
import android.location.LocationListener
import android.os.Bundle

class UserLocationListener : LocationListener {
    private var userLocation: Location? = null

    override fun onLocationChanged(location: Location) {
        // Update the user's location when it changes
        userLocation = location
    }

    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
        // Handle location status changes if needed
    }

    override fun onProviderEnabled(provider: String) {
        // Handle provider enabled event if needed
    }

    override fun onProviderDisabled(provider: String) {
        // Handle provider disabled event if needed
    }

    fun getUserLocation(): Location? {
        return userLocation
    }
}
