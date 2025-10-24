package com.example.seekshakcom.utils

import android.content.Context
import android.util.Log
import com.example.seekshakcom.BuildConfig
import com.example.seekshakcom.model.LocationRequest
import com.example.seekshakcom.network.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

object LocationHelper {

    data class LocationData(
        val lat: Double,
        val lon: Double,
        val sublocality: String?,  // village/subLocality
        val area: String,
        val city: String,
        val state: String,
        val country: String
    )

    suspend fun getApproxLocation(
        context: Context,
        lat: Double,
        lon: Double
    ): LocationData? = withContext(Dispatchers.IO) {
        try {
            val token = BuildConfig.MAPPLS_MAP_SDK_KEY
            val url =
                "https://apis.mapmyindia.com/advancedmaps/v1/$token/rev_geocode?lat=$lat&lng=$lon"

            val request = Request.Builder().url(url).build()
            val client = OkHttpClient()
            val response = client.newCall(request).execute()

            val body = response.body?.string() ?: return@withContext null
            Log.d("MapplsResponse", "Raw response: $body")

            val json = JSONObject(body)

            if (json.has("error_code")) {
                val errorCode = json.optString("error_code")
                val errorDesc = json.optString("error_description", "Unknown error")
                Log.e("MapplsAPI", "Error: $errorCode - $errorDesc")

                if (errorCode == "CLIENT_CREDENTIAL_EXPIRED") {
                    return@withContext null
                }
            }

            val result = json.optJSONArray("results")?.optJSONObject(0)
                ?: return@withContext null

            // ✅ Updated fallback for free API
            val sublocality = result.optString("subLocality").takeIf { it.isNotBlank() }
                ?: result.optString("village").takeIf { it.isNotBlank() }
//                ?: result.optString("locality").takeIf { it.isNotBlank() }

            val poi = result.optString("poi")
            val area = poi.takeIf { it.isNotBlank() }
                ?: result.optString("locality").takeIf { it.isNotBlank() }
                ?: sublocality
                ?: result.optString("street").takeIf { it.isNotBlank() }
                ?: "Unknown Area"

            val city = result.optString("city").takeIf { it.isNotBlank() }
                ?: result.optString("subDistrict").takeIf { it.isNotBlank() }
                ?: result.optString("district").takeIf { it.isNotBlank() }
                ?: "Unknown City"

            val state = result.optString("state", "Unknown State")
            val country = result.optString("country", "India")

            Log.d("LocationHelper", "Parsed Location → $sublocality | $area, $city, $state, $country")

            context.getSharedPreferences("LocationPrefs", Context.MODE_PRIVATE).edit()
                .putString("selected_city", city)
                .putString("selected_area", area)
                .putString("selected_state", state)
                .putString("selected_country", country)
                .putString("selected_sublocality", sublocality)
                .putString("lat", lat.toString())
                .putString("lon", lon.toString())
                .apply()

            return@withContext LocationData(lat, lon, sublocality, area, city, state, country)

        } catch (e: Exception) {
            Log.e("LocationHelper", "getApproxLocation error: ${e.message}")
            return@withContext null
        }
    }

    suspend fun sendLocationToBackend(context: Context, location: LocationData) {
        val prefs = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val token = prefs.getString("token", null) ?: run {
            Log.e("LocationHelper", "Token missing from SharedPreferences")
            return
        }

        val locationRequest = LocationRequest(
            latitude = location.lat,
            longitude = location.lon,
            sublocality = location.sublocality,
            area = location.area,
            city = location.city,
            state = location.state,
            country = location.country
        )

        withContext(Dispatchers.IO) {
            try {
                val response = RetrofitInstance.locationApi.updateLocation(
                    locationRequest,
                    "Bearer $token"
                )

                if (response.isSuccessful) {
                    Log.d("LocationHelper", "Location successfully updated to backend.")
                } else {
                    val error = response.errorBody()?.string()
                    Log.e("LocationHelper", "Backend Error: $error")
                }
            } catch (e: Exception) {
                Log.e("LocationHelper", "sendLocationToBackend exception: ${e.message}")
            }
        }
    }
}
