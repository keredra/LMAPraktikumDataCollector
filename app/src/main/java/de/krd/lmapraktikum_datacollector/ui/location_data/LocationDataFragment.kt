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
import kotlinx.android.synthetic.main.fragment_location_data.*

class LocationDataFragment : Fragment() {
    private val model: GlobalModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        model.data.locations.observe(viewLifecycleOwner, Observer {
            val locations = it

            if (!locations.isEmpty()) {
                textExample.text = "Lat: "+locations.last().latitude + " Long: " +locations.last().longitude
            }


        })

        return inflater.inflate(R.layout.fragment_location_data, container, false)
    }

}