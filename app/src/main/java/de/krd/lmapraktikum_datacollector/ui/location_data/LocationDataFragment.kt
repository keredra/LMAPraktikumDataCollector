@file:Suppress("DEPRECATION")

package de.krd.lmapraktikum_datacollector.ui.location_data

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import de.krd.lmapraktikum_datacollector.GlobalModel
import de.krd.lmapraktikum_datacollector.R
import de.krd.lmapraktikum_datacollector.data.LocationData
import kotlinx.android.synthetic.main.fragment_location_data.*
import kotlinx.android.synthetic.main.fragment_location_data.lvCurrentLocation

class LocationDataFragment : Fragment() {
    private val model: GlobalModel by activityViewModels()
    private lateinit var preferences: SharedPreferences
    var locationList = ArrayList<LocationData>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_location_data, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        preferences = PreferenceManager.getDefaultSharedPreferences(activity)
        model.data.locations.observe(viewLifecycleOwner, Observer {
            val locations = it
            val adapter = ArrayAdapter(requireActivity(), android.R.layout.simple_list_item_1, it)
            lvCurrentLocation.adapter = adapter

            if (!locations.isEmpty()) {
                locationList.add(locations.last())
                adapter.notifyDataSetChanged()
            }
        })
    }
}