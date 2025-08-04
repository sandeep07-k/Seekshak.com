package com.example.seekshakcom

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.seekshakcom.adapter.RecentLocationAdapter
import com.example.seekshakcom.databinding.ActivityLocationSelectBinding
import com.example.seekshakcom.model.LocationRequest
import com.example.seekshakcom.utils.LocationHelper
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.*

class PostLocationSelectActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLocationSelectBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var shimmerLayout: ShimmerFrameLayout
    private lateinit var adapter: RecentLocationAdapter
    private lateinit var locationDialog: AlertDialog

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) fetchLocationAndReturn()
            else Toast.makeText(this, "Permission required", Toast.LENGTH_SHORT).show()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLocationSelectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = ContextCompat.getColor(this, R.color.soft_blue)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        shimmerLayout = binding.shimmerLayout

        setupRecyclerView()

        binding.backIcon.setOnClickListener { finish() }
        binding.useCurrentLocation.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                showLocationDialog() // Show the dialog before starting location fetch
                fetchLocationAndReturn()
            } else {
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }

    }

    @SuppressLint("MissingPermission")
    private fun fetchLocationAndReturn() {
        binding.fetchingLocationText.text = "Fetching Location..."


        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                handleLocation(it)
            } ?: run {

                fusedLocationClient.getCurrentLocation(
                    com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
                    null
                ).addOnSuccessListener { freshLocation: Location? ->
                    freshLocation?.let {
                        handleLocation(it)
                    } ?: showLocationError()
                }.addOnFailureListener {
                    showLocationError()
                }
            }
        }.addOnFailureListener {
            showLocationError()
        }
    }

    private fun handleLocation(location: Location) {
        coroutineScope.launch {
            val locationData = LocationHelper.getApproxLocation(
                applicationContext, location.latitude, location.longitude
            )

            if (locationData != null) {
                val intent = Intent().apply {
                    putExtra("selected_sublocality", locationData.sublocality)
                    putExtra("selected_area", locationData.area)
                    putExtra("selected_city", locationData.city)
                    putExtra("selected_state", locationData.state)
                    putExtra("selected_country", locationData.country)
                    putExtra("lat", locationData.lat.toString())
                    putExtra("lon", locationData.lon.toString())
                }
                setResult(Activity.RESULT_OK, intent)
                finish()
            } else {
                dismissLocationDialog()
                Log.e("PostLocationSelect", "Reverse geocode returned null")
                showLocationError()
            }
        }
    }

    private fun showLocationError() {
        binding.fetchingLocationText.text = "Failed to fetch location"
        Toast.makeText(this, "Unable to fetch location", Toast.LENGTH_SHORT).show()
    }

    private fun setupRecyclerView() {
        adapter = RecentLocationAdapter { locationRequest ->


            val intent = Intent().apply {
                putExtra("selected_sublocality", locationRequest.sublocality)
                putExtra("selected_area", locationRequest.area)
                putExtra("selected_city", locationRequest.city)
                putExtra("selected_state", locationRequest.state)
                putExtra("selected_country", locationRequest.country)
                putExtra("lat", locationRequest.latitude.toString())
                putExtra("lon", locationRequest.longitude.toString())
            }
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
        binding.recentLocationsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.recentLocationsRecyclerView.adapter = adapter
        loadRecentLocations()
    }

    private fun loadRecentLocations() {
        val prefs = getSharedPreferences("RecentLocations", Context.MODE_PRIVATE)
        val raw = prefs.getString("recent_location_list", null) ?: return
        val locations = raw.split("|||").mapNotNull {
            val parts = it.split("~~")
            if (parts.size != 7) return@mapNotNull null
            LocationRequest(
                latitude = parts[0].toDoubleOrNull() ?: return@mapNotNull null,
                longitude = parts[1].toDoubleOrNull() ?: return@mapNotNull null,
                sublocality = parts[2],
                area = parts[3],
                city = parts[4],
                state = parts[5],
                country = parts[6]
            )
        }
        shimmerLayout.stopShimmer()
        shimmerLayout.visibility = View.GONE
        adapter.setLocations(locations)
    }
    private fun showLocationDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_fetching_location, null)

        val builder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)

        locationDialog = builder.create()
        locationDialog.show()
    }

    private fun dismissLocationDialog() {
        if (::locationDialog.isInitialized && locationDialog.isShowing) {
            locationDialog.dismiss()
        }
    }

    override fun onDestroy() {
        dismissLocationDialog()
        coroutineScope.cancel()
        super.onDestroy()
    }
}
