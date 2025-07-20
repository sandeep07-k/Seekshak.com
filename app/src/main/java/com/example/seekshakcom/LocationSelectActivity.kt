package com.example.seekshakcom

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.seekshakcom.adapter.RecentLocationAdapter
import com.example.seekshakcom.databinding.ActivityLocationSelectBinding
import com.example.seekshakcom.model.LocationRequest
import com.example.seekshakcom.utils.LocationHelper
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.*

class LocationSelectActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLocationSelectBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var shimmerLayout: ShimmerFrameLayout
    private lateinit var adapter: RecentLocationAdapter

    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private val fetchCooldownMinutes = 10

    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) autoFetchLocation()
            else Toast.makeText(this, "Permission required", Toast.LENGTH_SHORT).show()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLocationSelectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        shimmerLayout = binding.shimmerLayout

        setupRecyclerView()
        setupSwipeToDelete()

        binding.backIcon.setOnClickListener { finish() }

        binding.useCurrentLocation.setOnClickListener {
            fetchLocationAndReturn(true)
        }

        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            autoFetchLocation()
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun autoFetchLocation() {
        val prefs = getSharedPreferences("LocationPrefs", Context.MODE_PRIVATE)
        val lastFetchedMillis = prefs.getLong("last_fetched_time", 0)
        val now = System.currentTimeMillis()

        if ((now - lastFetchedMillis) < fetchCooldownMinutes * 60 * 1000) {
            val city = prefs.getString("selected_city", null)
            val area = prefs.getString("selected_area", null)
            val state = prefs.getString("selected_state", null)
            val sublocality = prefs.getString("selected_sublocality", null)

            val formatted = listOfNotNull(sublocality, area, city, state).joinToString(", ")
            binding.fetchingLocationText.text = formatted.ifEmpty { "Cached Location" }

            loadRecentLocations()
            return
        }

        fetchLocationAndReturn(false)
    }

    private fun fetchLocationAndReturn(shouldFinish: Boolean) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(this, "Location permission not granted", Toast.LENGTH_SHORT).show()
            return
        }

        binding.fetchingLocationText.text = "Fetching Location..."

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                handleLocation(location, shouldFinish)
            } else {
                // Fallback to current location if last known is null
                fusedLocationClient.getCurrentLocation(
                    com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
                    null
                ).addOnSuccessListener { freshLocation: Location? ->
                    if (freshLocation != null) {
                        handleLocation(freshLocation, shouldFinish)
                    } else {
                        binding.fetchingLocationText.text = "Unable to fetch location"
                    }
                }.addOnFailureListener {
                    binding.fetchingLocationText.text = "Failed to fetch location"
                }
            }
        }.addOnFailureListener {
            binding.fetchingLocationText.text = "Failed to fetch location"
        }
    }
    private fun handleLocation(location: Location, shouldFinish: Boolean) {
        coroutineScope.launch {
            val locationData = LocationHelper.getApproxLocation(
                applicationContext,
                location.latitude,
                location.longitude
            )

            if (locationData != null) {
                // ✅ This sends updated location to backend!
                LocationHelper.sendLocationToBackend(applicationContext, locationData)

                val locationRequest = LocationRequest(
                    latitude = locationData.lat,
                    longitude = locationData.lon,
                    sublocality = locationData.sublocality,
                    area = locationData.area,
                    city = locationData.city,
                    state = locationData.state,
                    country = locationData.country
                )

                val formatted = listOfNotNull(
                    locationRequest.sublocality,
                    locationRequest.area,
                    locationRequest.city,
                    locationRequest.state
                ).joinToString(", ")
                binding.fetchingLocationText.text = formatted

                saveToRecent(locationRequest)
                saveToPreferences(locationRequest)

                getSharedPreferences("LocationPrefs", Context.MODE_PRIVATE).edit()
                    .putLong("last_fetched_time", System.currentTimeMillis()).apply()

                if (shouldFinish) {
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
                } else {
                    loadRecentLocations()
                }
            } else {
                binding.fetchingLocationText.text = "Failed to get location details"
            }
        }
    }




    private fun saveToPreferences(location: LocationRequest) {
        val prefs = getSharedPreferences("LocationPrefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("selected_sublocality", location.sublocality)
            .putString("selected_area", location.area)
            .putString("selected_city", location.city)
            .putString("selected_state", location.state)
            .putString("selected_country", location.country)
            .putString("lat", location.latitude.toString())
            .putString("lon", location.longitude.toString())
            .apply()
    }

    private fun saveToRecent(location: LocationRequest) {
        val prefs = getSharedPreferences("RecentLocations", Context.MODE_PRIVATE)
        val list = getRecentLocationList().toMutableList()

        list.removeAll { it.latitude == location.latitude && it.longitude == location.longitude }
        list.add(0, location)
        if (list.size > 5) list.removeAt(list.size - 1)

        val json = list.joinToString("|||") {
            listOf(it.latitude, it.longitude, it.sublocality, it.area, it.city, it.state, it.country).joinToString("~~")
        }

        prefs.edit().putString("recent_location_list", json).apply()
    }

    private fun getRecentLocationList(): List<LocationRequest> {
        val prefs = getSharedPreferences("RecentLocations", Context.MODE_PRIVATE)
        val raw = prefs.getString("recent_location_list", null) ?: return emptyList()

        return raw.split("|||").mapNotNull {
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
    }

    private fun setupRecyclerView() {
        adapter = RecentLocationAdapter { locationRequest ->
            saveToPreferences(locationRequest)
            val intent = Intent()
            intent.putExtra("selected_sublocality", locationRequest.sublocality)
            intent.putExtra("selected_area", locationRequest.area)
            intent.putExtra("selected_city", locationRequest.city)
            intent.putExtra("selected_state", locationRequest.state)
            intent.putExtra("selected_country", locationRequest.country)
            intent.putExtra("lat", locationRequest.latitude.toString())
            intent.putExtra("lon", locationRequest.longitude.toString())
            setResult(Activity.RESULT_OK, intent)
            finish()
        }

        binding.recentLocationsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.recentLocationsRecyclerView.adapter = adapter

        shimmerLayout.visibility = View.VISIBLE
        shimmerLayout.startShimmer()

        Handler(Looper.getMainLooper()).postDelayed({
            loadRecentLocations()
        }, 1200)
    }

    private fun loadRecentLocations() {
        val locations = getRecentLocationList()
        shimmerLayout.stopShimmer()
        shimmerLayout.visibility = View.GONE
        adapter.setLocations(locations)
    }

    private fun setupSwipeToDelete() {
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, t: RecyclerView.ViewHolder) = false

            override fun onSwiped(holder: RecyclerView.ViewHolder, direction: Int) {
                val position = holder.bindingAdapterPosition
                adapter.removeLocation(position)
                saveAllRecentLocations(adapter.getLocations())
            }
        })
        itemTouchHelper.attachToRecyclerView(binding.recentLocationsRecyclerView)
    }

    private fun saveAllRecentLocations(locations: List<LocationRequest>) {
        val prefs = getSharedPreferences("RecentLocations", Context.MODE_PRIVATE)
        val json = locations.joinToString("|||") {
            listOf(it.latitude, it.longitude, it.sublocality, it.area, it.city, it.state, it.country).joinToString("~~")
        }
        prefs.edit().putString("recent_location_list", json).apply()
    }

    override fun onDestroy() {
        coroutineScope.cancel()
        super.onDestroy()
    }
}