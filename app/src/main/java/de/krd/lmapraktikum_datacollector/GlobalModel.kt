package de.krd.lmapraktikum_datacollector

import androidx.lifecycle.ViewModel
import de.krd.lmapraktikum_datacollector.data.DataCollection
import de.krd.lmapraktikum_datacollector.settings.SettingCollection
import org.json.JSONObject

class GlobalModel : ViewModel() {
    val data = DataCollection()
    val settings = SettingCollection()
}