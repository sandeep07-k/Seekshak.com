package com.example.seekshakcom

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.seekshakcom.adapter.CityAdapter
import com.example.seekshakcom.utils.LocationLoader


class CitySelectorActivity : AppCompatActivity() {

    private lateinit var backIcon: ImageView
    private lateinit var titleText: TextView
    private lateinit var allInStateText: TextView
    private lateinit var searchBar: EditText
    private lateinit var cityRecyclerView: RecyclerView

    private lateinit var cityAdapter: CityAdapter
    private lateinit var localityLauncher: ActivityResultLauncher<Intent>

    private var selectedState: String? = null
    private var cityList: List<String> = emptyList()
    private var filteredCityList = mutableListOf<String>()




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_city_selector)

        backIcon = findViewById(R.id.backIcon)
        titleText = findViewById(R.id.titleText)
        allInStateText = findViewById(R.id.all_in_state)
        searchBar = findViewById(R.id.searchBar)
        cityRecyclerView = findViewById(R.id.cityRecyclerView)

        selectedState = intent.getStringExtra("SELECTED_STATE")
        if (selectedState.isNullOrEmpty()) {
            finish() // Exit gracefully if data is not passed
            return
        }

        titleText.text = selectedState
        allInStateText.text = "All in $selectedState"




        val locationData = LocationLoader.loadLocations(this)
        cityList = locationData.states.find { it.name == selectedState }?.cities?.map { it.name } ?: emptyList()
        filteredCityList = cityList.toMutableList()

        localityLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                setResult(RESULT_OK, result.data)
                finish()
            }
        }

        cityAdapter = CityAdapter(filteredCityList) { city ->
            val intent = Intent(this, LocalitySelectorActivity::class.java)
            intent.putExtra("SELECTED_STATE", selectedState)
            intent.putExtra("SELECTED_CITY", city)
            localityLauncher.launch(intent)
        }

        cityRecyclerView.layoutManager = LinearLayoutManager(this)
        cityRecyclerView.adapter = cityAdapter

        searchBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = Unit
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterCities(s.toString())
            }
        })

        backIcon.setOnClickListener { finish() }
    }

    private fun filterCities(query: String) {
        filteredCityList.clear()
        if (query.isEmpty()) filteredCityList.addAll(cityList)
        else filteredCityList.addAll(cityList.filter { it.contains(query, ignoreCase = true) })
        cityAdapter.notifyDataSetChanged()
    }
}
