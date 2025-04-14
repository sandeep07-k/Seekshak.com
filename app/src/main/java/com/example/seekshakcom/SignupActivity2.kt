package com.example.seekshakcom

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import java.util.concurrent.TimeUnit

class SignupActivity2 : AppCompatActivity() {

    private lateinit var otpEditText: EditText
    private lateinit var verifyOtpButton: Button
    private lateinit var resendOtpButton: Button
    private lateinit var auth: FirebaseAuth
    private var verificationId: String? = null
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken

    private lateinit var role: String
    private lateinit var name: String
    private lateinit var email: String
    private lateinit var phone: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup2)

        otpEditText = findViewById(R.id.editText_otp)
        verifyOtpButton = findViewById(R.id.button_verify_otp)
        resendOtpButton = findViewById(R.id.button_resend_otp) // make sure to add this in XML
        auth = FirebaseAuth.getInstance()

        // Get data from intent
        role = intent.getStringExtra("role") ?: ""
        name = intent.getStringExtra("name") ?: ""
        email = intent.getStringExtra("email") ?: ""
        phone = intent.getStringExtra("phone") ?: ""
        verificationId = intent.getStringExtra("verificationId")

        // If first time opening, resend the OTP immediately
        resendOtp()

        verifyOtpButton.setOnClickListener {
            val code = otpEditText.text.toString().trim()
            if (verificationId != null && code.isNotEmpty()) {
                val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
                signInWithPhoneAuthCredential(credential)
            } else {
                Toast.makeText(this, "Enter OTP", Toast.LENGTH_SHORT).show()
            }
        }

        resendOtpButton.setOnClickListener {
            resendOtp()
        }
    }

    private fun resendOtp() {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    Toast.makeText(this@SignupActivity2, "Auto verification completed", Toast.LENGTH_SHORT).show()
                    signInWithPhoneAuthCredential(credential)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    Toast.makeText(this@SignupActivity2, "Verification failed: ${e.message}", Toast.LENGTH_LONG).show()
                }

                override fun onCodeSent(id: String, token: PhoneAuthProvider.ForceResendingToken) {
                    verificationId = id
                    resendToken = token
                    Toast.makeText(this@SignupActivity2, "OTP resent", Toast.LENGTH_SHORT).show()

                    // send result back to SignupActivity
                    val resultIntent = Intent()
                    resultIntent.putExtra("otp_status", "OTP resent")
                    setResult(Activity.RESULT_OK, resultIntent)
                }
            })
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val resultIntent = Intent(this, SignupActivity3::class.java)
                    resultIntent.putExtra("role", role)
                    resultIntent.putExtra("name", name)
                    resultIntent.putExtra("email", email)
                    resultIntent.putExtra("phone", phone)
                    startActivity(resultIntent)
                    finish()
                } else {
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        Toast.makeText(this, "Invalid OTP", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }
}
