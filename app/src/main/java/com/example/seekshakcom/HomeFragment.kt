package com.example.seekshakcom

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment


class HomeFragment : Fragment() {

    private lateinit var searchEditText: EditText
    private lateinit var notificationButton: ImageButton
    private lateinit var creditBalance: TextView
    private lateinit var creditCoin: ImageView
    private lateinit var locationText: TextView
    private lateinit var locationSelector: LinearLayout
    private lateinit var locationLauncher: ActivityResultLauncher<Intent>


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // ✅ Correct layout
        return inflater.inflate(R.layout.fragment_home, container, false)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        locationLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                val selectedCity = data?.getStringExtra("selected_city") ?: ""
                val selectedArea = data?.getStringExtra("selected_area") ?: ""

                // Update your locationText TextView
                view?.findViewById<TextView>(R.id.locationText)?.text = "$selectedArea, $selectedCity"
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Top Section
        searchEditText = view.findViewById(R.id.searchEditText)
        notificationButton = view.findViewById(R.id.notification_Btn)
        creditBalance = view.findViewById(R.id.credit_balance)
        creditCoin = view.findViewById(R.id.credit_coin)
        locationText = view.findViewById(R.id.locationText)
        locationSelector = view.findViewById(R.id.locationSelector)

        val locationSelector = view.findViewById<LinearLayout>(R.id.locationSelector)

        locationSelector.setOnClickListener {
            val intent = Intent(requireContext(), LocationActivity::class.java)
            locationLauncher.launch(intent)
        }

        // Notifications
        notificationButton.setOnClickListener {
            Toast.makeText(requireContext(), "Notifications", Toast.LENGTH_SHORT).show()
        }

        creditCoin.setOnClickListener {
            Toast.makeText(requireContext(), "Credits: ${creditBalance.text}", Toast.LENGTH_SHORT)
                .show()
        }
    }
}
