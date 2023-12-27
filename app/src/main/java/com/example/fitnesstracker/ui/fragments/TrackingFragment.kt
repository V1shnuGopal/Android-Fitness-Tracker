package com.example.fitnesstracker.ui.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.room.Room
import com.example.fitnesstracker.db.AppDatabase
import com.example.fitnesstracker.db.Exercise
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

open class TrackingFragment: Fragment() {

    protected lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private var totalDistance = 0f

    protected val locations = mutableListOf<Location>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L)
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(1000L)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                locations.addAll(locationResult.locations) // Add all locations returned by the API to our list

                // Calculate and log the total distance after each location update
                var totalDistance = 0f
                for (i in 1 until locations.size) {
                    val start = locations[i - 1]
                    val end = locations[i]
                    totalDistance += start.distanceTo(end)
                }
            }
        }
    }

    fun startTracking() {
        // Check if location permission is granted
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } else {
            // Location Permission not Granted
        }
    }

    fun stopTracking() {
        // Stop location updates
        fusedLocationClient.removeLocationUpdates(locationCallback)

        // Calculate total distance
        for (i in 1 until locations.size) {
            val start = locations[i - 1]
            val end = locations[i]

            totalDistance += start.distanceTo(end)
        }
    }

    fun insertIntoDB(exDate: Long, exType: String, exCount: Long){
        val exercise = Exercise(null, exDate, exType, exCount, totalDistance)
        CoroutineScope(Dispatchers.IO).launch {
            val db = Room.databaseBuilder(
                requireActivity().applicationContext,
                AppDatabase::class.java, AppDatabase.ExerciseHistoryDB
            ).build()
            db.exerciseDao().insert(exercise)
        }
    }
}