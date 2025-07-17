package com.example.seekshakcom.utils

import android.content.Context
import com.google.gson.Gson

object LocationLoader {
    fun loadLocations(context: Context): LocationData {
        val jsonString = context.assets.open("locations.json").bufferedReader().use { it.readText() }
        return Gson().fromJson(jsonString, LocationData::class.java)
    }
}
