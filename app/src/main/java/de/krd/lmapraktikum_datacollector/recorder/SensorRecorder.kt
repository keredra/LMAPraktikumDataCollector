@file:Suppress("DEPRECATION")

package de.krd.lmapraktikum_datacollector.recorder

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.Sensor.TYPE_ACCELEROMETER
import android.hardware.Sensor.TYPE_GYROSCOPE
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.preference.PreferenceManager
import android.util.Log
import de.krd.lmapraktikum_datacollector.GlobalModel
import de.krd.lmapraktikum_datacollector.R
import de.krd.lmapraktikum_datacollector.permission.PermissionActivity
import de.krd.lmapraktikum_datacollector.utils.PreferenceHelper

class SensorRecorder : SharedPreferences.OnSharedPreferenceChangeListener {
    private var run = false
    private var samplingPeriodUs = 0
    private var gyroscopeEnabled = false
    private var accelerometerEnabled = false
    private var activity: PermissionActivity
    private lateinit var model: GlobalModel
    private var sensorManager: SensorManager
    private val preferences: SharedPreferences

    private val sensorEventListener = object: SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        }

        override fun onSensorChanged(event: SensorEvent) {
            when(event?.sensor?.type){
                Sensor.TYPE_ACCELEROMETER,
                Sensor.TYPE_GYROSCOPE -> {
                    Log.d(event.sensor?.name, " X: ${event.values[0]} Y: ${event.values[1]} Z: ${event.values[2]}")
                    model.data.sensorEvents.add(event)
                }
            }
        }
    }
    constructor(activity: PermissionActivity, model: GlobalModel) {
        this.activity = activity
        this.model = model
        /*
         * Get the sensor service
         */
        sensorManager = activity.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        preferences = PreferenceManager.getDefaultSharedPreferences(activity)

        loadPreferences()
        preferences.registerOnSharedPreferenceChangeListener(this)
    }

    fun start() {
        run = true
        addSensorRequests()
    }

    fun stop() {
        run = false
        removeSensorRequests()
    }

    @SuppressLint("MissingPermission")
    private fun addSensorRequests() {
        if (gyroscopeEnabled) {
            sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(TYPE_GYROSCOPE), samplingPeriodUs)
        }
        if (accelerometerEnabled) {
            sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(TYPE_ACCELEROMETER), samplingPeriodUs)
        }
    }

    private fun removeSensorRequests() {
        sensorManager.unregisterListener(sensorEventListener)
    }

    private fun loadPreferences() {
        gyroscopeEnabled = PreferenceHelper.getBoolean(
            activity,
            preferences,
            R.string.setting_sensor_enable_gyroscope
        )
        accelerometerEnabled = PreferenceHelper.getBoolean(
            activity,
            preferences,
            R.string.setting_sensor_enable_accelerometer
        )
        samplingPeriodUs = PreferenceHelper.getInt(
            activity,
            preferences,
            R.string.setting_sensor_sampling_period
        )
    }
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            activity.getString(R.string.setting_sensor_enable_gyroscope),
            activity.getString(R.string.setting_sensor_enable_accelerometer),
            activity.getString(R.string.setting_sensor_sampling_period) -> {
                loadPreferences()
                if (run) {
                    removeSensorRequests()
                    addSensorRequests()
                }
            }
        }
    }
}