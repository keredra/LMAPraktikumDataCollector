package de.krd.lmapraktikum_datacollector

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.util.Log

class LocationRecorder {
    private lateinit var activity: MainActivity
    private lateinit var model: MainActivityModel
    private lateinit var locationManager: LocationManager

    constructor(activity: MainActivity, model: MainActivityModel) {
        this.activity = activity;
        this.model = model;
        /*
         * Get the location service
         */
        locationManager = activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    private val locationListener = LocationListener { location ->
        val locations = model.data.locations.value;
        /*
         * Check if location is new
         */
        if (locations.size == 0 || !compareLocations(location, locations.last())) {

            /*
             * GPS Debug Message
             */
            Log.d("GPS","lat: ${location.latitude}" + " lng: ${location.longitude}"
                    + " alt: ${location.altitude}" + " acc: ${location.accuracy}")

            model.data.locations.add(location);
        }
    }

    @SuppressLint("MissingPermission")
    fun start() {
        /*
                * Start the location listening and request for updates
                */
        activity.withPermission(Manifest.permission.ACCESS_FINE_LOCATION) {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                0L,
                0f,
                locationListener
            )
        }
    }

    fun stop() {
        locationManager.removeUpdates(locationListener)
    }

    companion object {
        fun compareLocations(l1: Location, l2: Location): Boolean {
            return l1.latitude == l2.latitude
                    && l1.longitude == l2.longitude
                    && l1.accuracy == l2.accuracy
        }
    }
}