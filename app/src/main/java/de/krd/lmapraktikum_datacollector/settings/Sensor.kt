package de.krd.lmapraktikum_datacollector.settings

data class Sensor (
    var gyroEnabled : Boolean,
    var accelerometerEnabled : Boolean,
    var samplingRate : Int
)