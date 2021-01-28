package de.krd.lmapraktikum_datacollector.recorder

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
    private val onConnectListener = Emitter.Listener {
        connected.postValue(true)
    }
    private val onDisconnectListener = Emitter.Listener {
        connected.postValue(true)
    }

    public fun observeStatus(owner: LifecycleOwner, observer: Observer<Boolean>) {
        connected.observe(owner, observer)
    }

    public fun connect(address: String) {
        disconnect()
        try {
            client = IO.socket("http://"+address)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        client?.let {
            it.on(Socket.EVENT_CONNECT, onConnectListener)
            it.on(Socket.EVENT_DISCONNECT, onDisconnectListener)
            it.on(Socket.EVENT_CONNECT_ERROR, onDisconnectListener)
        }

    }

    public fun disconnect() {
        client?.let{
            connected.postValue(false)
        }
        client?.disconnect()
    }

    public fun sendLocationData(contextName: String, locationData: LocationData) {
        val locationDataJson = JSONObject()

        locationDataJson.put("context", contextName)
        locationDataJson.put("data", gson.toJson(locationData))

        client?.emit("LocationData", locationDataJson)
    }

}