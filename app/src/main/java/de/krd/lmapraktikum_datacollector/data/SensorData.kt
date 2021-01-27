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
        //private val startUpTime = System.currentTimeMillis()

        fun fromSensorEvent(sensorEvent: SensorEvent) : SensorData {
            return SensorData(
                System.currentTimeMillis() + (sensorEvent.timestamp - SystemClock.elapsedRealtimeNanos()) / 1000000L,
                sensorEvent.sensor.type,
                sensorEvent.sensor.name,
                sensorEvent.values
            )
        }
    }

    fun toHRString() : String {
        var result = ""
        result += values.joinToString {
            String.format("%.3f", it)
        }
        return result
    }
}