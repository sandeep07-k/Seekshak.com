package com.example.seekshakcom

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.seekshakcom.adapter.StateAdapter
import com.example.seekshakcom.model.LocationRequest
import com.example.seekshakcom.network.RetrofitInstance
import com.example.seekshakcom.utils.LocationHelper
import com.example.seekshakcom.utils.LocationLoader
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LocationSelectActivity : AppCompatActivity() {

    private lateinit var searchBar: EditText
    private lateinit var stateRecyclerView: RecyclerView
    private lateinit var recentLocationText: TextView
    private lateinit var fetchingLocationText: TextView
    private lateinit var recentLocationLayout: LinearLayout
    private lateinit var clearLocation: TextView
    private lateinit var backIcon: ImageView
    private lateinit var useCurrentLocation: LinearLayout

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var stateAdapter: StateAdapter
    private lateinit var prefs: SharedPreferences
    private var filteredStates = mutableListOf<String>()
    private lateinit var allStates: List<String>

    private val cityLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            setResult(RESULT_OK, result.data)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location_select)

        searchBar = findViewById(R.id.searchBar)
        stateRecyclerView = findViewById(R.id.stateRecyclerView)
        recentLocationText = findViewById(R.id.recentLocationText)
        fetchingLocationText = findViewById(R.id.fetchingLocationText)
        recentLocationLayout = findViewById(R.id.recentLocation)
        clearLocation = findViewById(R.id.clearLocation)
        backIcon = findViewById(R.id.backIcon)
        useCurrentLocation = findViewById(R.id.useCurrentLocation)

        prefs = getSharedPreferences("LocationPrefs", Context.MODE_PRIVATE)

        val locationData = LocationLoader.loadLocations(this)
        allStates = locationData.states.map { it.name }
        filteredStates.addAll(allStates)

        stateAdapter = StateAdapter(this, filteredStates) { selectedState ->
            val intent = Intent(this, CitySelectorActivity::class.java)
            intent.putExtra("SELECTED_STATE", selectedState)
            cityLauncher.launch(intent)
        }

        stateRecyclerView.layoutManager = LinearLayoutManager(this)
        stateRecyclerView.adapter = stateAdapter

        searchBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = Unit
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterStates(s.toString())
            }
        })

        backIcon.setOnClickListener { finish() }

        val savedCity = prefs.getString("selected_city", null)
        val savedArea = prefs.getString("selected_area", null)

        if (!savedCity.isNullOrEmpty() && !savedArea.isNullOrEmpty()) {
            recentLocationText.text = "$savedArea, $savedCity"
        }

        recentLocationLayout.setOnClickListener {
            if (!savedCity.isNullOrEmpty() && !savedArea.isNullOrEmpty()) {
                val result = Intent().apply {
                    putExtra("selected_city", savedCity)
                    putExtra("selected_area", savedArea)
                }
                setResult(RESULT_OK, result)
                finish()
            }
        }

        clearLocation.setOnClickListener {
            prefs.edit().clear().apply()
            recentLocationText.text = ""
            fetchingLocationText.text = "Fetching Location..."
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        useCurrentLocation.setOnClickListener {
            requestLocationPermission()
        }
    }

    private fun requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1001
            )
        } else {
            fetchAndSaveLocation()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            fetchAndSaveLocation()
        } else {
            Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    private fun filterStates(query: String) {
        filteredStates.clear()
        if (query.isEmpty()) {
            filteredStates.addAll(allStates)
        } else {
            filteredStates.addAll(allStates.filter { it.contains(query, ignoreCase = true) })
        }
        stateAdapter.notifyDataSetChanged()
    }

    private fun fetchAndSaveLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                lifecycleScope.launch {
                    val locData = LocationHelper.getApproxLocation(this@LocationSelectActivity, location.latitude, location.longitude)
                    if (locData != null) {
                        recentLocationText.text = "${locData.area}, ${locData.city}"
                        fetchingLocationText.text = "${locData.area}, ${locData.city}"
                        sendLocationToBackend(
                            locData.lat, locData.lon,
                            locData.area, locData.city,
                            locData.state, locData.country
                        )
                    } else {
                        Toast.makeText(this@LocationSelectActivity, "Location not found", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Unable to get current location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendLocationToBackend(
        lat: Double,
        lng: Double,
        area: String,
        city: String,
        state: String,
        country: String
    ) {
        val prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val token = prefs.getString("token", null)

        if (token.isNullOrEmpty()) {
            Log.e("LocationUpdate", "No token available to send location")
            return
        }


        val locationRequest = LocationRequest(
            latitude = lat,
            longitude = lng,
            area = area,
            city = city,
            state = state,
            country = country
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.locationApi.updateLocation(locationRequest, "Bearer $token")
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@LocationSelectActivity, "Location updated", Toast.LENGTH_SHORT).show()
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.e("LocationUpdate", "Error response: $errorBody")
                        Toast.makeText(this@LocationSelectActivity, "Server update failed", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@LocationSelectActivity, "Network error", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


}
