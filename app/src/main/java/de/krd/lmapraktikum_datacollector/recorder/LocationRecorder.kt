@file:Suppress("DEPRECATION")

package de.krd.lmapraktikum_datacollector.recorder

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import android.preference.PreferenceManager
import android.util.Log
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationRequest.*
import de.krd.lmapraktikum_datacollector.GlobalModel
import de.krd.lmapraktikum_datacollector.R
import de.krd.lmapraktikum_datacollector.data.LocationData
import de.krd.lmapraktikum_datacollector.permission.PermissionActivity
import de.krd.lmapraktikum_datacollector.utils.PreferenceHelper


class LocationRecorder : SharedPreferences.OnSharedPreferenceChangeListener {
    companion object {
        fun compareLocations(l1: LocationData, l2: LocationData): Boolean {
            return l1.provider == l2.provider
                    && l1.latitude == l2.latitude
                    && l1.longitude == l2.longitude
                    && l1.accuracy == l2.accuracy
        }
    }


    private var useAndroidApi = true
    private var useFusedLocationApi = false

    private var run = false
    private var gpsEnabled = false
    private var networkEnabled = false
    private var activity: PermissionActivity
    private var minTimeMs = 0L
    private var minDistanceM = 0.0f
    private var interval = 10000L
    private var fastestInterval = 5000L
    private var priority = PRIORITY_HIGH_ACCURACY

    private val preferences: SharedPreferences
    private val model: GlobalModel

    private val locationManager: LocationManager
    private val locationProviderClient: FusedLocationProviderClient

    private val fusedApiLocationCallback: LocationCallback
    private val androidApiLocationListener: LocationListener

    private var locationRequest: LocationRequest? = null


    constructor(pActivity: PermissionActivity, pModel: GlobalModel) {
        activity = pActivity
        model = pModel
        /*
         * Get the location service
         */
        locationManager = activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationProviderClient = LocationServices.getFusedLocationProviderClient(this.activity)
        preferences = PreferenceManager.getDefaultSharedPreferences(activity)
        loadPreferences()
        preferences.registerOnSharedPreferenceChangeListener(this)

        fusedApiLocationCallback = createFusedLocationCallback()
        androidApiLocationListener = createAndroidApiLocationListener()
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
        if (useAndroidApi) {
            if (gpsEnabled) {
                activity.withPermission(Manifest.permission.ACCESS_FINE_LOCATION) {
                    locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        minTimeMs,
                        minDistanceM,
                        androidApiLocationListener
                    )
                }
            }

            if (networkEnabled) {
                activity.withPermission(Manifest.permission.ACCESS_COARSE_LOCATION) {
                    locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        minTimeMs,
                        minDistanceM,
                        androidApiLocationListener
                    )
                }
            }
        }
        if (useFusedLocationApi) {
            locationProviderClient.removeLocationUpdates(fusedApiLocationCallback)
            locationRequest = LocationRequest().setInterval(interval)
                .setFastestInterval(fastestInterval).setPriority(priority)

            activity.withPermission(Manifest.permission.ACCESS_FINE_LOCATION) {
                locationProviderClient.requestLocationUpdates(
                    locationRequest,
                    fusedApiLocationCallback,
                    Looper.myLooper()
                )
            }
        }
    }




    private fun removeLocationRequests() {
        locationManager.removeUpdates(androidApiLocationListener)
        locationProviderClient.removeLocationUpdates(fusedApiLocationCallback)
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




    private fun createFusedLocationCallback() : LocationCallback {
        return object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)

                if (locationResult != null && locationResult.lastLocation != null) {
                    Log.d(
                        locationResult.lastLocation?.provider,
                        "From: Google Api " +
                                "lat: ${locationResult.lastLocation?.latitude}"
                                + " lng: ${locationResult.lastLocation?.longitude}"
                                + " alt: ${locationResult.lastLocation?.altitude}"
                                + " acc: ${locationResult.lastLocation?.accuracy}"
                                + " prov: ${locationResult.lastLocation?.provider}"

                    )

                    val lastLocation = LocationData(
                        locationResult.lastLocation.time,
                        priority.toString(),
                        locationResult.lastLocation.latitude,
                        locationResult.lastLocation.longitude,
                        locationResult.lastLocation.altitude,
                        locationResult.lastLocation.accuracy
                    )
                    model.data.locations.add(lastLocation)
                }
            }
        }
    }
    private fun createAndroidApiLocationListener() : LocationListener {
        return LocationListener { location ->
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
                    location.provider,
                    "From: Android Api: " + "lat: ${location.latitude}" + " lng: ${location.longitude}"
                            + " alt: ${location.altitude}" + " acc: ${location.accuracy}"
                )

                model.data.locations.add(locationData);
            }
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
        priority = PreferenceHelper.getInt(
            activity,
            preferences,
            R.string.setting_location_priority
        )
        useAndroidApi = PreferenceHelper.getBoolean(
            activity,
            preferences,
            R.string.setting_location_android_api
        )

        useFusedLocationApi = PreferenceHelper.getBoolean(
            activity,
            preferences,
            R.string.setting_location_fused_location_api
        )
    }
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            activity.getString(R.string.setting_location_enable_gps),
            activity.getString(R.string.setting_location_enable_network),
            activity.getString(R.string.setting_location_update_time),
            activity.getString(R.string.setting_location_priority),
            activity.getString(R.string.setting_location_android_api),
            activity.getString(R.string.setting_location_fused_location_api),
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