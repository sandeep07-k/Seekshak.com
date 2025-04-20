package com.example.seekshakcom

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import java.util.concurrent.TimeUnit

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var phoneEditText: EditText
    private lateinit var sendOtpButton: Button
    private lateinit var verifyOtpButton: Button
    private lateinit var resendOtpButton: TextView
    private lateinit var goToBackPage: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var progressBar1: ProgressBar
    private lateinit var otpFields: List<EditText>

    private lateinit var auth: FirebaseAuth
    private var verificationId: String? = null
    private var isResendClicked = false
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password) // change this to actual layout name if different

        auth = FirebaseAuth.getInstance()

        phoneEditText = findViewById(R.id.phoneEditText)
        sendOtpButton = findViewById(R.id.sendOtpButton)
        verifyOtpButton = findViewById(R.id.button_verify_otp)
        resendOtpButton = findViewById(R.id.button_resend_otp)
        goToBackPage = findViewById(R.id.goToBackPage)
        progressBar = findViewById(R.id.progressBar)
        progressBar1 = findViewById(R.id.progressBar1)

        otpFields = listOf(
            findViewById(R.id.otp1),
            findViewById(R.id.otp2),
            findViewById(R.id.otp3),
            findViewById(R.id.otp4),
            findViewById(R.id.otp5),
            findViewById(R.id.otp6)
        )

        sendOtpButton.setOnClickListener {
            val phoneNumber = "+91" + phoneEditText.text.toString().trim()
            if (phoneNumber.length == 13) {
                progressBar1.visibility = View.VISIBLE
                sendOtp(phoneNumber)
            } else {
                Toast.makeText(this, "Enter valid phone number", Toast.LENGTH_SHORT).show()
            }
        }

        verifyOtpButton.setOnClickListener {
            val otp = otpFields.joinToString("") { it.text.toString().trim() }
            if (otp.length == 6 && verificationId != null) {
                progressBar.visibility = View.VISIBLE
                val credential = PhoneAuthProvider.getCredential(verificationId!!, otp)
                signInWithPhoneAuthCredential(credential)
            } else {
                Toast.makeText(this, "Enter valid 6-digit OTP", Toast.LENGTH_SHORT).show()
            }
        }

        resendOtpButton.setOnClickListener {
            val phoneNumber = "+91" + phoneEditText.text.toString().trim()
            if (phoneNumber.length == 13 && ::resendToken.isInitialized) {
                isResendClicked = true
                resendOtp(phoneNumber)
            } else {
                Toast.makeText(this, "Enter valid phone number first", Toast.LENGTH_SHORT).show()
            }
        }

        goToBackPage.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        autoMoveCursor()
    }

    private fun sendOtp(phoneNumber: String) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun resendOtp(phoneNumber: String) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
            .setForceResendingToken(resendToken)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            progressBar1.visibility = View.GONE
            Toast.makeText(this@ForgotPasswordActivity, "Verification failed: ${e.message}", Toast.LENGTH_LONG).show()
        }

        override fun onCodeSent(id: String, token: PhoneAuthProvider.ForceResendingToken) {
            progressBar1.visibility = View.GONE
            verificationId = id
            resendToken = token
            if (isResendClicked) {
                Toast.makeText(this@ForgotPasswordActivity, "A new OTP has been sent", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@ForgotPasswordActivity, "OTP sent", Toast.LENGTH_SHORT).show()
            }
            isResendClicked = false
        }
    }


    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                progressBar.visibility = View.GONE
                if (task.isSuccessful) {
                    val intent = Intent(this, ResetPasswordActivity::class.java)
                    intent.putExtra("phone", phoneEditText.text.toString().trim())
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Invalid OTP", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun autoMoveCursor() {
        for (i in 0 until otpFields.size - 1) {
            otpFields[i].addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    if (otpFields[i].text.length == 1) {
                        otpFields[i + 1].requestFocus()
                    }
                }
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })
        }
    }
}
