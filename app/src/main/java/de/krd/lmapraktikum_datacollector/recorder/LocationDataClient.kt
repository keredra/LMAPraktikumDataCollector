package de.krd.lmapraktikum_datacollector.recorder

import android.util.Log
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.gson.Gson
import de.krd.lmapraktikum_datacollector.data.LocationData
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONObject
import java.lang.Exception

class LocationDataClient() {
    private var client: Socket? = null
    private val gson = Gson()
    private val connected = MutableLiveData<Boolean>(false)

    private val onStateChangeListener = Emitter.Listener {
        connected.postValue(client!!.connected())
    }


    fun observeStatus(owner: LifecycleOwner, observer: Observer<Boolean>) {
        connected.observe(owner, observer)
    }

    fun connect(address: String) {
        disconnect()
        try {
            client = IO.socket("http://"+address+"/")
            client?.connect()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        client?.let {
            it.on(Socket.EVENT_CONNECT, onStateChangeListener)
            it.on(Socket.EVENT_DISCONNECT, onStateChangeListener)
            it.on(Socket.EVENT_CONNECT_ERROR, onStateChangeListener)
        }

    }

    fun disconnect() {
        client?.disconnect()
    }

    fun sendLocationData(contextName: String, locationData: LocationData) {
        val locationDataJson = JSONObject()

        locationDataJson.put("context", contextName)
        locationDataJson.put("data", JSONObject(gson.toJson(locationData)))

        client?.emit("LocationData", locationDataJson)
    }

}