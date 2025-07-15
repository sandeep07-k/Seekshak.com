package com.example.seekshakcom

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.seekshakcom.model.LocationRequest
import com.example.seekshakcom.network.LocationApiService
import com.example.seekshakcom.network.RetrofitHelper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.mappls.sdk.services.account.MapplsAccountManager
import kotlinx.coroutines.launch
import java.util.*

class LocationActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var currentLocationButton: TextView
    private lateinit var pickFromMapButton: Button
    private lateinit var loadingDialog: android.app.AlertDialog
    private lateinit var sharedPreferences: android.content.SharedPreferences

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            fetchCurrentLocation()
        } else {
            Toast.makeText(this, "Location permission denied.", Toast.LENGTH_SHORT).show()
        }
    }

    private val mapPickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            // TODO: Handle returned data from MapPickerActivity if needed
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location)

        // Initialize Mappls SDK
        MapplsAccountManager.getInstance().apply {
            restAPIKey = BuildConfig.MAPPLS_REST_API_KEY
            mapSDKKey = BuildConfig.MAPPLS_MAP_SDK_KEY
            atlasClientId = BuildConfig.MAPPLS_CLIENT_ID
            atlasClientSecret = BuildConfig.MAPPLS_CLIENT_SECRET
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

        currentLocationButton = findViewById(R.id.currentLocation)
        pickFromMapButton = findViewById(R.id.pickFromMap)

        currentLocationButton.setOnClickListener {
            checkLocationPermissionAndFetch()
        }

        pickFromMapButton.setOnClickListener {
            val intent = Intent(this, MapPickerActivity::class.java)
            mapPickerLauncher.launch(intent)
        }
    }

    private fun checkLocationPermissionAndFetch() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED -> {
                fetchCurrentLocation()
            }
            else -> {
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    private fun fetchCurrentLocation() {
        showLoadingDialog()

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            dismissLoadingDialog()

            if (location != null) {
                val geocoder = Geocoder(this, Locale.getDefault())
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)

                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    val area = address.subLocality.orEmpty()
                    val city = address.locality.orEmpty()
                    val state = address.adminArea.orEmpty()
                    val country = address.countryName.orEmpty()

                    val fullLocation = "$area, $city, $state, $country".trimStart { it == ',' }

                    // Send selected city and area back to HomeFragment
                    val resultIntent = Intent().apply {
                        putExtra("selected_city", city)
                        putExtra("selected_area", area)
                    }

                    setResult(Activity.RESULT_OK, resultIntent)
                    finish()

                    Toast.makeText(this, "Current Location: $fullLocation", Toast.LENGTH_LONG).show()

                    sendLocationToBackend(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        area = area,
                        city = city,
                        state = state,
                        country = country
                    )
                } else {
                    Toast.makeText(this, "Unable to fetch address.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Location not available", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            dismissLoadingDialog()
            Toast.makeText(this, "Failed to fetch location: ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendLocationToBackend(latitude: Double, longitude: Double, area: String, city: String, state: String, country: String) {
        val service = RetrofitHelper.createService(LocationApiService::class.java)
        val locationRequest = LocationRequest(latitude, longitude, area, city, state, country)
        val token = sharedPreferences.getString("token", null)

        if (token.isNullOrEmpty()) {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val response = service.updateLocation(locationRequest, "Bearer $token")
                if (response.isSuccessful && response.body() != null) {
                    Toast.makeText(this@LocationActivity, "Location Updated: ${response.body()!!.message}", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@LocationActivity, "Failed to update location", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@LocationActivity, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showLoadingDialog() {
        if (!::loadingDialog.isInitialized) {
            loadingDialog = android.app.AlertDialog.Builder(this)
                .setView(R.layout.dialog_loading)
                .setCancelable(false)
                .create()
        }
        loadingDialog.show()
    }

    private fun dismissLoadingDialog() {
        if (::loadingDialog.isInitialized && loadingDialog.isShowing) {
            loadingDialog.dismiss()
        }
    }

    companion object {
        const val MAP_PICKER_REQUEST_CODE = 1001
    }
}
