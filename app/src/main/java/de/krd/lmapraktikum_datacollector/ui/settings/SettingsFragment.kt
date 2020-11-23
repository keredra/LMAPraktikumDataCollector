package de.krd.lmapraktikum_datacollector.ui.settings

import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.activityViewModels
import androidx.preference.*
import de.krd.lmapraktikum_datacollector.GlobalModel
import de.krd.lmapraktikum_datacollector.R

class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {
    private val model: GlobalModel by activityViewModels()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
        updateSummary(preferenceScreen)
    }

    override fun onResume() {
        super.onResume()
        // Set up a listener whenever a key changes
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        // Unregister the listener whenever a key changes
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }
    private fun updateSummary(p: Preference) {
        if (p is PreferenceGroup) {
            val pGrp: PreferenceGroup = p as PreferenceGroup
            for (i in 0 until pGrp.preferenceCount) {
                updateSummary(pGrp.getPreference(i))
            }
        } else {
            updatePrefSummary(p)
        }
    }

    private fun updatePrefSummary(p: Preference) {
        if (p is ListPreference) {
            val listPref: ListPreference = p as ListPreference
            p.setSummary(listPref.getEntry())
        }
        if (p is EditTextPreference) {
            val editTextPref: EditTextPreference = p as EditTextPreference
            if (p.title.toString().toLowerCase().contains("password")) {
                p.summary = "******"
            } else {
                p.setSummary(editTextPref.getText())
            }
        }
        if (p is MultiSelectListPreference) {
            val editTextPref: EditTextPreference = p as EditTextPreference
            p.setSummary(editTextPref.getText())
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        updateSummary(preferenceScreen)
    }

}