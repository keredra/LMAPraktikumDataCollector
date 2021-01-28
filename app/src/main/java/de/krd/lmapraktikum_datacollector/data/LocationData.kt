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
    fun toHRString() : String {
        var result = ""
        result += ""+ String.format("%.3f", latitude) + ", "
        result += ""+ String.format("%.3f", longitude) + ", "
        result += ""+ String.format("%.3f", altitude) + ", "
        result += ""+ String.format("%.0f", accuracy) + ""
        return result
    }

    fun toLocation() : Location {
        val location = Location(provider)
        location.time
        location.latitude = latitude
        location.longitude = longitude
        location.altitude = altitude
        location.accuracy = accuracy

        return location
    }
}