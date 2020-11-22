@file:Suppress("DEPRECATION")

package de.krd.lmapraktikum_datacollector.recorder

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.preference.PreferenceManager
import android.util.Log
import androidx.lifecycle.Observer
import de.krd.lmapraktikum_datacollector.GlobalModel
import de.krd.lmapraktikum_datacollector.R
import de.krd.lmapraktikum_datacollector.permission.PermissionActivity

class LocationRecorder {
    private var run = false;
    private var activity: PermissionActivity
    private lateinit var model: GlobalModel
    private var locationManager: LocationManager
    private var preferences: SharedPreferences

    constructor(activity: PermissionActivity, model: GlobalModel) {
        this.activity = activity;
        this.model = model;
        /*
         * Get the location service
         */
        locationManager = activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        preferences = PreferenceManager.getDefaultSharedPreferences(activity)

        preferences.registerOnSharedPreferenceChangeListener { sharedPreferences, key -> run {
            when (key) {
                activity.getString(R.string.setting_location_enable_gps), activity.getString(R.string.setting_location_enable_network) -> {
                    if (run) {
                        removeLocationRequests()
                        addLocationRequests()
                    }
                }
            }
        } }
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

    fun start() {
        run = true;
        addLocationRequests()
    }

    fun stop() {
        run = false
        removeLocationRequests()
    }

    @SuppressLint("MissingPermission")
    private fun addLocationRequests() {
        val minTimeMs = preferences.getString(activity.getString(R.string.setting_location_update_time), "0")!!.toLong()
        val minDistanceM = preferences.getString(activity.getString(R.string.setting_location_min_distance), "0.0")!!.toFloat()

        if (preferences.getBoolean(activity.getString(R.string.setting_location_enable_gps), false)) {
            activity.withPermission(Manifest.permission.ACCESS_FINE_LOCATION) {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    minTimeMs,
                    minDistanceM,
                    locationListener
                )
            }
        }

        if (preferences.getBoolean(activity.getString(R.string.setting_location_enable_network), false)) {
            activity.withPermission(Manifest.permission.ACCESS_COARSE_LOCATION) {
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    minTimeMs,
                    minDistanceM,
                    locationListener
                )
            }
        }
    }

    private fun removeLocationRequests() {
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