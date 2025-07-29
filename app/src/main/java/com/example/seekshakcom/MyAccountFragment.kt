package com.example.seekshakcom

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.seekshakcom.databinding.FragmentMyaccountBinding
import com.example.seekshakcom.utils.ProfileManager
import com.google.firebase.auth.FirebaseAuth
import com.yalantis.ucrop.UCrop

class MyAccountFragment : Fragment() {

    private var _binding: FragmentMyaccountBinding? = null
    private val binding get() = _binding!!

    private lateinit var profileManager: ProfileManager

    private lateinit var editProfileLauncher: ActivityResultLauncher<Intent>

    private val cropImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val resultUri = UCrop.getOutput(result.data!!)
            resultUri?.let { profileManager.handleCropResult(it) }
        }
    }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        profileManager.handleGalleryImage(uri)
    }

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        profileManager.handleCameraImageResult(success)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyaccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        profileManager = ProfileManager(this, binding, cropImageLauncher)
        profileManager.initProfile(pickImageLauncher, takePictureLauncher)

        editProfileLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                profileManager.loadUserData()
            }
        }

        binding.editProfile.setOnClickListener {
            val intent = Intent(requireContext(), EditProfileActivity::class.java)
            editProfileLauncher.launch(intent)
        }

        view.findViewById<LinearLayout>(R.id.logout_layout).setOnClickListener {
            showLogoutDialog()
        }
    }

    override fun onResume() {
        super.onResume()
        profileManager.loadUserData()
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Log Out")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Yes") { _, _ -> logoutUser() }
            .setNegativeButton("No", null)
            .show()
    }

    private fun logoutUser() {
        FirebaseAuth.getInstance().signOut()

        requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE).edit().clear().apply()
        requireContext().getSharedPreferences("user_profile", Context.MODE_PRIVATE).edit().clear().apply()

        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}
