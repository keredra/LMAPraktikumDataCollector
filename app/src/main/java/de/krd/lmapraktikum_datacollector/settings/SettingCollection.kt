package de.krd.lmapraktikum_datacollector.settings

import androidx.lifecycle.MutableLiveData
import org.json.JSONObject

data class SettingCollection(
    var location: MutableLiveData<Location> = MutableLiveData<Location>(Location(true, true, 0, 0.0f)),
    var googleMaps: MutableLiveData<GoogleMaps> = MutableLiveData<GoogleMaps>(GoogleMaps(17.0f, true, 10000))
) {

    companion object {
        fun toJSON(): JSONObject {
            var json = JSONObject()

            return json
        }

        fun fromJSON(json: JSONObject) : SettingCollection {
            return SettingCollection()
        }
    }
}