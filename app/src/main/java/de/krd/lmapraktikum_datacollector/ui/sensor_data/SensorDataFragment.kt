@file:Suppress("DEPRECATION")

package de.krd.lmapraktikum_datacollector.ui.sensor_data

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import de.krd.lmapraktikum_datacollector.GlobalModel
import de.krd.lmapraktikum_datacollector.R
import de.krd.lmapraktikum_datacollector.data.SensorData
import kotlinx.android.synthetic.main.fragment_sensor_data.*

class SensorDataFragment : Fragment(), Observer<MutableList<SensorData>> {
    private val model: GlobalModel by activityViewModels()
    private lateinit var arrayAdapter: ArrayAdapter<SensorData>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sensor_data, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = SensorListviewAdapter(requireActivity(), model.data.sensorEvents.value as ArrayList<SensorData>)
        lvCurrentSensor.adapter = adapter

    }

    override fun onChanged(t: MutableList<SensorData>?) {
        arrayAdapter.notifyDataSetChanged()
    }
}