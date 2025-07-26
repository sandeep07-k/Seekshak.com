package com.example.seekshakcom

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import com.google.firebase.auth.FirebaseAuth
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.seekshakcom.databinding.FragmentMyaccountBinding
import com.example.seekshakcom.network.ApiClient
import com.example.seekshakcom.utils.SharedPrefManager
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.launch
import java.io.File

class MyAccountFragment : Fragment() {

    // ViewBinding variable
    private var _binding: FragmentMyaccountBinding? = null
    private val binding get() = _binding!!

    private val cropImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val resultUri = UCrop.getOutput(result.data!!)
            resultUri?.let { setProfileImage(it) }
        } else if (result.resultCode == UCrop.RESULT_ERROR) {
            val cropError = UCrop.getError(result.data!!)
            cropError?.printStackTrace()
            Toast.makeText(requireContext(), "Crop failed: ${cropError?.message}", Toast.LENGTH_SHORT).show()
        }
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
            showImagePickerDialog()
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
        getSavedProfileImageUri(requireContext())?.let { uri ->
            Glide.with(this)
                .load(uri)
                .circleCrop() // <== IMPORTANT: Keep this to enforce circular display
                .placeholder(R.drawable.ic_user)
                .into(binding.profileIcon)
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
//                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private var cameraImageUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            launchImageCropper(it)
        }

    }
    private fun launchImageCropper(sourceUri: Uri) {
        val destinationUri = Uri.fromFile(
            File(requireContext().cacheDir, "cropped_${System.currentTimeMillis()}.jpg")
        )

        val options = UCrop.Options().apply {
            setCompressionQuality(90)
            setCircleDimmedLayer(true)
            setShowCropFrame(false)
            setShowCropGrid(false)
            setToolbarTitle("Crop Image")
        }

        val intent = UCrop.of(sourceUri, destinationUri)
            .withAspectRatio(1f, 1f)
            .withMaxResultSize(512, 512)
            .withOptions(options)
            .getIntent(requireContext())

        cropImageLauncher.launch(intent)
    }



    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
        if (success && cameraImageUri != null) {
            launchImageCropper(cameraImageUri!!)
        }
    }

    private fun setProfileImage(uri: Uri) {
        Glide.with(this)
            .load(uri)
            .circleCrop() // This crops the image into a circle
            .placeholder(R.drawable.ic_user)
            .into(binding.profileIcon)

        saveProfileImageUri(requireContext(), uri)
    }


    private fun pickImageFromGallery() {
        pickImageLauncher.launch("image/*")
    }

    private fun takePhotoFromCamera() {
        val photoFile = File(requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "profile_${System.currentTimeMillis()}.jpg")
        cameraImageUri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.fileprovider", photoFile)
        takePictureLauncher.launch(cameraImageUri)
    }
    private fun removeProfileImage() {
        saveProfileImageUri(requireContext(), null) // remove saved URI

        Glide.with(this)
            .load(R.drawable.ic_user) // fallback image
            .circleCrop()
            .into(binding.profileIcon)

        Toast.makeText(requireContext(), "Profile picture removed", Toast.LENGTH_SHORT).show()
    }


    private fun  showImagePickerDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery", "Remove Photo")

        AlertDialog.Builder(requireContext())
            .setTitle("Change Profile Picture")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> takePhotoFromCamera()
                    1 -> pickImageFromGallery()
                    2 -> removeProfileImage()
                }
            }
            .show()
    }
    private fun saveProfileImageUri(context: Context, uri: Uri?) {
        context.getSharedPreferences("user_profile", Context.MODE_PRIVATE).edit().apply {
            if (uri != null) {
                putString("profile_image_uri", uri.toString())
            } else {
                remove("profile_image_uri")
            }
        }.apply()
    }


    private fun getSavedProfileImageUri(context: Context): Uri? {
        val uriStr = context.getSharedPreferences("user_profile", Context.MODE_PRIVATE)
            .getString("profile_image_uri", null)
        return uriStr?.let { Uri.parse(it) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
