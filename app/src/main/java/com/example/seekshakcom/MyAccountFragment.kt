package com.example.seekshakcom

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import com.google.firebase.auth.FirebaseAuth
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.seekshakcom.databinding.FragmentMyaccountBinding
import com.example.seekshakcom.network.ApiClient
import com.example.seekshakcom.utils.SharedPrefManager
import kotlinx.coroutines.launch

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


    private fun setupListeners() {


        binding.icCamera.setOnClickListener {
            Toast.makeText(requireContext(), "Change profile picture", Toast.LENGTH_SHORT).show()
        }
    }
    private fun setupViews() {
        // Load from SharedPreferences first
        SharedPrefManager.getUser(requireContext())?.let { user ->
            binding.userId.text = user.userId.uppercase()
            binding.userName.text = user.name
                .split(" ")
                .joinToString(" ") { word ->
                    word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                }
            binding.mobileNo.text = user.phone
        }

        // Fetch fresh from server in background
        FirebaseAuth.getInstance().currentUser?.getIdToken(true)
            ?.addOnSuccessListener { result ->
                val idToken = result.token
                if (idToken != null) {
                    fetchUserFromBackend("Bearer $idToken")
                }
            }
            ?.addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to get auth token", Toast.LENGTH_SHORT).show()
            }
    }



    private fun fetchUserFromBackend(authToken: String) {
        lifecycleScope.launch {
            try {
                val response = ApiClient.instance.getUserProfile(authToken)
                if (response.isSuccessful) {
                    response.body()?.let { user ->
                        // Save to prefs
                        SharedPrefManager.saveUser(requireContext(), user)

                        // Update UI
                        binding.userId.text = user.userId.uppercase()
                        binding.userName.text = user.name
                            .split(" ")
                            .joinToString(" ") { word ->
                                word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                            }
                        binding.mobileNo.text = user.phone
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }







    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
