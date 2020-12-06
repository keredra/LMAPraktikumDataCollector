package de.krd.lmapraktikum_datacollector

import androidx.lifecycle.ViewModel
import de.krd.lmapraktikum_datacollector.data.DataCollection

class GlobalModel : ViewModel() {
    val data = DataCollection()
}