package com.example.seekshakcom

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.signature.ObjectKey
import com.example.seekshakcom.databinding.FragmentMyaccountBinding
import com.example.seekshakcom.model.GenericResponse
import com.example.seekshakcom.model.ImageRemoveRequest
import com.example.seekshakcom.network.ApiClient
import com.example.seekshakcom.utils.SharedPrefManager
import com.google.firebase.auth.FirebaseAuth
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class MyAccountFragment : Fragment() {

    private var _binding: FragmentMyaccountBinding? = null
    private val binding get() = _binding!!
    private lateinit var cameraLauncher: ActivityResultLauncher<Uri>
    private var cameraImageUri: Uri? = null
    private lateinit var imagePickerLauncher: ActivityResultLauncher<String>


    private val cropImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
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
        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                uploadImageToServer(it)
            }
        }
        cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success && cameraImageUri != null) {
                uploadImageToServer(cameraImageUri!!)
            }
        }


        view.findViewById<LinearLayout>(R.id.logout_layout).setOnClickListener {
            showLogoutDialog()
        }

        setupViews()
        setupListeners()

        loadProfileImageFromPrefs()
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
        startActivity(Intent(requireContext(), LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
    }

    private fun setupListeners() {
        binding.icCamera.setOnClickListener {
            showImagePickerDialog()
        }

        binding.profileIcon.setOnClickListener {
            val imageUrl = SharedPrefManager.getUser(requireContext())?.profileImage
                ?: SharedPrefManager.getImageUrl(requireContext())

            if (!imageUrl.isNullOrEmpty()) {
                val intent = Intent(requireContext(), FullImageActivity::class.java)
                intent.putExtra("imageUrl", imageUrl)
                startActivity(intent)
            }
        }

    }

    private fun setupViews() {
        val user = SharedPrefManager.getUser(requireContext())
        binding.userId.text = user?.userId?.uppercase() ?: ""
        binding.userName.text = user?.name
            ?.split(" ")
            ?.joinToString(" ") { it.replaceFirstChar { c -> c.uppercaseChar() } } ?: ""
        binding.mobileNo.text = user?.phone ?: ""

        val imageUrl = user?.profileImage ?: SharedPrefManager.getImageUrl(requireContext())

        Glide.with(requireContext())
            .load(imageUrl)
            .signature(ObjectKey(imageUrl ?: "default_key")) // 👈 add this to force refresh if URL changes
            .placeholder(R.drawable.ic_user)
            .error(R.drawable.ic_user)
            .circleCrop()
            .into(binding.profileIcon)


        // 🔄 Always fetch fresh user data in background
        FirebaseAuth.getInstance().currentUser?.getIdToken(true)
            ?.addOnSuccessListener { result ->
                val idToken = result.token
                if (idToken != null) {
                    fetchUserFromBackend("Bearer $idToken")
                }
            }
    }


    private fun fetchUserFromBackend(authToken: String) {
        lifecycleScope.launch {
            try {
                val response = ApiClient.instance.getUserProfile(authToken)
                if (response.isSuccessful) {
                    response.body()?.let { user ->
//                        SharedPrefManager.saveUser(requireContext(), user)
                        SharedPrefManager.saveImageUrl(requireContext(), user.profileImage) // Save the image URL separately
                        SharedPrefManager.saveUser(requireContext(), user.copy(profileImage = user.profileImage))


                        binding.userId.text = user.userId.uppercase()
                        binding.userName.text = user.name
                            .split(" ")
                            .joinToString(" ") { word ->
                                word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                            }
                        binding.mobileNo.text = user.phone

                        // 🔄 Refresh profile image only if changed
                        refreshProfileImageInBackground(user.profileImage)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun refreshProfileImageInBackground(newUrl: String?) {
        val savedUrl = SharedPrefManager.getImageUrl(requireContext())

        if (!newUrl.isNullOrEmpty() && newUrl != savedUrl) {
            SharedPrefManager.saveImageUrl(requireContext(), newUrl)
            Glide.with(requireContext())
                .load(newUrl)
                .circleCrop()
                .placeholder(R.drawable.ic_user)
                .error(R.drawable.ic_user)
                .into(binding.profileIcon)
        }
    }





    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { launchImageCropper(it) }
    }

    private fun launchImageCropper(sourceUri: Uri) {
        val destinationUri = Uri.fromFile(File(requireContext().cacheDir, "cropped_${System.currentTimeMillis()}.jpg"))
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

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && cameraImageUri != null) {
            launchImageCropper(cameraImageUri!!)
        }
    }

    private fun setProfileImage(uri: Uri) {
        Glide.with(this).load(uri).circleCrop().into(binding.profileIcon)
        saveProfileImageUri(requireContext(), uri)
        uploadImageToServer(uri)
    }

    private fun pickImageFromGallery() {
        pickImageLauncher.launch("image/*")
    }

    private fun takePhotoFromCamera() {
        val photoFile = File(requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "profile_${System.currentTimeMillis()}.jpg")
        cameraImageUri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.fileprovider", photoFile)
        takePictureLauncher.launch(cameraImageUri)
    }

    private fun showImagePickerDialog() {
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
    private fun uploadImageToServer(uri: Uri) {
        val context = requireContext()
        val file = uriToFile(uri) ?: return
        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("profileImage", file.name, requestFile)

        val firebaseUid = FirebaseAuth.getInstance().currentUser?.uid
        val uidRequestBody = firebaseUid?.toRequestBody("text/plain".toMediaTypeOrNull())
        if (uidRequestBody == null) {
            Toast.makeText(context, "Firebase UID is null", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val response = ApiClient.instance.uploadProfileImage(body, uidRequestBody)
                if (response.isSuccessful) {
                    val imageUrl = response.body()?.imageUrl
                    if (!imageUrl.isNullOrEmpty()) {
                        SharedPrefManager.saveImageUrl(context, imageUrl)

                        // Also update full user object if exists
                        SharedPrefManager.getUser(context)?.let { oldUser ->
                            val updatedUser = oldUser.copy(profileImage = imageUrl)
                            SharedPrefManager.saveUser(context, updatedUser)
                        }

                        Glide.with(context)
                            .load(imageUrl)
                            .placeholder(R.drawable.ic_user)
                            .error(R.drawable.ic_user)
                            .circleCrop()
                            .into(binding.profileIcon)
                    } else {
                        Toast.makeText(context, "Image URL is empty", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Upload failed", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Error uploading", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun removeProfileImage() {
        val firebaseUid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val request = ImageRemoveRequest(firebaseUid)

        ApiClient.instance.removeImage(request).enqueue(object : Callback<GenericResponse> {
            override fun onResponse(
                call: Call<GenericResponse>,
                response: Response<GenericResponse>
            ) {
                if (response.isSuccessful) {
                    // Clear locally saved URL
                    SharedPrefManager.saveImageUrl(requireContext(), null)
                    saveProfileImageUri(requireContext(), null)

                    // Clear Glide memory + disk cache (force refresh)
                    Glide.with(requireContext()).clear(binding.profileIcon)

                    // Set fallback placeholder
                    binding.profileIcon.setImageResource(R.drawable.ic_user)

                    Toast.makeText(requireContext(), "Profile picture removed", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Failed to remove image", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                t.printStackTrace()
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
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

    private fun uriToFile(uri: Uri): File? {
        val inputStream = requireContext().contentResolver.openInputStream(uri) ?: return null
        val tempFile = File.createTempFile("upload_", ".jpg", requireContext().cacheDir)
        tempFile.outputStream().use { outputStream ->
            inputStream.copyTo(outputStream)
        }
        return tempFile
    }
    private fun loadProfileImageFromPrefs() {
        val context = requireContext()
        val imageUrl = SharedPrefManager.getImageUrl(context)

        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.ic_user)
                .error(R.drawable.ic_user)
                .circleCrop()
                .signature(ObjectKey(imageUrl)) // 🔁 Important: Refresh when URL changes
                .into(binding.profileIcon)
        } else {
            binding.profileIcon.setImageResource(R.drawable.ic_user)
        }
    }




    override fun onResume() {
        super.onResume()
        loadProfileImageFromPrefs()
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
