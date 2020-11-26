package de.krd.lmapraktikum_datacollector.data

import android.hardware.SensorEvent
import android.os.SystemClock


data class SensorData(
    val timestamp: Long,
    val type: Int,
    val name: String,
    val values: FloatArray
) {
    companion object {
        private val startUpTime = System.currentTimeMillis()

        fun fromSensorEvent(sensorEvent: SensorEvent) : SensorData {
            return SensorData(
                startUpTime + sensorEvent.timestamp / 1000000,
                sensorEvent.sensor.type,
                sensorEvent.sensor.name,
                sensorEvent.values
            )
        }
    }
}