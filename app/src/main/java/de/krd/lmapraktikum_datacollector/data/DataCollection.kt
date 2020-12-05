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

class DataCollection {
    val locations = MutableLiveDataList<LocationData>()
    val sensorEvents = MutableLiveDataList<SensorData>()
    val latLng = MutableLiveDataList<LatLng>()

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

    fun extractLatLng(string: String){
        latLng.clear()
        val factory: XmlPullParserFactory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware
        val parser = factory.newPullParser()
        parser.setInput(StringReader(string))
        var eventType = parser.eventType

        while (eventType != XmlPullParser.END_DOCUMENT) {
            val tagName = parser.name
            if (eventType == XmlPullParser.START_TAG) {
                try {
                    if (tagName.equals("rtept")) {
                        val latitude = parser.getAttributeValue(0).toDouble()
                        val longitude = parser.getAttributeValue(1).toDouble()
                        val wayPoint = LatLng(latitude, longitude)
                        latLng.add(wayPoint)
                        Log.i("Waypoint", ""+wayPoint.latitude+","+wayPoint.longitude)
                    }
                } catch (e: Exception) {
                    Log.e("Fehler", "Parsen nicht möglich")

                }
            }
            eventType = parser.next()
        }
    }
}