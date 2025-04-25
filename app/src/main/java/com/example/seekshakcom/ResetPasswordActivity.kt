package com.example.seekshakcom

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.firebase.auth.FirebaseAuth
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class ResetPasswordActivity : AppCompatActivity() {

    private lateinit var newPasswordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var toggleNewPasswordVisibility: ImageView
    private lateinit var toggleConfirmPasswordVisibility: ImageView
    private var isNewPasswordVisible = false
    private var isConfirmPasswordVisible = false

    private lateinit var goToBackPage: TextView
    private lateinit var confirmButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var phoneNumber: String

    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        // 1. Allow layout to draw behind system bars
        WindowCompat.setDecorFitsSystemWindows(window, false)
        // 2. Make status bar transparent
        Color.TRANSPARENT.also { window.statusBarColor = it }
        // 3. Optional: Change status bar icon color (dark icons = true)
        val insetsController = WindowInsetsControllerCompat(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = true

        newPasswordEditText = findViewById(R.id.newPasswordEditText)
        confirmPasswordEditText = findViewById(R.id.editText_confirm_password)
        toggleNewPasswordVisibility = findViewById(R.id.toggleNewPasswordVisibility)
        toggleConfirmPasswordVisibility = findViewById(R.id.toggleNewPasswordVisibility1)
        confirmButton = findViewById(R.id.confirmButton)
        progressBar = findViewById(R.id.progressBar)
        goToBackPage = findViewById(R.id.goToBackPage)
        firebaseAuth = FirebaseAuth.getInstance()

        phoneNumber = intent.getStringExtra("phone") ?: ""

        goToBackPage.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        toggleNewPasswordVisibility.setOnClickListener {
            isNewPasswordVisible = !isNewPasswordVisible
            newPasswordEditText.inputType = if (isNewPasswordVisible)
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            else
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

            newPasswordEditText.setSelection(newPasswordEditText.text.length)
            toggleNewPasswordVisibility.setImageResource(
                if (isNewPasswordVisible) R.drawable.ic_visibility else R.drawable.ic_visibility_off
            )
        }

        toggleConfirmPasswordVisibility.setOnClickListener {
            isConfirmPasswordVisible = !isConfirmPasswordVisible
            confirmPasswordEditText.inputType = if (isConfirmPasswordVisible)
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            else
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

            confirmPasswordEditText.setSelection(confirmPasswordEditText.text.length)
            toggleConfirmPasswordVisibility.setImageResource(
                if (isConfirmPasswordVisible) R.drawable.ic_visibility else R.drawable.ic_visibility_off
            )
        }

        confirmButton.setOnClickListener {
            val password = newPasswordEditText.text.toString().trim()
            val confirmPassword = confirmPasswordEditText.text.toString().trim()

            if (password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill both fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val strongPasswordPattern = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{6,}")
            if (!password.matches(strongPasswordPattern)) {
                Toast.makeText(
                    this,
                    "Password must be at least 6 characters with upper, lower, and a digit",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            progressBar.visibility = View.VISIBLE

            firebaseAuth.currentUser?.getIdToken(true)
                ?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val idToken = task.result?.token
                        if (idToken != null) {
                            updatePasswordOnBackend(password, idToken)
                        } else {
                            progressBar.visibility = View.GONE
                            Toast.makeText(this, "Failed to get Firebase token", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        progressBar.visibility = View.GONE
                        Toast.makeText(this, "Token fetch failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
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
            .url("https://seekshak-backend.onrender.com/api/auth/reset-password")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this@ResetPasswordActivity, "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    if (response.isSuccessful) {
                        Toast.makeText(this@ResetPasswordActivity, "Password updated successfully", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@ResetPasswordActivity, LoginActivity::class.java))
                        finish()
                    } else {
                        val errorBody = response.body?.string() ?: "No error body"
                        Toast.makeText(
                            this@ResetPasswordActivity,
                            "Error: ${response.code} - $errorBody",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        })
    }
}
