package com.example.seekshakcom.utils

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.seekshakcom.R
import com.example.seekshakcom.databinding.FragmentMyaccountBinding
import com.example.seekshakcom.model.GenericResponse
import com.example.seekshakcom.model.ImageRemoveRequest
import com.example.seekshakcom.network.ApiClient
import com.example.seekshakcom.student.FullImageActivity
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

class ProfileManager(
    private val fragment: Fragment,
    private val binding: FragmentMyaccountBinding,
    private val cropImageLauncher: ActivityResultLauncher<Intent>
) {

    private var cameraImageUri: Uri? = null
    private var galleryLauncher: ActivityResultLauncher<String>? = null
    private var cameraLauncher: ActivityResultLauncher<Uri>? = null

    fun initProfile(
        pickImageLauncher: ActivityResultLauncher<String>,
        takePictureLauncher: ActivityResultLauncher<Uri>
    ) {
        // store for later use
        galleryLauncher = pickImageLauncher
        cameraLauncher = takePictureLauncher

        binding.icCamera.setOnClickListener {
            showImagePickerDialog()
        }

        binding.profileIcon.setOnClickListener {
            val imageUrl = SharedPrefManager.getUser(fragment.requireContext())?.profileImage
                ?: SharedPrefManager.getImageUrl(fragment.requireContext())
            if (!imageUrl.isNullOrEmpty()) {
                val intent = Intent(fragment.requireContext(), FullImageActivity::class.java)
                intent.putExtra("imageUrl", imageUrl)
                fragment.startActivity(intent)
            }
        }

        FirebaseAuth.getInstance().currentUser?.getIdToken(true)
            ?.addOnSuccessListener {
                val token = it.token
                if (!token.isNullOrEmpty()) {
                    fetchUserFromBackend("Bearer $token")
                }
            }

        loadUserData()
    }

    fun launchImageCropper(sourceUri: Uri) {
        val destinationUri = Uri.fromFile(
            File(fragment.requireContext().cacheDir, "cropped_${System.currentTimeMillis()}.jpg")
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
            .getIntent(fragment.requireContext())

        cropImageLauncher.launch(intent)
    }

    fun handleCropResult(uri: Uri) {
        Glide.with(fragment).load(uri).circleCrop().into(binding.profileIcon)
        saveProfileImageUri(uri)
        uploadImageToServer(uri)
    }

    fun getCameraImageUri(): Uri {
        val photoFile = File(
            fragment.requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            "profile_${System.currentTimeMillis()}.jpg"
        )
        cameraImageUri = androidx.core.content.FileProvider.getUriForFile(
            fragment.requireContext(),
            "${fragment.requireContext().packageName}.fileprovider",
            photoFile
        )
        return cameraImageUri!!
    }

    fun handleCameraImageResult(success: Boolean) {
        if (success && cameraImageUri != null) {
            launchImageCropper(cameraImageUri!!)
        }
    }

    fun handleGalleryImage(uri: Uri?) {
        uri?.let { launchImageCropper(it) }
    }

    private fun showImagePickerDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery", "Remove Photo")
        AlertDialog.Builder(fragment.requireContext())
            .setTitle("Change Profile Picture")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> cameraLauncher?.launch(getCameraImageUri())
                    1 -> galleryLauncher?.launch("image/*")
                    2 -> removeProfileImage()
                }
            }
            .show()
    }

    private fun uploadImageToServer(uri: Uri) {
        val context = fragment.requireContext()
        val file = uriToFile(uri) ?: return

        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("profileImage", file.name, requestFile)

        val firebaseUid = FirebaseAuth.getInstance().currentUser?.uid
        val uidRequestBody = firebaseUid?.toRequestBody("text/plain".toMediaTypeOrNull())
        if (uidRequestBody == null) {
            Toast.makeText(context, "Firebase UID is null", Toast.LENGTH_SHORT).show()
            return
        }

        fragment.lifecycleScope.launch {
            try {
                val response = ApiClient.instance.uploadProfileImage(body, uidRequestBody)
                if (response.isSuccessful) {
                    val imageUrl = response.body()?.imageUrl
                    if (!imageUrl.isNullOrEmpty()) {
                        SharedPrefManager.saveImageUrl(context, imageUrl)
                        SharedPrefManager.getUser(context)?.let { user ->
                            SharedPrefManager.saveUser(context, user.copy(profileImage = imageUrl))
                        }

                        Glide.with(context).load(imageUrl)
                            .placeholder(R.drawable.ic_user)
                            .circleCrop()
                            .into(binding.profileIcon)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun fetchUserFromBackend(authToken: String) {
        fragment.lifecycleScope.launch {
            try {
                val response = ApiClient.instance.getUserProfile(authToken)
                if (response.isSuccessful) {
                    response.body()?.let { user ->
                        SharedPrefManager.saveImageUrl(fragment.requireContext(), user.profileImage)
                        SharedPrefManager.saveUser(fragment.requireContext(), user.copy(profileImage = user.profileImage))
                        loadUserData()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun removeProfileImage() {
        val context = fragment.requireContext()
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val request = ImageRemoveRequest(uid)

        ApiClient.instance.removeImage(request).enqueue(object : Callback<GenericResponse> {
            override fun onResponse(call: Call<GenericResponse>, response: Response<GenericResponse>) {
                if (response.isSuccessful) {
                    SharedPrefManager.saveImageUrl(context, null)
                    saveProfileImageUri(null)
                    Glide.with(context).clear(binding.profileIcon)
                    binding.profileIcon.setImageResource(R.drawable.ic_user)
                    Toast.makeText(context, "Profile picture removed", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                t.printStackTrace()
            }
        })
    }

    fun loadUserData() {
        val user = SharedPrefManager.getUser(fragment.requireContext())
        binding.userId.text = user?.userId?.uppercase() ?: ""
        binding.userName.text = user?.name?.split(" ")?.joinToString(" ") { it.replaceFirstChar { c -> c.uppercaseChar() } } ?: ""
        binding.mobileNo.text = user?.phone?.let {
            if (it.startsWith("+91")) "+91 ${it.removePrefix("+91").trim()}" else it
        } ?: ""

        binding.userEmail.text = user?.email ?: ""
        if(binding.userEmail.text.isNullOrEmpty()){ binding.userEmail.text = "Update your email !!"}

        val imageUrl = user?.profileImage
        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(fragment.requireContext()).load(imageUrl)
                .circleCrop()
                .placeholder(R.drawable.ic_user)
                .into(binding.profileIcon)
        } else {
            binding.profileIcon.setImageResource(R.drawable.ic_user)
        }
    }

    private fun saveProfileImageUri(uri: Uri?) {
        val context = fragment.requireContext()
        context.getSharedPreferences("user_profile", Context.MODE_PRIVATE).edit().apply {
            if (uri != null) {
                putString("profile_image_uri", uri.toString())
            } else {
                remove("profile_image_uri")
            }
        }.apply()
    }

    private fun uriToFile(uri: Uri): File? {
        return try {
            val inputStream = fragment.requireContext().contentResolver.openInputStream(uri) ?: return null
            val tempFile = File.createTempFile("upload_", ".jpg", fragment.requireContext().cacheDir)
            tempFile.outputStream().use { outputStream -> inputStream.copyTo(outputStream) }
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    companion object {
        fun fetchAndLoadUserDataForEditProfile(
            context: Context,
            userIdText: TextView,
            nameField: EditText,
            phoneField: EditText,
            emailField: EditText,
            profileImage: ImageView,
            onLoaded: (() -> Unit)? = null
        ) {
            val user = SharedPrefManager.getUser(context)
            if (user != null) {
                userIdText.text = user.userId.uppercase()

                val capitalizedName = user.name.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase() else it.toString()
                }
                nameField.setText(capitalizedName)

                phoneField.setText(user.phone.removePrefix("+91"))
                emailField.setText(user.email ?: "")
                if (!user.profileImage.isNullOrBlank()) {
                    Glide.with(context)
                        .load(user.profileImage)
                        .placeholder(R.drawable.ic_user)
                        .circleCrop()
                        .into(profileImage)
                }
                onLoaded?.invoke()
            }
        }
    }
}
