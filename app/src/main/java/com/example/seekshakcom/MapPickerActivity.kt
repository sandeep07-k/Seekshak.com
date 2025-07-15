package com.example.seekshakcom

import android.content.Context
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.seekshakcom.model.LocationRequest
import com.example.seekshakcom.network.LocationApiService
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*

class MapPickerActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: android.content.SharedPreferences
    private lateinit var pickLocationButton: Button
    private lateinit var loadingDialog: android.app.AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location)

        sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

        pickLocationButton = findViewById(R.id.pickFromMap)

        pickLocationButton.setOnClickListener {


        }
    }
    private fun sendPickedLocationToBackend(latitude: Double, longitude: Double, area: String, city: String, state: String, country: String) {
        showLoadingDialog()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://seekshak-backend.onrender.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(LocationApiService::class.java)

        val locationRequest = LocationRequest(
            latitude = latitude,
            longitude = longitude,
            area = area,
            city = city,
            state = state,
            country = country
        )

        val token = sharedPreferences.getString("token", null)


        if (token.isNullOrEmpty()) {
            dismissLoadingDialog()
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val authHeader = "Bearer $token"
                val response = service.updateLocation(locationRequest, authHeader)
                dismissLoadingDialog()
                if (response.isSuccessful && response.body() != null) {
                    Toast.makeText(this@MapPickerActivity, "Location Updated: ${response.body()!!.message}", Toast.LENGTH_SHORT).show()
                    finish() // Close and go back after success
                } else {
                    Toast.makeText(this@MapPickerActivity, "Failed to update location", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                dismissLoadingDialog()
                Toast.makeText(this@MapPickerActivity, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showLoadingDialog() {
        val builder = android.app.AlertDialog.Builder(this)
        builder.setCancelable(false)
        builder.setView(R.layout.dialog_loading)
        loadingDialog = builder.create()
        loadingDialog.show()
    }

    private fun dismissLoadingDialog() {
        if (::loadingDialog.isInitialized && loadingDialog.isShowing) {
            loadingDialog.dismiss()
        }
    }
}
