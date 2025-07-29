
package com.example.seekshakcom

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.seekshakcom.databinding.ActivityEditProfileBinding
import com.example.seekshakcom.model.UpdateProfileResponse
import com.example.seekshakcom.network.ApiClient
import com.example.seekshakcom.network.ApiService
import com.example.seekshakcom.utils.ProfileManager
import com.example.seekshakcom.utils.SharedPrefManager
import com.google.firebase.auth.FirebaseAuth
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var apiService: ApiService
    private lateinit var phoneVerificationLauncher: ActivityResultLauncher<Intent>

    private var originalName: String = ""
    private var originalPhone: String = ""
    private var originalEmail: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        apiService = ApiClient.instance

        // ✅ Load user data from ProfileManager (with callback)
        ProfileManager.fetchAndLoadUserDataForEditProfile(
            context = this,
            userIdText = binding.userIdCombined,
            nameField = binding.userName,
            phoneField = binding.mobileNo,
            emailField = binding.emailId,
            profileImage = binding.profileIcon
        ) {
            // Callback runs after UI is populated
            originalName = binding.userName.text?.toString() ?: ""
            originalPhone = binding.mobileNo.text?.toString() ?: ""
            originalEmail = binding.emailId.text?.toString() ?: ""
            binding.charCount.text = "${originalName.length}/30"
        }

        binding.userName.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.charCount.text = "${s?.length ?: 0}/30"
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        phoneVerificationLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val newNumber = result.data?.getStringExtra("verifiedPhoneNumber")
                if (!newNumber.isNullOrBlank()) {
                    val displayNumber = if (newNumber.startsWith("+91")) newNumber.removePrefix("+91") else newNumber
                    binding.mobileNo.setText(displayNumber)
                    binding.mobileVerified.text = "Yay! Your number is verified."
//                    SharedPrefManager.getUser(this)?.let {
//                        val updatedUser = it.copy(phone = newNumber)
//                        SharedPrefManager.saveUser(this, updatedUser)
//                    }
                }
            }
        }

        binding.mobileNo.setOnClickListener {
            val intent = Intent(this, PhoneVerificationActivity::class.java)
            intent.putExtra("existingNumber", binding.mobileNo.text.toString())
            phoneVerificationLauncher.launch(intent)
        }

        binding.saveBtn.setOnClickListener {
            val name = binding.userName.text.toString().trim()
            val email = binding.emailId.text.toString().trim()
            val phone = binding.mobileNo.text.toString().trim()

            if (!hasUnsavedChanges()) {
                Toast.makeText(this, "No changes to update", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            updateUserProfile(name, phone, email)
        }

        binding.icClose.setOnClickListener {
            showDiscardChangesDialogOrFinish()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showDiscardChangesDialogOrFinish()
            }
        })
    }

    private fun updateUserProfile(name: String, phone: String, email: String) {
        val firebaseUid = FirebaseAuth.getInstance().currentUser?.uid
        if (firebaseUid == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val updateMap = mutableMapOf<String, String>()
        if (name != originalName) updateMap["name"] = name

        if (phone != originalPhone) {
            // ✅ Add +91 prefix if not already present
            val formattedPhone = if (phone.startsWith("+91")) phone else "+91$phone"
            updateMap["phone"] = formattedPhone
        }

        if (email != originalEmail) updateMap["email"] = email

        if (updateMap.isEmpty()) {
            Toast.makeText(this, "No changes to update", Toast.LENGTH_SHORT).show()
            return
        }
        Log.d("EditProfile", "Using firebaseUid = $firebaseUid")


        updateMap["firebaseUid"] = firebaseUid

        apiService.updateUserProfilePartial(updateMap)
            .enqueue(object : Callback<UpdateProfileResponse> {
                override fun onResponse(
                    call: Call<UpdateProfileResponse>,
                    response: Response<UpdateProfileResponse>
                ) {
                    if (response.isSuccessful && response.body()?.user != null) {
                        val updatedUser = response.body()!!.user
                        SharedPrefManager.saveUser(this@EditProfileActivity, updatedUser)
                        Toast.makeText(this@EditProfileActivity, "Profile updated!", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.e("EditProfile", "Update failed: ${response.code()} - $errorBody")
                        Toast.makeText(this@EditProfileActivity, "Update failed", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<UpdateProfileResponse>, t: Throwable) {
                    Toast.makeText(this@EditProfileActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }


    private fun hasUnsavedChanges(): Boolean {
        val currentName = binding.userName.text.toString()
        val currentPhone = binding.mobileNo.text.toString()
        val currentEmail = binding.emailId.text.toString()
        return currentName != originalName || currentPhone != originalPhone || currentEmail != originalEmail
    }

    private fun showDiscardChangesDialogOrFinish() {
        if (hasUnsavedChanges()) {
            AlertDialog.Builder(this)
                .setTitle("Discard Changes?")
                .setMessage("You have unsaved changes. Are you sure you want to discard them?")
                .setPositiveButton("Yes") { _, _ -> finish() }
                .setNegativeButton("Cancel", null)
                .show()
        } else {
            setResult(Activity.RESULT_OK)
            finish()
        }
    }
}
