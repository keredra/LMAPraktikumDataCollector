package de.krd.lmapraktikum_datacollector.ui.home

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager.getDefaultSharedPreferences
import de.krd.lmapraktikum_datacollector.GlobalModel
import de.krd.lmapraktikum_datacollector.R
import de.krd.lmapraktikum_datacollector.data.LocationData
import de.krd.lmapraktikum_datacollector.data.SensorData
import de.krd.lmapraktikum_datacollector.utils.PreferenceHelper
import kotlinx.android.synthetic.main.fragment_home.*
import java.util.*

class HomeFragment : Fragment(), Observer<MutableList<SensorData>>  {
    private val model: GlobalModel by activityViewModels()
    private lateinit var preferences: SharedPreferences
    private lateinit var preferencesEditor: SharedPreferences.Editor

    private lateinit var sensorObserver: Observer<MutableList<SensorData>>
    private lateinit var locationObserver: Observer<MutableList<LocationData>>

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        //val seekBar = seekBar ?????
        val rActivity = requireActivity()
        preferences = getDefaultSharedPreferences(rActivity)
        preferencesEditor = preferences.edit()
        val periodId = rActivity.getString(R.string.setting_sensor_sampling_period)

        seekBar.progress = PreferenceHelper.getInt(rActivity, preferences, R.string.setting_sensor_sampling_period)
        seekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val value = ""+progress
                //Log.d("Progress", samplingRate.toString()) // ?????
                Log.d("Progress", value)
                //preferencesEditor.putLong("@string/setting_sensor_sampling_period",samplingRate) ???
                preferencesEditor.putString(periodId, value)
                preferencesEditor.commit()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })

        sensorObserver = Observer<MutableList<SensorData>> { sensorDataList ->
            val accelerometerDataList = sensorDataList.filter { it.type == 1 }
            val gyroscopeDataList = sensorDataList.filter { it.type == 4 }
            if(accelerometerDataList.size > 1) {
                val currentAccelerometer = accelerometerDataList.get(accelerometerDataList.size-1)
                val lastAccelerometer = accelerometerDataList.get(accelerometerDataList.size-2)
                tv_currAcc.text = "Aktuelle Accelerometerwerte (X, Y, Z, Timestamp):\n" +currentAccelerometer.values[0]+", "+currentAccelerometer.values[1]+", "+currentAccelerometer.values[2]+"\n "+ Date(currentAccelerometer.timestamp)
                tv_lastAcc.text = "Letzte Accelerometerwerte (X, Y, Z, Timestamp):\n" +lastAccelerometer.values[0]+", "+lastAccelerometer.values[1]+", "+lastAccelerometer.values[2]+"\n "+Date(lastAccelerometer.timestamp)
                tv_currSampRateAcc.text = "Aktuelle Abtastrate Accelerometer: " +(currentAccelerometer.timestamp-lastAccelerometer.timestamp)
            }
            if (gyroscopeDataList.size > 1) {
                val currentGyroskop = gyroscopeDataList.get(gyroscopeDataList.size - 1)
                val lastGyroskop = gyroscopeDataList.get(gyroscopeDataList.size - 2)
                tv_currGyro.text = "Aktuelle Gyroskopwerte (X, Y, Z, Timestamp):\n" + currentGyroskop.values[0] + ", " + currentGyroskop.values[1] + ", " + currentGyroskop.values[2] + "\n " + Date(currentGyroskop.timestamp)
                tv_lastGyro.text = "Letzte Gyroskopwerte (X, Y, Z, Timestamp):\n" + lastGyroskop.values[0] + ", " + lastGyroskop.values[1] + ", " + lastGyroskop.values[2] + "\n " + Date(lastGyroskop.timestamp)
                tv_currSampRateGyro.text = "Aktuelle Abtastrate Gyroskop: " + (currentGyroskop.timestamp - lastGyroskop.timestamp)
            }
        }
        locationObserver = Observer { locationDataList ->
            if(locationDataList.size > 1) {
                val currentLocation = locationDataList.get(locationDataList.size - 1)
                val lastLocation = locationDataList.get(locationDataList.size - 2)
                tv_currLocation.text = "Aktuelle Positionsdaten (Latitude, Longitude, Altitude, Timestamp):\n" + currentLocation.latitude + ", " + currentLocation.longitude + ", " + currentLocation.altitude + "\n " + Date(currentLocation.timestamp)
                tv_lastLocation.text = "Letzte Positionsdaten (Latitude, Longitude, Altitude, Timestamp):\n" + lastLocation.latitude + ", " + lastLocation.longitude + ", " + lastLocation.altitude + "\n " + Date(lastLocation.timestamp)
                tv_currSampRateLocation.text = "Aktuelle Abtastrate Position: " + (currentLocation.timestamp - lastLocation.timestamp)
            }
        }

    }

    override fun onPause() {
        super.onPause()
        model.data.sensorEvents.removeObserver(sensorObserver)
        model.data.locations.removeObserver(locationObserver)
    }

    override fun onResume() {
        super.onResume()
        model.data.sensorEvents.observe(viewLifecycleOwner, sensorObserver)
        model.data.locations.observe(viewLifecycleOwner, locationObserver)
    }
    override fun onChanged(t: MutableList<SensorData>?) {

    }
}