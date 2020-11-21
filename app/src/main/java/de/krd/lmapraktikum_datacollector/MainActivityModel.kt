package de.krd.lmapraktikum_datacollector

import androidx.lifecycle.ViewModel
import de.krd.lmapraktikum_datacollector.data.DataCollection
import de.krd.lmapraktikum_datacollector.data.SettingCollection

class MainActivityModel : ViewModel() {
    val data = DataCollection();
    val settings = SettingCollection(16.0f);


}