package de.krd.lmapraktikum_datacollector.data

import android.hardware.SensorEvent
import android.location.Location
import de.krd.lmapraktikum_datacollector.utils.MutableLiveDataList
import org.json.JSONObject

class DataCollection {
    val locations = MutableLiveDataList<Location>()
    val sensorEvents = MutableLiveDataList<SensorEvent>()

    companion object {
        fun toJSON(): JSONObject {
            var json = JSONObject()


            return json
        }

        fun fromJSON(json: JSONObject) : DataCollection {
            return DataCollection()
        }
    }
}