@file:Suppress("DEPRECATION")

package de.krd.lmapraktikum_datacollector.recorder

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import android.preference.PreferenceManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationRequest.*
import de.krd.lmapraktikum_datacollector.GlobalModel
import de.krd.lmapraktikum_datacollector.R
import de.krd.lmapraktikum_datacollector.data.LocationData
import de.krd.lmapraktikum_datacollector.permission.PermissionActivity
import de.krd.lmapraktikum_datacollector.utils.PreferenceHelper


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
    private var locationProviderClient: FusedLocationProviderClient
    var locationRequest: LocationRequest? = null
    private lateinit var locationCallback: LocationCallback
    var interval = 10000 //
    var fastestInterval = 5000 //This method sets the fastest rate in milliseconds at which your app can handle location updates
    private var priority = PRIORITY_HIGH_ACCURACY
    private var cbAndroidApi = true


    constructor(activity: PermissionActivity, model: GlobalModel) {
        this.activity = activity;
        this.model = model;
        /*
         * Get the location service
         */
        locationManager = activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationProviderClient = LocationServices.getFusedLocationProviderClient(this.activity);  //wo soll ich das sonst initialisieren?
        locationCallback = getLocationCallback();
        locationRequest = LocationRequest().setInterval(interval.toLong()).setFastestInterval(fastestInterval.toLong()).setPriority(priority)
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
                location.provider,
                    "From: Android Api: "+"lat: ${location.latitude}" + " lng: ${location.longitude}"
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
        if(cbAndroidApi) {
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

        if(!cbAndroidApi) {
            if (priority == 0) {
                getLocationCallback()?.let { updateLocationRequest(it, interval, fastestInterval, LocationRequest.PRIORITY_HIGH_ACCURACY) };
                Log.i("priority:", "HIGH_ACCURACY")
                //TODO fastestIntervall muss man auch noch uebergeben koennen
            }
            if (priority == 1) {
                getLocationCallback()?.let { updateLocationRequest(it, interval, fastestInterval, LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY) };
                Log.i("priority:", "BALANCED_POWER_ACCURACY")
            }
            if (priority == 2) {
                getLocationCallback()?.let { updateLocationRequest(it, interval, fastestInterval, LocationRequest.PRIORITY_LOW_POWER) };
                Log.i("priority:", "LOW_POWER")
            }
            if (priority == 3) {
                getLocationCallback()?.let { updateLocationRequest(it, interval, fastestInterval, LocationRequest.PRIORITY_NO_POWER) };
                Log.i("priority:", "NO_POWER")
            }
        }
    }

    private fun getLocationCallback(): LocationCallback {
        return object : LocationCallback() {
            override fun onLocationResult(location_result: LocationResult?) {
                super.onLocationResult(location_result)

                if(location_result != null && location_result.lastLocation != null ){
                    Log.d(
                            location_result?.lastLocation?.provider,
                            "From: Google Api "+
                                    "lat: ${location_result?.lastLocation?.latitude}"
                            + " lng: ${location_result?.lastLocation?.longitude}"
                            + " alt: ${location_result?.lastLocation?.altitude}"
                            + " acc: ${location_result?.lastLocation?.accuracy}"

                    )

                    val lastLocation = LocationData(location_result.lastLocation.time,location_result.lastLocation.provider,
                            location_result.lastLocation.latitude,location_result.lastLocation.longitude,
                            location_result.lastLocation.altitude,location_result.lastLocation.accuracy)
                    model.data.locations.add(lastLocation)
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun updateLocationRequest(pLocationCallback: LocationCallback , pInterval: Int, pFastestInterval: Int, priority: Int) {
        locationProviderClient.removeLocationUpdates(locationCallback)
        locationCallback = pLocationCallback
        locationRequest = LocationRequest().setInterval(pInterval.toLong()).setFastestInterval(pFastestInterval.toLong()).setPriority(priority)
        locationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
    }


    private fun removeLocationRequests() {
        locationManager.removeUpdates(locationListener)
        locationProviderClient.removeLocationUpdates(locationCallback)
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
        priority = PreferenceHelper.getInt(
                activity,
                preferences,
                R.string.setting_location_priority
        )
        cbAndroidApi = PreferenceHelper.getBoolean(
                activity,
                preferences,
                R.string.setting_location_checkBox_AndroidApi
        )
    }
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            "setting_location_enable_gps",
            "setting_location_enable_network",
            "setting_location_update_time",
            "setting_location_priority",
            "setting_location_checkBox_AndroidApi",
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