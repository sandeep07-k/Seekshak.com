package com.example.seekshakcom

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.seekshakcom.model.EmailRequest
import com.example.seekshakcom.model.OtpVerificationRequest
import com.example.seekshakcom.network.ApiResponse
import com.example.seekshakcom.network.RetrofitInstance
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EmailVerificationActivity : AppCompatActivity() {

    private lateinit var otpFields: List<EditText>
    private lateinit var verifyOtpButton: Button
    private lateinit var resendTimerText: TextView
    private lateinit var emailText: TextView
    private var resendJob: Job? = null

    private lateinit var email: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_email_verification)

        email = intent.getStringExtra("email") ?: ""
        emailText = findViewById(R.id.email_Id)
        emailText.text = email

        otpFields = listOf(
            findViewById(R.id.otp1), findViewById(R.id.otp2), findViewById(R.id.otp3),
            findViewById(R.id.otp4), findViewById(R.id.otp5), findViewById(R.id.otp6)
        )

        verifyOtpButton = findViewById(R.id.verifyOtpButton)
        resendTimerText = findViewById(R.id.resend_timer)

        setupOtpInputNavigation()
        startResendCountdown()


        verifyOtpButton.setOnClickListener {
            val enteredOtp = otpFields.joinToString("") { it.text.toString().trim() }
            if (enteredOtp.length == 6) {
                verifyOtp(email, enteredOtp)
            } else {
                Toast.makeText(this, "Please enter full 6-digit OTP", Toast.LENGTH_SHORT).show()
            }
        }

        resendTimerText.setOnClickListener {
            if (resendTimerText.text.contains("Resend code")) {
                sendOtpToEmail(email)
                startResendCountdown()
            }
        }
    }

    private fun setupOtpInputNavigation() {
        for (i in otpFields.indices) {
            otpFields[i].addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    if (s?.length == 1 && i < otpFields.size - 1) {
                        otpFields[i + 1].requestFocus()
                    } else if (s?.isEmpty() == true && i > 0) {
                        otpFields[i - 1].requestFocus()
                    }
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })
        }
    }

    private fun startResendCountdown() {
        resendTimerText.isEnabled = false
        resendJob?.cancel()

        resendJob = CoroutineScope(Dispatchers.Main).launch {
            var timeLeft = 30
            while (timeLeft > 0) {
                resendTimerText.text = "Resend code in ${timeLeft}s"
                delay(1000)
                timeLeft--
            }
            resendTimerText.text = "Resend code"
            resendTimerText.isEnabled = true
        }
    }

    private fun sendOtpToEmail(email: String) {
        RetrofitInstance.emailOtpApi.sendOtp(EmailRequest(email))
            .enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(this@EmailVerificationActivity, "OTP resent to $email", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@EmailVerificationActivity, "Failed to resend OTP", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    Toast.makeText(this@EmailVerificationActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun verifyOtp(email: String, otp: String) {
        val loadingDialog = AlertDialog.Builder(this)
            .setView(R.layout.dialog_loading_otp)
            .setCancelable(false)
            .create()
        loadingDialog.show()

        RetrofitInstance.emailOtpApi.verifyOtp(OtpVerificationRequest(email, otp))
            .enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    loadingDialog.dismiss()
                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(this@EmailVerificationActivity, "Email Verified", Toast.LENGTH_SHORT).show()

                        // ✅ Send result back to EnterEmailActivity
                        val resultIntent = Intent()
                        resultIntent.putExtra("verified_email", email)
                        setResult(Activity.RESULT_OK, resultIntent)
                        finish()

                    } else {
                        Toast.makeText(this@EmailVerificationActivity, response.body()?.message ?: "Invalid OTP", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    loadingDialog.dismiss()
                    Toast.makeText(this@EmailVerificationActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    override fun onDestroy() {
        resendJob?.cancel()
        super.onDestroy()
    }
}
