package com.example.seekshakcom

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import java.util.concurrent.TimeUnit

class OtpVerificationActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var verificationId: String
    private lateinit var phoneNumber: String

    private lateinit var otpFields: List<EditText>
    private lateinit var verifyButton: Button
    private lateinit var resendTimer: TextView
    private lateinit var backArrow: ImageView
    private lateinit var editNumber: ImageView

    private var countDownTimer: CountDownTimer? = null
    private var canResend = false
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp_verification)

        auth = FirebaseAuth.getInstance()

        verificationId = intent.getStringExtra("verificationId") ?: ""
        phoneNumber = intent.getStringExtra("phoneNumber") ?: ""
        resendToken = intent.getParcelableExtra("resendToken")

        findViewById<TextView>(R.id.subtitle).text = "We sent a 6-digit code to $phoneNumber"

        otpFields = listOf(
            findViewById(R.id.otp1),
            findViewById(R.id.otp2),
            findViewById(R.id.otp3),
            findViewById(R.id.otp4),
            findViewById(R.id.otp5),
            findViewById(R.id.otp6),
        )

        verifyButton = findViewById(R.id.verifyOtpButton)
        resendTimer = findViewById(R.id.resend_timer)
        backArrow = findViewById(R.id.back_arrow)
        editNumber = findViewById(R.id.edit_number)

        setupOtpInputs()
        setupCallbacks()
        startResendCountdown()

        backArrow.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        editNumber.setOnClickListener {
            finish()
        }

        resendTimer.setOnClickListener {
            if (canResend && resendToken != null) {
                resendOtp()
            }
        }

        verifyButton.setOnClickListener {
            val code = otpFields.joinToString("") { it.text.toString().trim() }
            if (code.length == 6) {
                verifyCode(code)
            } else {
                Toast.makeText(this, "Please enter a 6-digit code", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun verifyCode(code: String) {
        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        signInWithPhoneCredential(credential)
    }

    private fun signInWithPhoneCredential(credential: PhoneAuthCredential) {
        val loadingDialog = AlertDialog.Builder(this)
            .setView(R.layout.dialog_loading_otp)
            .setCancelable(false)
            .create()
        loadingDialog.show()

        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            // ✅ Correct: update phone without signing in as new user
            currentUser.updatePhoneNumber(credential)
                .addOnCompleteListener(this) { task ->
                    loadingDialog.dismiss()
                    if (task.isSuccessful) {
                        val resultIntent = Intent().apply {
                            putExtra("verifiedPhoneNumber", phoneNumber) // Keep +91
                        }
                        setResult(Activity.RESULT_OK, resultIntent)
                        finish()
                    } else {
                        Toast.makeText(this, "Failed to verify phone: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            loadingDialog.dismiss()
            Toast.makeText(this, "User not logged in. Please login again.", Toast.LENGTH_SHORT).show()
        }
    }







    private fun setupOtpInputs() {
        for (i in 0..4) {
            otpFields[i].addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    if (!s.isNullOrEmpty()) otpFields[i + 1].requestFocus()
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })
        }

        otpFields[5].addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun startResendCountdown() {
        canResend = false
        resendTimer.text = "Resend code in 30s"
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                resendTimer.text = "Resend code in ${millisUntilFinished / 1000}s"
            }

            override fun onFinish() {
                resendTimer.text = "Resend code"
                canResend = true
            }
        }.start()
    }

    private fun setupCallbacks() {
        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // Auto-retrieval or instant verification
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Toast.makeText(this@OtpVerificationActivity, "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }

            override fun onCodeSent(newVerificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                verificationId = newVerificationId
                resendToken = token
                Toast.makeText(this@OtpVerificationActivity, "OTP resent", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun resendOtp() {
        startResendCountdown()

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
            .setForceResendingToken(resendToken!!)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    override fun onDestroy() {
        countDownTimer?.cancel()
        super.onDestroy()
    }
}
