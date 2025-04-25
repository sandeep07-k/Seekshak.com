package com.example.seekshakcom

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment


class HomeFragment : Fragment() {

    private lateinit var searchEditText: EditText
    private lateinit var notificationButton: ImageButton
    private lateinit var creditBalance: TextView
    private lateinit var creditCoin: ImageView



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // ✅ Correct layout
        return inflater.inflate(R.layout.fragment_home, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Top Section
        searchEditText = view.findViewById(R.id.searchEditText)
        notificationButton = view.findViewById(R.id.notification_Btn)
        creditBalance = view.findViewById(R.id.credit_balance)
        creditCoin = view.findViewById(R.id.credit_coin)

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
