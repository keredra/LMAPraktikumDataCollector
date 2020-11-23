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
import de.krd.lmapraktikum_datacollector.data.LocationData
import de.krd.lmapraktikum_datacollector.permission.PermissionActivity
import de.krd.lmapraktikum_datacollector.utils.PreferenceHelper
import java.util.prefs.PreferenceChangeEvent
import java.util.prefs.PreferenceChangeListener

class LocationRecorder : SharedPreferences.OnSharedPreferenceChangeListener {
    private var run = false
    private var gpsEnabled = false
    private var networkEnabled = false
    private var activity: PermissionActivity
    private var minTimeMs = 0L
    private var minDistanceM = 0.0f
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
        loadPreferences()
        preferences.registerOnSharedPreferenceChangeListener(this)
    }

    private val locationListener = LocationListener { location ->
        val locations = model.data.locations.value;
        /*
         * Check if location is new
         */
        val locationData = LocationData.fromLocation(location)
        if (locations.isEmpty() || !isLastProviderLocation(locationData)) {
            /*
             * GPS Debug Message
             */
            Log.d(
                location.provider, "lat: ${location.latitude}" + " lng: ${location.longitude}"
                        + " alt: ${location.altitude}" + " acc: ${location.accuracy}"
            )

            model.data.locations.add(locationData);
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
        if (gpsEnabled) {
            activity.withPermission(Manifest.permission.ACCESS_FINE_LOCATION) {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    minTimeMs,
                    minDistanceM,
                    locationListener
                )
            }
        }

        if (networkEnabled) {
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

    private fun isLastProviderLocation(location: LocationData): Boolean {
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
        fun compareLocations(l1: LocationData, l2: LocationData): Boolean {
            return l1.provider.equals(l2.provider)
                    && l1.latitude == l2.latitude
                    && l1.longitude == l2.longitude
                    && l1.accuracy == l2.accuracy
        }
    }

    private fun loadPreferences() {
        gpsEnabled = PreferenceHelper.getBoolean(
            activity,
            preferences,
            R.string.setting_location_enable_gps
        )
        networkEnabled = PreferenceHelper.getBoolean(
            activity,
            preferences,
            R.string.setting_location_enable_network
        )
        minTimeMs = PreferenceHelper.getLong(
            activity,
            preferences,
            R.string.setting_location_update_time
        )
        minDistanceM = PreferenceHelper.getFloat(
            activity,
            preferences,
            R.string.setting_location_min_distance
        )
    }
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            activity.getString(R.string.setting_location_enable_gps),
            activity.getString(R.string.setting_location_enable_network),
            activity.getString(R.string.setting_location_update_time),
            activity.getString(R.string.setting_location_min_distance) -> {
                loadPreferences()
                if (run) {
                    removeLocationRequests()
                    addLocationRequests()
                }
            }
        }
    }
}