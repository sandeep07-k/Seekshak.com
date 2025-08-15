package com.example.seekshakcom

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.seekshakcom.institute.InstituteHomeActivity
import com.example.seekshakcom.tutor.TutorHomeActivity
import com.example.seekshakcom.model.UserRoleResponse
import com.example.seekshakcom.network.ApiClient
import com.example.seekshakcom.student.StudentHomeActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.TimeUnit

class LoginActivity2 : AppCompatActivity() {

    private lateinit var otpFields: List<EditText>
    private lateinit var verifyOtpButton: Button
    private lateinit var resendOtpText: TextView
    private lateinit var backPage: ImageView
    private lateinit var editNumber: ImageView

    private lateinit var auth: FirebaseAuth
    private lateinit var verificationId: String
    private lateinit var phone: String
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    private lateinit var countdownTimer: CountDownTimer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login2)

        auth = FirebaseAuth.getInstance()
        verificationId = intent.getStringExtra("verificationId") ?: ""
        phone = intent.getStringExtra("phone") ?: ""
        resendToken = ResendTokenHolder.resendToken

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = ContextCompat.getColor(this, R.color.soft_blue)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true

        // UI references
        otpFields = listOf(
            findViewById(R.id.otp1), findViewById(R.id.otp2), findViewById(R.id.otp3),
            findViewById(R.id.otp4), findViewById(R.id.otp5), findViewById(R.id.otp6)
        )
        verifyOtpButton = findViewById(R.id.verifyOtpButton)
        resendOtpText = findViewById(R.id.resendOtpText)
        backPage = findViewById(R.id.goToBackPage)
        editNumber = findViewById(R.id.edit_number)


        setupOtpAutoMove()

        findViewById<TextView>(R.id.subtitle).text = "We sent a 6-digit code to $phone"

        editNumber.setOnClickListener {
            finish()
        }
        verifyOtpButton.setOnClickListener {
            hideKeyboard()
            verifyOtp()
        }
        backPage.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        resendOtpText.setOnClickListener {
            if (resendOtpText.isEnabled) {
                resendOtp()
            }
        }

        startResendCountdown()
    }

    private fun verifyOtp() {
        val code = otpFields.joinToString("") { it.text.toString().trim() }

        if (code.length != 6) {
            Toast.makeText(this, "Enter valid 6-digit OTP", Toast.LENGTH_SHORT).show()
            return
        }

        val loadingDialog = AlertDialog.Builder(this)
            .setView(R.layout.dialog_loading_otp)
            .setCancelable(false)
            .create()
        loadingDialog.show()

        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        auth.signInWithCredential(credential).addOnCompleteListener { task ->
            loadingDialog.dismiss()
            if (task.isSuccessful) {
                fetchRoleFromBackend(phone)
            } else {
                Toast.makeText(this, "Invalid OTP", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun fetchRoleFromBackend(phone: String) {
        val loadingDialog = AlertDialog.Builder(this)
            .setView(R.layout.dialog_loading_otp)
            .setCancelable(false)
            .create()
        loadingDialog.show()

        ApiClient.instance.getUserRole(phone).enqueue(object : Callback<UserRoleResponse> {
            override fun onResponse(call: Call<UserRoleResponse>, response: Response<UserRoleResponse>) {
                loadingDialog.dismiss()
                if (response.isSuccessful && response.body() != null) {
                    val role = response.body()!!.role
                    val userId = response.body()!!.userId
                    val token = response.body()?.token ?: ""
                    saveUserInfo(role, userId, token)
                    redirectToHome(role)
                } else {
                    Toast.makeText(this@LoginActivity2, "Failed to fetch user role", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UserRoleResponse>, t: Throwable) {
                loadingDialog.dismiss()
                Toast.makeText(this@LoginActivity2, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun saveUserInfo(role: String, userId: String, token: String) {
        getSharedPreferences("UserPrefs", MODE_PRIVATE).edit().apply {
            putString("token", token)
            putBoolean("isLoggedIn", true)
            putString("userId", userId)
            putString("role", role)
            apply()
        }
    }

    private fun redirectToHome(role: String) {
        val intent = when (role) {
            "student" -> Intent(this, StudentHomeActivity::class.java)
            "educator" -> Intent(this, TutorHomeActivity::class.java)
            "institute" -> Intent(this, InstituteHomeActivity::class.java)
            else -> Intent(this, StudentHomeActivity::class.java)
        }
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun resendOtp() {
        resendOtpText.isEnabled = false
        resendOtpText.alpha = 0.7f
        resendOtpText.text = "Resending code"

        val optionsBuilder = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phone)
            .setTimeout(30L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    auth.signInWithCredential(credential).addOnCompleteListener {
                        if (it.isSuccessful) fetchRoleFromBackend(phone)
                    }
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    Toast.makeText(this@LoginActivity2, "Verification failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }

                override fun onCodeSent(newVerificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                    verificationId = newVerificationId
                    resendToken = token
                    Toast.makeText(this@LoginActivity2, "OTP resent successfully", Toast.LENGTH_SHORT).show()
                    startResendCountdown()
                }
            })

        // Only use forceResendingToken if available
        resendToken?.let {
            optionsBuilder.setForceResendingToken(it)
        }

        PhoneAuthProvider.verifyPhoneNumber(optionsBuilder.build())
    }

    private fun startResendCountdown() {
        resendOtpText.isEnabled = false
        resendOtpText.alpha = 0.5f
        countdownTimer = object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                resendOtpText.text = "Resend code in ${millisUntilFinished / 1000}s"
            }

            override fun onFinish() {
                resendOtpText.text = "Resend code"
                resendOtpText.isEnabled = true
                resendOtpText.alpha = 1f
            }
        }
        countdownTimer.start()
    }

    private fun setupOtpAutoMove() {
        for (i in otpFields.indices) {
            otpFields[i].addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    if (s?.length == 1 && i < otpFields.size - 1) otpFields[i + 1].requestFocus()
                    else if (s?.isEmpty() == true && i > 0) otpFields[i - 1].requestFocus()
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })
        }
    }

    private fun hideKeyboard() {
        val view = currentFocus
        if (view != null) {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}
