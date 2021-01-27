@file:Suppress("DEPRECATION")

package de.krd.lmapraktikum_datacollector.ui.location_data

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import de.krd.lmapraktikum_datacollector.GlobalModel
import de.krd.lmapraktikum_datacollector.R
import de.krd.lmapraktikum_datacollector.data.LocationData
import kotlinx.android.synthetic.main.fragment_location_data.lvCurrentLocation

class LocationDataFragment : Fragment(), Observer<MutableList<LocationData>> {
    private val model: GlobalModel by activityViewModels()
    private lateinit var adapter: LocationListViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_location_data, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = LocationListViewAdapter(requireActivity())
        lvCurrentLocation.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        model.data.locations.observe(viewLifecycleOwner, this)
    }


    override fun onPause() {
        model.data.locations.removeObserver(this)
        super.onPause()
    }


    override fun onChanged(t: MutableList<LocationData>) {
        adapter.clearItems()
        adapter.addItems(t)
    }


}