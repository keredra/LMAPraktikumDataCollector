package de.krd.lmapraktikum_datacollector.data

import com.google.android.gms.maps.model.LatLng

data class PositionEvaluationData(val latLng: LatLng, var tsArrival: Long, var tsDepature: Long) {
}