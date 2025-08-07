package com.example.seekshakcom

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.seekshakcom.utils.LocationHelper
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private lateinit var searchEditText: EditText
    private lateinit var notificationButton: ImageButton
    private lateinit var creditBalance: TextView
    private lateinit var creditCoin: ImageView
    private lateinit var locationText: TextView
    private lateinit var locationSelector: LinearLayout
    private lateinit var locationLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        locationLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                val selectedCity = data?.getStringExtra("selected_city") ?: ""
                val selectedArea = data?.getStringExtra("selected_area") ?: ""
                val selectedSublocality = data?.getStringExtra("selected_sublocality") ?: ""

                // 🆕 Prefer showing sublocality if available
                locationText.text = if (selectedSublocality.isNotEmpty())
                    "$selectedArea, $selectedCity"
                else
                    "$selectedArea, $selectedCity"
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchEditText = view.findViewById(R.id.searchEditText)
        notificationButton = view.findViewById(R.id.notification_Btn)
        creditBalance = view.findViewById(R.id.credit_balance)
        creditCoin = view.findViewById(R.id.credit_coin)
        locationText = view.findViewById(R.id.locationText)
        locationSelector = view.findViewById(R.id.locationSelector)

        locationSelector.setOnClickListener {
            val token = requireContext()
                .getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                .getString("token", null)

            val intent = Intent(requireContext(), LocationSelectActivity::class.java)
            intent.putExtra("token", token)
            locationLauncher.launch(intent)
        }

        notificationButton.setOnClickListener {
            Toast.makeText(requireContext(), "Notifications", Toast.LENGTH_SHORT).show()
        }

        creditCoin.setOnClickListener {
            Toast.makeText(
                requireContext(),
                "Credits: ${creditBalance.text}",
                Toast.LENGTH_SHORT
            ).show()
        }

        // ✅ Load cached location
        loadCachedLocation()
    }

    private fun loadCachedLocation() {
        val prefs = requireContext().getSharedPreferences("LocationPrefs", Context.MODE_PRIVATE)

        val city = prefs.getString("selected_city", null)
        val area = prefs.getString("selected_area", null)
        val sublocality = prefs.getString("selected_sublocality", null)
        val lat = prefs.getString("lat", null)?.toDoubleOrNull()
        val lon = prefs.getString("lon", null)?.toDoubleOrNull()
        val state = prefs.getString("selected_state", null)
        val country = prefs.getString("selected_country", null)

        if (!city.isNullOrEmpty() && !area.isNullOrEmpty() && lat != null && lon != null) {
            // 🆕 Prefer showing sublocality if available
            locationText.text = if (!sublocality.isNullOrEmpty())
                "$area, $city"
            else
                "$area, $city"

            val location = LocationHelper.LocationData(
                lat, lon,
                sublocality ?: "",
                area,
                city,
                state ?: "Unknown State",
                country ?: "India",

            )

            lifecycleScope.launch {
                LocationHelper.sendLocationToBackend(requireContext(), location)
            }
        }
    }
}
