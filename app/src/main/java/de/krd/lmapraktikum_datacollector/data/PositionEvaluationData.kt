package de.krd.lmapraktikum_datacollector.data

import com.google.android.gms.maps.model.LatLng

data class PositionEvaluationData(val latLng: LatLng, var timestamp: Long) {
}