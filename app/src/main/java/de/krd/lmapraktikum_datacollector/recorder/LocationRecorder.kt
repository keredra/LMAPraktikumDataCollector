package de.krd.lmapraktikum_datacollector.recorder

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.util.Log
import androidx.lifecycle.Observer
import de.krd.lmapraktikum_datacollector.GlobalModel
import de.krd.lmapraktikum_datacollector.permission.PermissionActivity

class LocationRecorder {
    private lateinit var activity: PermissionActivity
    private lateinit var model: GlobalModel
    private lateinit var locationManager: LocationManager

    constructor(activity: PermissionActivity, model: GlobalModel) {
        this.activity = activity;
        this.model = model;
        /*
         * Get the location service
         */
        locationManager = activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager

//        model.settings.location.observe(activity, Observer {
//            stop()
//            start()
//        })
    }

    private val locationListener = LocationListener { location ->
        val locations = model.data.locations.value;
        /*
         * Check if location is new
         */
        if (locations.isEmpty() || !isLastProviderLocation(location)) {
            /*
             * GPS Debug Message
             */
            Log.d(location.provider,"lat: ${location.latitude}" + " lng: ${location.longitude}"
                    + " alt: ${location.altitude}" + " acc: ${location.accuracy}")

            model.data.locations.add(location);
        }
    }

    @SuppressLint("MissingPermission")
    fun start() {
        /*
                * Start the location listening and request for updates
                */
        val settings = model.settings.location.value!!;

        if (settings.gpsEnabled) {
            activity.withPermission(Manifest.permission.ACCESS_FINE_LOCATION) {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    settings.minTimeMs,
                    settings.minDistanceM,
                    locationListener
                )
            }
        }

        if (settings.networkEnabled) {
            activity.withPermission(Manifest.permission.ACCESS_COARSE_LOCATION) {
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    settings.minTimeMs,
                    settings.minDistanceM,
                    locationListener
                )
            }
        }
    }

    fun stop() {
        locationManager.removeUpdates(locationListener)
    }

    private fun isLastProviderLocation(location: Location) : Boolean {
        val locations = model.data.locations.value;
        var isLastLocation: Boolean = false;
        if (!locations.isEmpty()) {
            val itr = locations.listIterator(locations.size)

            var lastProviderLocationChecked = false;
            while (!isLastLocation && !lastProviderLocationChecked && itr.hasPrevious()) {
                val listLocation = itr.previous()
                isLastLocation = compareLocations(location, listLocation)
                lastProviderLocationChecked = location.provider.equals(listLocation.provider)
            }
        }
        return isLastLocation;
    }
    companion object {
        fun compareLocations(l1: Location, l2: Location): Boolean {
            return l1.provider.equals(l2.provider)
                    && l1.latitude == l2.latitude
                    && l1.longitude == l2.longitude
                    && l1.accuracy == l2.accuracy
        }
    }
}