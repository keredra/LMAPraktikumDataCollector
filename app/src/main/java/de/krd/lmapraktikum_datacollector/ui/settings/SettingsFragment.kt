package de.krd.lmapraktikum_datacollector.ui.settings

import android.os.Bundle
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.ui.navigateUp
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import de.krd.lmapraktikum_datacollector.GlobalModel
import de.krd.lmapraktikum_datacollector.R

class SettingsFragment : PreferenceFragmentCompat() {
    private val model: GlobalModel by activityViewModels()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }

}