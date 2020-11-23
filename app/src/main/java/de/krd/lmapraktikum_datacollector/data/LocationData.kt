package de.krd.lmapraktikum_datacollector.data

import android.location.Location

data class LocationData(
    val timestamp: Long,
    val provider: String,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double,
    val accuracy: Float
) {

    companion object {
        fun fromLocation(location: Location) : LocationData {
            return LocationData(
                location.time,
                location.provider,
                location.latitude,
                location.longitude,
                location.altitude,
                location.accuracy
            )
        }
    }

}