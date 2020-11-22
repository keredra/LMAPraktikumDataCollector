package de.krd.lmapraktikum_datacollector.ui.sensor_data

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import de.krd.lmapraktikum_datacollector.GlobalModel
import de.krd.lmapraktikum_datacollector.R
import kotlinx.android.synthetic.main.fragment_location_data.*
import kotlinx.android.synthetic.main.fragment_sensor_data.*

@Suppress("DEPRECATION")
class SensorDataFragment : Fragment() {
    private val model: GlobalModel by activityViewModels()
    private lateinit var preferences: SharedPreferences
    /*
    TODO: Implementierung der gesammelten Sensordaten unterhalb der aktuellen Sensordaten in diesem Fragment
    */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sensor_data, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        preferences = PreferenceManager.getDefaultSharedPreferences(activity)
        model.data.sensorEvents.observe(viewLifecycleOwner, Observer {
            val sensorEvents = it

            if (!sensorEvents.isEmpty()) {
                /*
                    TODO: Angabe des Sensortyps für die aktuellen Sensordaten
                */
                tvCurrentSensorData.text =
                    "X: " + sensorEvents.last().values[0] + " Y: " + sensorEvents.last().values[1] + " Z: " + sensorEvents.last().values[2]
            }
        })
    }
}