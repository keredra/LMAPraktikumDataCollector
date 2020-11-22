package de.krd.lmapraktikum_datacollector.settings

data class Location(
    var gpsEnabled: Boolean,
    var networkEnabled: Boolean,
    var minTimeMs: Long,
    var minDistanceM: Float
)