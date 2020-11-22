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
import de.krd.lmapraktikum_datacollector.permission.PermissionActivity

class SensorRecorder {
    private var run = false
    private var activity: PermissionActivity
    private lateinit var model: GlobalModel
    private var sensorManager: SensorManager
    private var preferences: SharedPreferences

    constructor(activity: PermissionActivity, model: GlobalModel) {
        this.activity = activity
        this.model = model
        /*
         * Get the sensor service
         */
        sensorManager = activity.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        preferences = PreferenceManager.getDefaultSharedPreferences(activity)
        /*
        TODO: Implementierung entsprechender Preferences als Key erforderlich, z.B. activity.getString(R.string.setting_sensor_enable_accelerometer), activity.getString(R.string.setting_sensor_enable_gyroscope)
         */
        preferences.registerOnSharedPreferenceChangeListener { sharedPreferences, key ->
            run {
                if (run) {
                    removeSensorRequests()
                    addSensorRequests()
                }
            }
        }
    }

    private val sensorListener by lazy {
        object: SensorEventListener {
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            }

            override fun onSensorChanged(event: SensorEvent) {
                val sensorEvents = model.data.sensorEvents.value
                if(sensorEvents.isEmpty() || !isLastSensorEvent(event))
                    when (event.sensor?.type) {
                        TYPE_ACCELEROMETER -> {
                            Log.d("Accelerometer", " X: ${event.values[0]}\n Y: ${event.values[1]} \n Z: ${event.values[2]} ")
                            model.data.sensorEvents.add(event)
                        }
                        TYPE_GYROSCOPE -> {
                            Log.d("Gyroskop", " X: ${event.values[0]}\n Y: ${event.values[1]} \n Z: ${event.values[2]} ")
                            model.data.sensorEvents.add(event)
                        }
                    }

            }
        }
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
        /*
        TODO: Implementierung entsprechender Preferences erforderlich
         */
        val samplingRate = 10000
        val sensorType = 1
        when (sensorType) {
            TYPE_ACCELEROMETER -> {
                sensorManager.registerListener(sensorListener, sensorManager.getDefaultSensor(TYPE_ACCELEROMETER), samplingRate)
            }
            TYPE_GYROSCOPE -> {
                sensorManager.registerListener(sensorListener, sensorManager.getDefaultSensor(TYPE_GYROSCOPE), samplingRate)
            }
        }
    }

    private fun removeSensorRequests() {
        sensorManager.unregisterListener(sensorListener)
    }

    private fun isLastSensorEvent(event: SensorEvent) : Boolean {
        val sensorEvents = model.data.sensorEvents.value
        var isLastSensorEvent = false
        if (!sensorEvents.isEmpty()) {
            val itr = sensorEvents.listIterator(sensorEvents.size)

            var lastSensorEventTypeChecked = false
            while (!isLastSensorEvent && !lastSensorEventTypeChecked && itr.hasPrevious()) {
                val listSensorEvent = itr.previous()
                isLastSensorEvent = compareSensorEvents(event, listSensorEvent)
                lastSensorEventTypeChecked = event.sensor.type.equals(listSensorEvent.sensor.type)
            }
        }
        return isLastSensorEvent
    }
    companion object {
        fun compareSensorEvents(e1: SensorEvent, e2: SensorEvent): Boolean {
            return e1.sensor.type.equals(e2.sensor.type)
                    && e1.values[0] == e2.values[0]
                    && e1.values[1] == e2.values[1]
                    && e1.values[2] == e2.values[2]
        }
    }
}