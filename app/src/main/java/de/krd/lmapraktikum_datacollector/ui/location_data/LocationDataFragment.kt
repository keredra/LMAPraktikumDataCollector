package de.krd.lmapraktikum_datacollector.ui.location_data

import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import de.krd.lmapraktikum_datacollector.GlobalModel
import de.krd.lmapraktikum_datacollector.R
import kotlinx.android.synthetic.main.fragment_location_data.*

class LocationDataFragment : Fragment() {
    private val model: GlobalModel by activityViewModels()
    private lateinit var preferences: SharedPreferences

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

            if (!locations.isEmpty()) {
                textExample.text =
                    "Lat: " + locations.last().latitude + " Long: " + locations.last().longitude
            }
        })
        val gps = preferences.getBoolean(getString(R.string.setting_location_enable_gps), false)
        textExample.text = ""+gps

    }

}