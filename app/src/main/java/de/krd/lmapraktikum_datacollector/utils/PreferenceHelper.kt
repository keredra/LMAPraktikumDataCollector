package de.krd.lmapraktikum_datacollector.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.fragment.app.Fragment
import de.krd.lmapraktikum_datacollector.R
import java.lang.Exception

object PreferenceHelper {

    fun getBoolean(context: Context, preferences: SharedPreferences, stringRessourceId: Int) : Boolean {
        var result = false
        try {
            result = preferences.getBoolean(context.getString(stringRessourceId), false)
        } catch (e: Exception) {

        }
        return result
    }

    fun getFloat(context: Context, preferences: SharedPreferences, stringRessourceId: Int) : Float {
        var result = 0.0f
        try {
            result = preferences.getString(context.getString(stringRessourceId), "0.0")!!.toFloat()
        } catch (e: Exception) {

        }
        return result
    }

    fun getInt(context: Context, preferences: SharedPreferences, stringRessourceId: Int) : Int {
        var result = 0
        try {
            result = preferences.getString(context.getString(stringRessourceId), "0")!!.toInt()
        } catch (e: Exception) {

        }
        return result
    }
    fun getLong(context: Context, preferences: SharedPreferences, stringRessourceId: Int) : Long {
        var result = 0L
        try {
            result = preferences.getString(context.getString(stringRessourceId), "0")!!.toLong()
        } catch (e: Exception) {

        }
        return result
    }
}