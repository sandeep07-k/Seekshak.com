package com.example.seekshakcom

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.seekshakcom.adapter.LocalityAdapter
import com.example.seekshakcom.utils.LocationLoader

class LocalitySelectorActivity : AppCompatActivity() {

    private lateinit var backIcon: ImageView
    private lateinit var titleText: TextView
    private lateinit var allInCityText: TextView
    private lateinit var searchBar: EditText
    private lateinit var cityRecyclerView: RecyclerView

    private var selectedState: String = ""
    private var selectedCity: String = ""

    private var allLocalities = listOf<String>()
    private val filteredLocalities = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_locality_selector)

        backIcon = findViewById(R.id.backIcon)
        titleText = findViewById(R.id.titleText)
        allInCityText = findViewById(R.id.all_in_city)
        searchBar = findViewById(R.id.searchBar)
        cityRecyclerView = findViewById(R.id.cityRecyclerView)

        selectedState = intent.getStringExtra("SELECTED_STATE") ?: ""
        selectedCity = intent.getStringExtra("SELECTED_CITY") ?: ""

        titleText.text = selectedCity
        allInCityText.text = "All in $selectedCity"

        val locationData = LocationLoader.loadLocations(this)
        allLocalities = locationData.states
            .find { it.name == selectedState }
            ?.cities?.find { it.name == selectedCity }
            ?.localities ?: emptyList()

        filteredLocalities.addAll(allLocalities)

        val adapter = LocalityAdapter(filteredLocalities) { selectedArea ->
            getSharedPreferences("LocationPrefs", Context.MODE_PRIVATE)
                .edit()
                .putString("selected_city", selectedCity)
                .putString("selected_area", selectedArea)
                .apply()

            val result = Intent().apply {
                putExtra("selected_city", selectedCity)
                putExtra("selected_area", selectedArea)
            }
            setResult(RESULT_OK, result)
            finish()
        }

        cityRecyclerView.layoutManager = LinearLayoutManager(this)
        cityRecyclerView.adapter = adapter

        backIcon.setOnClickListener { finish() }

        searchBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = Unit
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterLocalities(s.toString())
                adapter.notifyDataSetChanged()
            }
        })
    }

    private fun filterLocalities(query: String) {
        filteredLocalities.clear()
        if (query.isEmpty()) filteredLocalities.addAll(allLocalities)
        else filteredLocalities.addAll(allLocalities.filter { it.contains(query, ignoreCase = true) })
    }
}
