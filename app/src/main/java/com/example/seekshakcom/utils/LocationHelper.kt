package com.example.seekshakcom.utils

import android.content.Context
import android.util.Log
import com.example.seekshakcom.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

object LocationHelper {

    suspend fun getApproxLocation(
        context: Context,
        lat: Double,
        lon: Double
    ): LocationData? = withContext(Dispatchers.IO) {
        try {
//            val token = BuildConfig.MAPPLS_MAP_SDK_KEY


            val url = "https://apis.mapmyindia.com/advancedmaps/v1/$token/rev_geocode?lat=$lat&lng=$lon"


            val request = Request.Builder().url(url).build()
            val client = OkHttpClient()

            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return@withContext null


            val json = JSONObject(body)
            val result = json.getJSONArray("results").optJSONObject(0)
            val poi = result.optString("poi")
            val area = if (poi.isNullOrEmpty()) {
                result.optString("locality", result.optString("street", "Unknown Area"))
            } else poi
            val city = result.optString("city", "Unknown City")
            val state = result.optString("state", "Unknown State")
            val country = result.optString("country", "India")

            Log.d("LocationHelper", "Parsed: $area, $city, $state, $country")

            val prefs = context.getSharedPreferences("LocationPrefs", Context.MODE_PRIVATE)
            prefs.edit()
                .putString("selected_city", city)
                .putString("selected_area", area)
                .apply()

            return@withContext LocationData(lat, lon, area, city, state, country)
        } catch (e: Exception) {
            return@withContext null
        }
    }

    data class LocationData(
        val lat: Double,
        val lon: Double,
        val area: String,
        val city: String,
        val state: String,
        val country: String
    )

}
