package com.example.seekshakcom

import android.app.Activity
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

class SignupActivity2 : AppCompatActivity() {

    private lateinit var otpFields: List<EditText>
    private lateinit var verifyOtpButton: Button
    private lateinit var resendOtpButton: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var backPage: TextView
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

        otpFields = listOf(
            findViewById(R.id.otp1),
            findViewById(R.id.otp2),
            findViewById(R.id.otp3),
            findViewById(R.id.otp4),
            findViewById(R.id.otp5),
            findViewById(R.id.otp6)
        )

        verifyOtpButton = findViewById(R.id.button_verify_otp)
        resendOtpButton = findViewById(R.id.button_resend_otp)
        progressBar = findViewById(R.id.progressBar)
        backPage = findViewById(R.id.goToBackPage)
        auth = FirebaseAuth.getInstance()

        // Get data from intent
        role = intent.getStringExtra("role") ?: ""
        name = intent.getStringExtra("name") ?: ""
        email = intent.getStringExtra("email") ?: ""
        phone = intent.getStringExtra("phone") ?: ""
        verificationId = intent.getStringExtra("verificationId")

//        resendOtp()

        verifyOtpButton.setOnClickListener {
            val code = otpFields.joinToString("") { it.text.toString().trim() }
            if (verificationId != null && code.length == 6) {
                progressBar.visibility = View.VISIBLE
                val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
                signInWithPhoneAuthCredential(credential)
            } else {
                Toast.makeText(this, "Enter valid 6-digit OTP", Toast.LENGTH_SHORT).show()
            }
        }

        resendOtpButton.setOnClickListener {
            resendOtp()
        }
        backPage.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }

        autoMoveCursor()
    }

    private fun autoMoveCursor() {
        for (i in 0 until otpFields.size - 1) {
            otpFields[i].addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {
                    // No action needed here
                }

                override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
                    // No action needed here
                }

                override fun afterTextChanged(editable: Editable?) {
                    if (otpFields[i].text.length == 1) {
                        otpFields[i + 1].requestFocus()
                    }
                }
            })
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
                progressBar.visibility = View.GONE
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
