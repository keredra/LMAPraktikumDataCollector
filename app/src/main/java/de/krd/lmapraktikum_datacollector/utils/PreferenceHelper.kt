package de.krd.lmapraktikum_datacollector.utils

import android.content.Context
import android.content.SharedPreferences
import java.lang.Exception

object PreferenceHelper {

    fun getBoolean(context: Context, preferences: SharedPreferences, stringResourceId: Int) : Boolean {
        var result = false
        try {
            result = preferences.getBoolean(context.getString(stringResourceId), false)
        } catch (e: Exception) {

        }
        return result
    }

    fun getFloat(context: Context, preferences: SharedPreferences, stringResourceId: Int) : Float {
        var result = 0.0f
        try {
            result = preferences.getString(context.getString(stringResourceId), "0.0")!!.toFloat()
        } catch (e: Exception) {

        }
        return result
    }

    fun getInt(context: Context, preferences: SharedPreferences, stringResourceId: Int) : Int {
        var result = 0
        try {
            result = preferences.getString(context.getString(stringResourceId), "0")!!.toInt()
        } catch (e: Exception) {

        }
        return result
    }
    fun getLong(context: Context, preferences: SharedPreferences, stringResourceId: Int) : Long {
        var result = 0L
        try {
            result = preferences.getString(context.getString(stringResourceId), "0")!!.toLong()
        } catch (e: Exception) {

        }
        return result
    }

    fun getString(context: Context, preferences: SharedPreferences, stringResourceId: Int) : String {
        var result = ""
        try {
            result = preferences.getString(context.getString(stringResourceId), "0")!!
        } catch (e: Exception) {

        }
        return result
    }
}