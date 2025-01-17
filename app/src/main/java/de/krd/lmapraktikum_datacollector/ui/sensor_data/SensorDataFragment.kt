@file:Suppress("DEPRECATION")

package de.krd.lmapraktikum_datacollector.ui.sensor_data

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import de.krd.lmapraktikum_datacollector.GlobalModel
import de.krd.lmapraktikum_datacollector.R
import de.krd.lmapraktikum_datacollector.data.SensorData
import kotlinx.android.synthetic.main.fragment_sensor_data.*

class SensorDataFragment : Fragment(), Observer<MutableList<SensorData>> {
    private val model: GlobalModel by activityViewModels()
    private lateinit var adapter: SensorListViewAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sensor_data, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = SensorListViewAdapter(requireActivity())
        lvCurrentSensor.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        model.data.sensorEvents.observe(viewLifecycleOwner, this)
    }
    override fun onPause() {
        model.data.sensorEvents.removeObserver(this)
        super.onPause()
    }
    override fun onChanged(t: MutableList<SensorData>) {
        adapter.clearItems()
        adapter.addItems(t)
    }
}