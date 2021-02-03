package de.krd.lmapraktikum_datacollector.data

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import de.krd.lmapraktikum_datacollector.utils.MutableLiveDataList
import org.json.JSONArray
import org.json.JSONObject
import org.w3c.dom.Element
import org.w3c.dom.NodeList
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader
import java.util.*

class DataCollection {
    val locations = MutableLiveDataList<LocationData>()
    val sensorEvents = MutableLiveDataList<SensorData>()
    val route = MutableLiveDataList<PositionEvaluationData>()

    fun loadJSON(json: String) {
        locations.clear()
        sensorEvents.clear()
        route.clear()

        val gson = Gson()

        val jsonObject = JSONObject(json)

        val locationsList = mutableListOf<LocationData>()
        val jsonLocations = jsonObject.getJSONArray("locations")
        for (i in 0 until jsonLocations.length()) {
            val jsonLocation = jsonLocations.getJSONObject(i)

            locationsList.add(gson.fromJson(jsonLocation.toString(), LocationData::class.java))
        }

        locations.addAll(locationsList)

        val sensorEventsList = mutableListOf<SensorData>()
        val jsonSensorEvents = jsonObject.getJSONArray("sensor_events")
        for (i in 0 until jsonSensorEvents.length()) {
            val jsonSensorEvent = jsonSensorEvents.getJSONObject(i)

            sensorEventsList.add(gson.fromJson(jsonSensorEvent.toString(), SensorData::class.java))
        }
        sensorEvents.addAll(sensorEventsList)

        val positionEvaluationDataList = mutableListOf<PositionEvaluationData>()
        val jsonPositionEvaluationData = jsonObject.getJSONArray("position_evaluation_data")
        for (i in 0 until jsonPositionEvaluationData.length()) {
            val jsonPositionEvaluation = jsonPositionEvaluationData.getJSONObject(i)

            positionEvaluationDataList.add(gson.fromJson(jsonPositionEvaluation.toString(), PositionEvaluationData::class.java))
        }
        route.addAll(positionEvaluationDataList)
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

        var jsonPositionEvaluationData = JSONArray()
        route.value.forEach{
            jsonPositionEvaluationData.put(JSONObject(gson.toJson(it)))
        }
        json.put("position_evaluation_data", jsonPositionEvaluationData)

        return json.toString()
    }

    fun loadGpx(string: String){
        route.clear()
        val factory: XmlPullParserFactory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware
        val parser = factory.newPullParser()
        parser.setInput(StringReader(string))
        var eventType = parser.eventType
        val positionEvaluationDataList = mutableListOf<PositionEvaluationData>()

        while (eventType != XmlPullParser.END_DOCUMENT) {
            val tagName = parser.name
            if (eventType == XmlPullParser.START_TAG) {
                try {
                    if (tagName == "rtept" || tagName == "wpt") {
                        val latitude = parser.getAttributeValue(0).toDouble()
                        val longitude = parser.getAttributeValue(1).toDouble()

                        positionEvaluationDataList.add(PositionEvaluationData(LatLng(latitude, longitude), 0L, 0L))
                    }
                } catch (e: Exception) {
                    Log.e("Fehler", "Parsen nicht möglich")
                }
            }
            eventType = parser.next()
        }
        route.addAll(positionEvaluationDataList)
    }
}