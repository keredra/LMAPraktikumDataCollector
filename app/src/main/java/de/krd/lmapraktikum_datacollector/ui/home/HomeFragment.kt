package de.krd.lmapraktikum_datacollector.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import de.krd.lmapraktikum_datacollector.GlobalModel
import de.krd.lmapraktikum_datacollector.R
import de.krd.lmapraktikum_datacollector.data.LocationData
import de.krd.lmapraktikum_datacollector.data.SensorData
import de.krd.lmapraktikum_datacollector.ui.location_data.LocationListviewAdapter
import de.krd.lmapraktikum_datacollector.ui.sensor_data.SensorListviewAdapter
import kotlinx.android.synthetic.main.fragment_home.*
import java.util.*
import kotlin.collections.ArrayList

class HomeFragment : Fragment(), Observer<MutableList<SensorData>>  {
    private val model: GlobalModel by activityViewModels()
    private lateinit var arrayAdapter: ArrayAdapter<SensorData>
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = SensorListviewAdapter(requireActivity(), model.data.sensorEvents.value.filter {it.type == 1 } as ArrayList<SensorData>)
        if(adapter.getCount()>1) {
            val currentAccelerometer = adapter.getItem(adapter.getCount()-1)
            val lastAccelerometer = adapter.getItem(adapter.getCount()-2)
            tv_currAcc.text = "Aktuelle Accelerometerwerte (X, Y, Z, Timestamp):\n" +currentAccelerometer.values[0].toString()+", "+currentAccelerometer.values[1].toString()+", "+currentAccelerometer.values[2].toString()+", "+ Date(currentAccelerometer.timestamp).toString()
            tv_lastAcc.text = "Letzte Accelerometerwerte (X, Y, Z, Timestamp):\n" +lastAccelerometer.values[0].toString()+", "+lastAccelerometer.values[1].toString()+", "+lastAccelerometer.values[2].toString()+", "+Date(lastAccelerometer.timestamp).toString()
            tv_currSampRateAcc.text = "Aktuelle Abtastrate Accelerometer: " +(currentAccelerometer.timestamp-lastAccelerometer.timestamp).toString()
        }
        val adapterGyro = SensorListviewAdapter(requireActivity(), model.data.sensorEvents.value.filter {it.type == 4 } as ArrayList<SensorData>)
        if(adapterGyro.getCount()>1) {
            val currentGyroskop = adapterGyro.getItem(adapterGyro.getCount()-1)
            val lastGyroskop = adapterGyro.getItem(adapterGyro.getCount()-2)
            tv_currGyro.text = "Aktuelle Gyroskopwerte (X, Y, Z, Timestamp):\n" +currentGyroskop.values[0].toString()+", "+currentGyroskop.values[1].toString()+", "+currentGyroskop.values[2].toString()+", "+ Date(currentGyroskop.timestamp).toString()
            tv_lastGyro.text = "Letzte Gyroskopwerte (X, Y, Z, Timestamp):\n" +lastGyroskop.values[0].toString()+", "+lastGyroskop.values[1].toString()+", "+lastGyroskop.values[2].toString()+", "+Date(lastGyroskop.timestamp).toString()
            tv_currSampRateGyro.text = "Aktuelle Abtastrate Gyroskop: " +(currentGyroskop.timestamp-lastGyroskop.timestamp).toString()
        }
        val adapterLocation = LocationListviewAdapter(requireActivity(), model.data.locations.value as ArrayList<LocationData>)
        if(adapterLocation.getCount()>1) {
            val currentLocation = adapterLocation.getItem(adapterLocation.getCount()-1)
            val lastLocation = adapterLocation.getItem(adapterLocation.getCount()-2)
            tv_currLocation.text = "Aktuelle Positionsdaten (Latitude, Longitude, Altitude, Timestamp):\n" +currentLocation.latitude.toString()+", "+currentLocation.longitude.toString()+", "+currentLocation.altitude.toString()+", "+ Date(currentLocation.timestamp).toString()
            tv_lastLocation.text = "Letzte Positionsdaten (Latitude, Longitude, Altitude, Timestamp):\n" +lastLocation.latitude.toString()+", "+lastLocation.longitude.toString()+", "+lastLocation.altitude.toString()+", "+Date(lastLocation.timestamp).toString()
            tv_currSampRateLocation.text = "Aktuelle Abtastrate Position: " +(currentLocation.timestamp-lastLocation.timestamp).toString()
        }
    }

    override fun onChanged(t: MutableList<SensorData>?) {
        arrayAdapter.notifyDataSetChanged()
    }
}