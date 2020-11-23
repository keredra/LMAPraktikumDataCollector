package de.krd.lmapraktikum_datacollector.data

import android.hardware.SensorEvent

data class SensorData(
    val timestamp: Long,
    val type: Int,
    val name: String,
    val values: FloatArray
) {
    companion object {
        fun fromSensorEvent(sensorEvent: SensorEvent) : SensorData {
            return SensorData(
                sensorEvent.timestamp,
                sensorEvent.sensor.type,
                sensorEvent.sensor.name,
                sensorEvent.values
            )
        }
    }
}