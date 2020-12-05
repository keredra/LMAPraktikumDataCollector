package de.krd.lmapraktikum_datacollector.data

import de.krd.lmapraktikum_datacollector.utils.MutableLiveDataList

class EvaluationCollection {
    val route = MutableLiveDataList<PositionEvaluationData>()
}