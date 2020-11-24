package de.krd.lmapraktikum_datacollector.data

import android.hardware.SensorEvent
import android.location.Location
import android.provider.ContactsContract
import com.google.gson.Gson
import de.krd.lmapraktikum_datacollector.utils.MutableLiveDataList
import org.json.JSONArray
import org.json.JSONObject

class DataCollection {
    val locations = MutableLiveDataList<LocationData>()
    val sensorEvents = MutableLiveDataList<SensorData>()


    fun loadJSON(json: String) {
        locations.clear()
        sensorEvents.clear()
        val gson = Gson()

        val jsonObject = JSONObject(json)

        val jsonLocations = jsonObject.getJSONArray("locations")
        for (i in 0 until jsonLocations.length()) {
            val jsonLocation = jsonLocations.getJSONObject(i)

            val location = gson.fromJson(jsonLocation.toString(), LocationData::class.java)
            locations.add(location)
        }

        val jsonSensorEvents = jsonObject.getJSONArray("sensor_events")
        for (i in 0 until jsonSensorEvents.length()) {
            val jsonSensorEvent = jsonSensorEvents.getJSONObject(i)

            val sensorData = gson.fromJson(jsonSensorEvent.toString(), SensorData::class.java)
            sensorEvents.add(sensorData)
        }
    }

    fun getJSON(): String {
        var json = JSONObject()
        val gson = Gson()

        var jsonLocations = JSONArray()
        locations.value.forEach {
            jsonLocations.put(JSONObject(gson.toJson(it)))
        }
        json.put("locations", jsonLocations)

        var jsonSensorEvents = JSONArray()
        sensorEvents.value.forEach {
            jsonSensorEvents.put(JSONObject(gson.toJson(it)))
        }
        json.put("sensor_events", jsonSensorEvents)

        return json.toString()
    }
}