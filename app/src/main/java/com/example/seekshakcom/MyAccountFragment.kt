package com.example.seekshakcom.ui.myaccount

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import com.google.firebase.auth.FirebaseAuth
import com.example.seekshakcom.R
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.seekshakcom.LoginActivity
import com.example.seekshakcom.databinding.FragmentMyaccountBinding

class MyAccountFragment : Fragment() {

    // ViewBinding variable
    private var _binding: FragmentMyaccountBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyaccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val logoutLayout = view.findViewById<LinearLayout>(R.id.logout_layout)

        logoutLayout.setOnClickListener {
            showLogoutDialog()
        }

        setupViews()
        setupListeners()
    }
    private fun showLogoutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Log Out")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Yes") { dialog, _ ->
                logoutUser()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun logoutUser() {
        // Firebase sign out
        FirebaseAuth.getInstance().signOut()

        // Clear local session if needed (optional)
        val preferences = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        preferences.edit().clear().apply()

        // Redirect to LoginActivity
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun setupViews() {
        binding.userId.text = "Std-1234"
        binding.userName.text = "Sandeep Kumar"
        binding.mobileNo.text = "+91 9876543210"
        binding.userEmail.text = "sandeepkumar9334@gmail.com"
    }

    private fun setupListeners() {


        binding.icCamera.setOnClickListener {
            Toast.makeText(requireContext(), "Change profile picture", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
