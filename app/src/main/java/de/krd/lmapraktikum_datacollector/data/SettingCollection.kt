package de.krd.lmapraktikum_datacollector.data

import org.json.JSONObject

data class SettingCollection(var zoomFactor: Float) {


    fun toJSON() : JSONObject {
        var json = JSONObject()

        return json
    }

    fun fromJSON(json: JSONObject) {

    }
}