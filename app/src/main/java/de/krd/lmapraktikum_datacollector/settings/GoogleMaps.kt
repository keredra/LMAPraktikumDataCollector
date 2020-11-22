package de.krd.lmapraktikum_datacollector.settings

data class GoogleMaps(
    var zoomFactor: Float,
    var followEnabled: Boolean,
    var followTimeoutMs: Long
)