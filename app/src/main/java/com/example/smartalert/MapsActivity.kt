package com.example.smartalert

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.smartalert.databinding.ActivityMapsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.TileOverlayOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var userLocation: LatLng
    private lateinit var incidentLocation: LatLng
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var message: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_maps)

        val toolbar: Toolbar = findViewById(R.id.toolbarStat)
        setSupportActionBar(toolbar)
        supportActionBar?.title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Check if the Intent has the extras before retrieving them
        if (intent.hasExtra("userPosition") && intent.hasExtra("incidentLatLng") && intent.hasExtra("message")) {
            userLocation = intent.getParcelableExtra<LatLng>("userPosition")!! // Replace `LatLng` with the appropriate class for the user's location
            incidentLocation = intent.getParcelableExtra<LatLng>("incidentLatLng")!! // Replace `LatLng` with the appropriate class for the incident location
            message = intent.getStringExtra("message").toString()

            // Now you can use `userLocation`, `incidentLocation`, and `message` as needed
            // For example, you can print them to check their values
            Log.d("Debug", "User Location: $userLocation")
            Log.d("Debug", "Incident Location: $incidentLocation")
            Log.d("Debug", "Message: $message")

            // Rest of your code goes here...
        } else {
            // Handle the case where the required extras are missing
            Log.e("Error", "Required extras are missing from the Intent.")
        }
        // Set the context for the SupportMapFragment
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.retainInstance = true // Retain instance to prevent recreation on configuration changes

        mapFragment.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        // Enable the "My Location" blue dot on the map
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        mMap.isMyLocationEnabled = true
        // Set a click listener for the "My Location" button
        mMap.setOnMyLocationClickListener {
            // Request the last known location from the FusedLocationProviderClient
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        // Create a LatLng object from the user's location
                        val userLatLng = LatLng(location.latitude, location.longitude)

                        // Move the camera to the user's location with a desired zoom level
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 17f))
                    }
                }
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    // Create a LatLng object from the user's location
                    val userLatLng = LatLng(location.latitude, location.longitude)

                    // Move the camera to the user's location with a desired zoom level
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 17f))
                }
            }

        mMap.addMarker(MarkerOptions().position(incidentLocation).title(message))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(incidentLocation))
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(incidentLocation, 17f))

    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}