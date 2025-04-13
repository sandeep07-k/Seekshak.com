package com.example.seekshakcom

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class ResetPasswordActivity : AppCompatActivity() {

    private lateinit var otpEditText: EditText
    private lateinit var newPasswordEditText: EditText
    private lateinit var confirmButton: Button
    private lateinit var progressBar: ProgressBar

    private lateinit var verificationId: String

    private lateinit var mAuth: FirebaseAuth
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        otpEditText = findViewById(R.id.otpEditText)
        newPasswordEditText = findViewById(R.id.newPasswordEditText)
        confirmButton = findViewById(R.id.confirmButton)
        progressBar = findViewById(R.id.progressBar)

        mAuth = FirebaseAuth.getInstance()

        // Retrieve verification ID from intent
        verificationId = intent.getStringExtra("verificationId") ?: ""

        // Handle confirm button click
        confirmButton.setOnClickListener {
            val otp = otpEditText.text.toString().trim()
            val newPassword = newPasswordEditText.text.toString().trim()

            // Check if OTP and password are valid
            if (otp.length < 6 || newPassword.length < 6) {
                Toast.makeText(this, "Invalid OTP or password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Create PhoneAuthCredential and sign in
            val credential = PhoneAuthProvider.getCredential(verificationId, otp)
            signInWithPhoneAuthCredential(credential, newPassword)
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential, newPassword: String) {
        progressBar.visibility = android.view.View.VISIBLE

        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Get Firebase ID Token and update password on backend
                    mAuth.currentUser?.getIdToken(true)
                        ?.addOnSuccessListener { result ->
                            val firebaseToken = result.token
                            updatePasswordOnBackend(newPassword, firebaseToken ?: "")
                        }
                        ?.addOnFailureListener {
                            progressBar.visibility = android.view.View.GONE
                            Toast.makeText(this, "Token error: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    progressBar.visibility = android.view.View.GONE
                    Toast.makeText(this, "OTP verification failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun updatePasswordOnBackend(password: String, firebaseToken: String) {
        val json = JSONObject().apply {
            put("newPassword", password)
            put("firebaseToken", firebaseToken)
        }

        val mediaType = "application/json".toMediaType()
        val requestBody = json.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url("https://seekshak-backend.onrender.com/api/auth/reset-password") // Replace with your actual backend URL
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    progressBar.visibility = android.view.View.GONE
                    Toast.makeText(this@ResetPasswordActivity, "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    progressBar.visibility = android.view.View.GONE
                    if (response.isSuccessful) {
                        Toast.makeText(this@ResetPasswordActivity, "Password updated!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@ResetPasswordActivity, LoginActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this@ResetPasswordActivity, "Error: ${response.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}
