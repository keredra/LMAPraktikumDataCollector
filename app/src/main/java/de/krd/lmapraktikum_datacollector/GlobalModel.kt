package de.krd.lmapraktikum_datacollector

import androidx.lifecycle.ViewModel
import de.krd.lmapraktikum_datacollector.data.DataCollection
import org.json.JSONObject

class GlobalModel : ViewModel() {
    val data = DataCollection()
}