package de.krd.lmapraktikum_datacollector

import androidx.lifecycle.ViewModel
import de.krd.lmapraktikum_datacollector.data.DataCollection
import de.krd.lmapraktikum_datacollector.data.EvaluationCollection
import org.json.JSONObject

class GlobalModel : ViewModel() {
    val data = DataCollection()
    val evaluationData = EvaluationCollection()
}