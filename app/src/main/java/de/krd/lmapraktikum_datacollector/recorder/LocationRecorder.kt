package de.krd.lmapraktikum_datacollector.recorder

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import androidx.preference.PreferenceManager
import android.util.Log
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationRequest.*
import de.krd.lmapraktikum_datacollector.GlobalModel
import de.krd.lmapraktikum_datacollector.R
import de.krd.lmapraktikum_datacollector.data.LocationData
import de.krd.lmapraktikum_datacollector.data.SensorData
import de.krd.lmapraktikum_datacollector.permission.PermissionActivity
import de.krd.lmapraktikum_datacollector.utils.PreferenceHelper
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue


class LocationRecorder : SharedPreferences.OnSharedPreferenceChangeListener {
    companion object {
        private fun compareLocations(l1: LocationData, l2: LocationData): Boolean {
            return l1.provider == l2.provider
                    && l1.latitude == l2.latitude
                    && l1.longitude == l2.longitude
                    && l1.accuracy == l2.accuracy
        }

        private enum class Strategies(val id: Int) {
            PERIODIC(1),
            PERIODIC_DISTANCE(2),
            DISTANCE(3),
            DISTANCE_STATIC_SPEED(4),
            DISTANCE_DYNAMIC_SPEED(5)
        }

        private const val KM = 1000
        private const val SECOND : Long = 1000
        private const val MINUTE : Long = 60 * SECOND
        private const val HOUR : Long = 60 * MINUTE
    }


    private var useAndroidApi = true
    private var useFusedLocationApi = false
    private var useCustomDistanceValidation = false
    private var useSensorBasedMovementDetection = false

    private var strategy = Strategies.PERIODIC.id

    private var run = false
    private var gpsEnabled = false
    private var networkEnabled = false


    private var sensorManager: SensorManager
    private var movementDetectionTimedOut = false
    private var timeoutMovement = 0.0f
    private var timeoutMovementStartTime = 0L

    private var movementDetectionTimeout = 10
    private var movementDetectionThreshold = 100.0f


    private var activity: PermissionActivity
    private var minTime = 0L
    private var minDistance = 1.0f
    private var maxSpeed = 7.0f
    private var interval = 10000L
    private var fastestInterval = 5000L
    private var priority = PRIORITY_HIGH_ACCURACY

    private val preferences: SharedPreferences
    private val model: GlobalModel

    private val locationManager: LocationManager
    private val locationProviderClient: FusedLocationProviderClient

    private val fusedApiLocationCallback: LocationCallback
    private val androidApiLocationListener: LocationListener
    private val sensorEventListener : SensorEventListener

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
        sensorManager = activity.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorEventListener = createSensorEventListener()
    }

    fun start() {
        run = true;
        setLocationRequests()
        if (useSensorBasedMovementDetection) {
            startMotionDetection()
        }
    }

    fun stop() {
        run = false
        removeLocationRequests()
        sensorManager.unregisterListener(sensorEventListener)
    }

    private fun setLocationRequests() {
        if (useAndroidApi) {
            setAndroidApiLocationRequestByStrategy()
        }
        if (useFusedLocationApi) {
            setFusedLocationApiRequest(interval, fastestInterval, priority)
        }
    }

    private fun setAndroidApiLocationRequestByStrategy() {
        when(strategy) {
            Strategies.PERIODIC.id -> setAndroidApiRequest(minTime, 0f)
            Strategies.PERIODIC_DISTANCE.id -> setAndroidApiRequest(minTime, if (useCustomDistanceValidation) 0f else minDistance)
            Strategies.DISTANCE.id -> setAndroidApiRequest(0, if (useCustomDistanceValidation) 0f else minDistance)
            Strategies.DISTANCE_STATIC_SPEED.id, Strategies.DISTANCE_DYNAMIC_SPEED.id
            -> setAndroidApiRequest(0, 0f)
        }
    }

    @SuppressLint("MissingPermission")
    private fun setFusedLocationApiRequest(_interval: Long, _fastestInterval: Long, _priority: Int) {
        locationProviderClient.removeLocationUpdates(fusedApiLocationCallback)
        locationRequest = LocationRequest().setInterval(_interval)
                .setFastestInterval(_fastestInterval).setPriority(_priority)

        activity.withPermission(Manifest.permission.ACCESS_FINE_LOCATION) {
            locationProviderClient.requestLocationUpdates(
                    locationRequest,
                    fusedApiLocationCallback,
                    Looper.myLooper()
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun setAndroidApiRequest(_minTime: Long, _minDistance: Float) {
        locationManager.removeUpdates(androidApiLocationListener)
        if (gpsEnabled) {
            activity.withPermission(Manifest.permission.ACCESS_FINE_LOCATION) {
                locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        _minTime,
                        _minDistance,
                        androidApiLocationListener
                )
            }
        }

        if (networkEnabled) {
            activity.withPermission(Manifest.permission.ACCESS_COARSE_LOCATION) {
                locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        _minTime,
                        _minDistance,
                        androidApiLocationListener
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
        if (locations.isNotEmpty()) {
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
                    val location = LocationData(
                        locationResult.lastLocation.time,
                        priority.toString(),
                        locationResult.lastLocation.latitude,
                        locationResult.lastLocation.longitude,
                        locationResult.lastLocation.altitude,
                        locationResult.lastLocation.accuracy
                    )
                    model.data.locations.add(location)
                }
            }
        }
    }

    private fun createAndroidApiLocationListener() : LocationListener {
        return LocationListener { location ->
            val locations = model.data.locations
            val locationData = LocationData.fromLocation(location)

            if (locations.value.isEmpty() || !isLastProviderLocation(locationData)) {
                when(strategy) {
                    Strategies.PERIODIC.id -> {
                        locations.add(locationData)
                    }
                    Strategies.PERIODIC_DISTANCE.id,
                    Strategies.DISTANCE.id -> {
                        if (useCustomDistanceValidation && locations.value.size >= 1) {
                            val lastLocation = locations.value.last().toLocation()
                            if (lastLocation.distanceTo(location) >= minDistance) {
                                locations.add(locationData)
                            }
                        } else {
                            locations.add(locationData)
                        }
                    }
                    Strategies.DISTANCE_STATIC_SPEED.id -> {
                        if (useCustomDistanceValidation && locations.value.size >= 1) {
                            val lastLocation = locations.value.last().toLocation()
                            if (lastLocation.distanceTo(location) >= minDistance) {
                                locations.add(locationData)
                            }
                        } else {
                            locations.add(locationData)
                        }
                        val lastLocation = locations.value.last().toLocation()
                        val distanceToLastLocation = lastLocation.distanceTo(location)
                        locationManager.removeUpdates(androidApiLocationListener)
                        GlobalScope.launch {
                            delay(((minDistance-distanceToLastLocation) / (maxSpeed * KM / HOUR)).toLong())
                            activity.runOnUiThread {
                                setAndroidApiRequest(0, 0f)
                            }
                        }

                    }
                    Strategies.DISTANCE_DYNAMIC_SPEED.id -> {
                        if (useCustomDistanceValidation && locations.value.size >= 1) {
                            val lastLocation = locations.value.last().toLocation()
                            if (lastLocation.distanceTo(location) >= minDistance) {
                                locations.add(locationData)
                            }
                        } else {
                            locations.add(locationData)
                        }
                        val lastLocation = locations.value.last().toLocation()
                        val distanceToLastLocation = lastLocation.distanceTo(location)
                        val currentSpeed = if (lastLocation.speed > 0) lastLocation.speed * SECOND else maxSpeed * KM / HOUR
                        val timeToNextFix = ((minDistance-distanceToLastLocation) / currentSpeed).toLong()

                        locationManager.removeUpdates(androidApiLocationListener)
                        GlobalScope.launch {
                            delay(timeToNextFix)
                            activity.runOnUiThread {
                                setAndroidApiRequest(0, 0f)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun createSensorEventListener() : SensorEventListener {
        return object: SensorEventListener {
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            }
            override fun onSensorChanged(event: SensorEvent) {
                when(event?.sensor?.type){
                    Sensor.TYPE_LINEAR_ACCELERATION -> {
                        event.values.forEach {
                            timeoutMovement += it.absoluteValue
                        }
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - timeoutMovementStartTime
                                > movementDetectionTimeout * SECOND) {
                            if (timeoutMovementStartTime > 0 && timeoutMovement < movementDetectionThreshold) {
                                movementDetectionTimedOut = true
                                removeLocationRequests()
                            }
                            timeoutMovementStartTime = currentTime
                            timeoutMovement = 0f

                        } else if (movementDetectionTimedOut && timeoutMovement > movementDetectionThreshold) {
                            movementDetectionTimedOut = false
                            setLocationRequests()
                        }
                    }
                }
            }
        }
    }

    private fun startMotionDetection() {
        sensorManager.unregisterListener(sensorEventListener)
        if (useSensorBasedMovementDetection) {
            sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), SensorManager.SENSOR_DELAY_NORMAL)
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
        minTime = PreferenceHelper.getLong(
            activity,
            preferences,
            R.string.setting_location_update_time
        )
        minDistance = PreferenceHelper.getFloat(
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

        useCustomDistanceValidation = PreferenceHelper.getBoolean(
            activity,
            preferences,
            R.string.setting_location_custom_distance_validation
        )

        strategy = PreferenceHelper.getInt(
            activity,
            preferences,
            R.string.setting_location_strategy
        )

        maxSpeed = PreferenceHelper.getFloat(
            activity,
            preferences,
            R.string.setting_location_max_speed
        )

        useSensorBasedMovementDetection = PreferenceHelper.getBoolean(
            activity,
            preferences,
            R.string.setting_location_movement_detection
        )

        movementDetectionThreshold = PreferenceHelper.getFloat(
                activity,
                preferences,
                R.string.setting_location_movement_detection_threshold
        )

        movementDetectionTimeout = PreferenceHelper.getInt(
                activity,
                preferences,
                R.string.setting_location_movement_detection_timeout
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
            activity.getString(R.string.setting_location_min_distance),
            activity.getString(R.string.setting_location_custom_distance_validation),
            activity.getString(R.string.setting_location_strategy),
            activity.getString(R.string.setting_location_max_speed),
            activity.getString(R.string.setting_location_movement_detection),
            activity.getString(R.string.setting_location_movement_detection_threshold),
            activity.getString(R.string.setting_location_movement_detection_timeout)  -> {
                loadPreferences()
                if (run) {
                    start()
                }


            }
        }
    }
}