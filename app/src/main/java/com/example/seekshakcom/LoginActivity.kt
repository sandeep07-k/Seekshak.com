package com.example.seekshakcom

import android.app.AlertDialog
import android.content.*
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.seekshakcom.model.UserExistsResponse
import com.example.seekshakcom.network.ApiClient
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.TimeUnit
import com.google.firebase.auth.PhoneAuthProvider

object ResendTokenHolder {
    var resendToken: PhoneAuthProvider.ForceResendingToken? = null
}

class LoginActivity : AppCompatActivity() {

    private lateinit var phoneEditText: EditText
    private lateinit var sendOtpButton: Button
    private lateinit var goToSignup: TextView
    private lateinit var auth: FirebaseAuth


    private var lastSentPhone: String? = null
    private var lastVerificationId: String? = null
    private var lastResendToken: PhoneAuthProvider.ForceResendingToken? = null

    private lateinit var loadingDialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Auto-login check
        val prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val isLoggedIn = prefs.getBoolean("isLoggedIn", false)
        val role = prefs.getString("role", null)

        if (isLoggedIn && role != null) {
            redirectToHome(role)
            finish()
            return
        }
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        phoneEditText = findViewById(R.id.phoneEditText)
        sendOtpButton = findViewById(R.id.sendOtpButton)
        goToSignup = findViewById(R.id.goToSignup)

        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = ContextCompat.getColor(this, R.color.soft_blue)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true

        loadingDialog = AlertDialog.Builder(this)
            .setView(R.layout.dialog_progress)
            .setCancelable(false)
            .create()

        goToSignup.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }

        sendOtpButton.setOnClickListener {
            hideKeyboard()
            val phone = phoneEditText.text.toString().trim()
            if (phone.length != 10) {
                phoneEditText.error = "Enter valid 10-digit number"
                return@setOnClickListener
            }

            val formattedPhone = if (phone.startsWith("+91")) phone else "+91$phone"

            // ✅ Reuse session if phone hasn't changed
            if (formattedPhone == lastSentPhone && lastVerificationId != null && lastResendToken != null) {
                val intent = Intent(this@LoginActivity, LoginActivity2::class.java)

                intent.putExtra("verificationId", lastVerificationId)
                intent.putExtra("phone", formattedPhone)
                ResendTokenHolder.resendToken = lastResendToken

                startActivity(intent)
            } else {
                checkUserExists(formattedPhone)
            }
        }
    }
    private fun redirectToHome(role: String?) {
        val intent = when (role) {
            "student" -> Intent(this, StudentHomeActivity::class.java)
            "educator" -> Intent(this, TutorHomeActivity::class.java)
            "institute" -> Intent(this, InstituteHomeActivity::class.java)
            else -> Intent(this, StudentHomeActivity::class.java)
        }
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }


    private fun checkUserExists(formattedPhone: String) {
        sendOtpButton.isEnabled = false
        loadingDialog.show()

        ApiClient.instance.checkUserExists(formattedPhone).enqueue(object : Callback<UserExistsResponse> {
            override fun onResponse(call: Call<UserExistsResponse>, response: Response<UserExistsResponse>) {
                if (response.body()?.exists == true) {
                    sendOtp(formattedPhone)
                } else {
                    sendOtpButton.isEnabled = true
                    loadingDialog.dismiss()
                    Toast.makeText(this@LoginActivity, "No account found. Please sign up first.", Toast.LENGTH_LONG).show()
                    goToSignup.visibility = View.VISIBLE
                }
            }

            override fun onFailure(call: Call<UserExistsResponse>, t: Throwable) {
                sendOtpButton.isEnabled = true
                loadingDialog.dismiss()
                Toast.makeText(this@LoginActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun sendOtp(formattedPhone: String) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(formattedPhone)
            .setTimeout(30L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    // No auto login here
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    sendOtpButton.isEnabled = true
                    loadingDialog.dismiss()
                    Toast.makeText(this@LoginActivity, "Failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }

                override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                    loadingDialog.dismiss()
                    sendOtpButton.isEnabled = true

                    // ✅ Save session
                    lastSentPhone = formattedPhone
                    lastVerificationId = verificationId
                    lastResendToken = token

                    val intent = Intent(this@LoginActivity, LoginActivity2::class.java)
                    intent.putExtra("verificationId", verificationId)
                    intent.putExtra("phone", formattedPhone)
                    ResendTokenHolder.resendToken = lastResendToken
                    startActivity(intent)
                }
            }).build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun hideKeyboard() {
        val view = currentFocus
        if (view != null) {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}
