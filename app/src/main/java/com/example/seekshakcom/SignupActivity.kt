package com.example.seekshakcom

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.seekshakcom.model.RegisterRequest
import com.example.seekshakcom.model.RegisterResponse
import com.example.seekshakcom.network.ApiClient
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.TimeUnit

class SignupActivity : AppCompatActivity() {

    private lateinit var roleSpinner: Spinner
    private lateinit var nameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var otpEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var sendOtpButton: Button
    private lateinit var verifyOtpButton: Button
    private lateinit var registerButton: Button
    private lateinit var loginRedirectText: TextView
    private lateinit var progressBar: ProgressBar  // Added ProgressBar

    private lateinit var auth: FirebaseAuth
    private var verificationId: String? = null
    private var otpVerified = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        auth = FirebaseAuth.getInstance()

        // Bind UI components
        roleSpinner = findViewById(R.id.spinner_role)
        nameEditText = findViewById(R.id.editText_name)
        emailEditText = findViewById(R.id.editText_email)
        phoneEditText = findViewById(R.id.editText_phone)
        otpEditText = findViewById(R.id.editText_otp)
        passwordEditText = findViewById(R.id.editText_password)
        confirmPasswordEditText = findViewById(R.id.editText_confirm_password)
        sendOtpButton = findViewById(R.id.button_send_otp)
        verifyOtpButton = findViewById(R.id.button_verify_otp)
        registerButton = findViewById(R.id.button_register)
        loginRedirectText = findViewById(R.id.textView_login_redirect)
        progressBar = findViewById(R.id.progressBar)  // Binding ProgressBar

        sendOtpButton.setOnClickListener {
            val phone = phoneEditText.text.toString().trim()
            if (phone.length != 10) {
                Toast.makeText(this, "Please enter a valid 10-digit mobile number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val phoneNumber = "+91$phone"
            sendOtp(phoneNumber)
        }

        verifyOtpButton.setOnClickListener {
            val code = otpEditText.text.toString().trim()
            if (verificationId != null && code.isNotEmpty()) {
                val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
                signInWithPhoneAuthCredential(credential)
            } else {
                Toast.makeText(this, "OTP not sent or empty", Toast.LENGTH_SHORT).show()
            }
        }

        registerButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val phone = phoneEditText.text.toString().trim()
            val password = passwordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()
            val role = roleSpinner.selectedItem.toString()

            if (!otpVerified) {
                Toast.makeText(this, "Please verify OTP first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (name.isEmpty() || phone.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            registerButton.isEnabled = false
            progressBar.visibility = View.VISIBLE  // Show progress bar

            val currentUser = auth.currentUser
            if (currentUser == null) {
                Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
                progressBar.visibility = View.GONE  // Hide progress bar
                registerButton.isEnabled = true
                return@setOnClickListener
            }

            currentUser.getIdToken(true).addOnCompleteListener { tokenTask ->
                if (tokenTask.isSuccessful) {
                    val firebaseToken = tokenTask.result?.token ?: ""

                    val registerRequest = RegisterRequest(
                        role = role,
                        name = name,
                        email = email,
                        phone = "+91$phone",
                        password = password,
                        firebaseToken = firebaseToken
                    )

                    ApiClient.instance.registerUser(registerRequest).enqueue(object : Callback<RegisterResponse> {
                        override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                            progressBar.visibility = View.GONE  // Hide progress bar
                            registerButton.isEnabled = true
                            if (response.isSuccessful) {
                                val userId = response.body()?.userId

                                val sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
                                sharedPreferences.edit().apply {
                                    putString("user_id", userId)
                                    putString("user_role", role)
                                    putString("user_name", name)
                                    putString("user_email", email)
                                    apply()
                                }

                                Toast.makeText(this@SignupActivity, "Registration successful: $userId", Toast.LENGTH_LONG).show()
                                startActivity(Intent(this@SignupActivity, LoginActivity::class.java))
                                finish()
                            } else {
                                val errorBody = response.errorBody()?.string()
                                Log.e("SignupActivity", "Registration failed: ${response.code()} - $errorBody")
                                Toast.makeText(this@SignupActivity, "Failed: $errorBody", Toast.LENGTH_LONG).show()
                            }
                        }

                        override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                            progressBar.visibility = View.GONE  // Hide progress bar
                            registerButton.isEnabled = true
                            Toast.makeText(this@SignupActivity, "Error: ${t.message}", Toast.LENGTH_LONG).show()
                        }
                    })
                } else {
                    progressBar.visibility = View.GONE  // Hide progress bar
                    registerButton.isEnabled = true
                    Toast.makeText(this, "Failed to get Firebase token", Toast.LENGTH_SHORT).show()
                }
            }
        }

        loginRedirectText.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun sendOtp(phoneNumber: String) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    otpVerified = true
                    Toast.makeText(this@SignupActivity, "Phone verified automatically", Toast.LENGTH_SHORT).show()
                    signInWithPhoneAuthCredential(credential)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    Toast.makeText(this@SignupActivity, "Verification failed: ${e.message}", Toast.LENGTH_LONG).show()
                }

                @SuppressLint("SetTextI18n")
                override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                    this@SignupActivity.verificationId = verificationId
                    Toast.makeText(this@SignupActivity, "OTP Sent", Toast.LENGTH_SHORT).show()
                    sendOtpButton.text = "Resend OTP"
                }
            })
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    otpVerified = true
                    Toast.makeText(this, "OTP Verified", Toast.LENGTH_SHORT).show()
                } else {
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        Toast.makeText(this, "Invalid OTP", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }
}
