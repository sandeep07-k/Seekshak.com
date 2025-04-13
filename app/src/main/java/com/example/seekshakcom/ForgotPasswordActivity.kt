package com.example.seekshakcom

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import java.util.concurrent.TimeUnit

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var phoneEditText: EditText
    private lateinit var sendOtpButton: Button
    private lateinit var progressBar: ProgressBar

    private lateinit var mAuth: FirebaseAuth
    private lateinit var verificationId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        phoneEditText = findViewById(R.id.phoneEditText)
        sendOtpButton = findViewById(R.id.sendOtpButton)
        progressBar = findViewById(R.id.progressBar)

        mAuth = FirebaseAuth.getInstance()

        sendOtpButton.setOnClickListener {
            val phone = phoneEditText.text.toString().trim()

            if (phone.isEmpty() || phone.length < 10) {
                Toast.makeText(this, "Enter valid phone number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            sendOtp("+91$phone")
        }
    }

    private fun sendOtp(phoneNumber: String) {
        progressBar.visibility = android.view.View.VISIBLE

        val options = PhoneAuthOptions.newBuilder(mAuth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    // Auto verification done, you can skip entering OTP
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    progressBar.visibility = android.view.View.GONE
                    Toast.makeText(this@ForgotPasswordActivity, "Verification failed: ${e.message}", Toast.LENGTH_LONG).show()
                }

                override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                    progressBar.visibility = android.view.View.GONE
                    this@ForgotPasswordActivity.verificationId = verificationId

                    // Navigate to ResetPasswordActivity
                    val intent = Intent(this@ForgotPasswordActivity, ResetPasswordActivity::class.java)
                    intent.putExtra("verificationId", verificationId)
                    intent.putExtra("phoneNumber", phoneNumber)
                    startActivity(intent)
                    finish()
                }
            })
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }
}
