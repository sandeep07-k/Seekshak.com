package com.example.seekshakcom

import android.content.*
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.seekshakcom.model.UserExistsResponse
import com.example.seekshakcom.model.UserRoleResponse
import com.example.seekshakcom.network.ApiClient
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

class LoginActivity : AppCompatActivity() {

    private lateinit var phoneEditText: EditText
    private lateinit var sendOtpButton: Button
    private lateinit var verifyOtpButton: Button
    private lateinit var otpFields: List<EditText>
    private lateinit var progressBar: ProgressBar
    private lateinit var goToSignup: TextView

    private var verificationId: String? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var smsReceiver: BroadcastReceiver
    private lateinit var otpContainer: View
    private lateinit var resendOtpText: TextView
    private lateinit var countdownTimer: CountDownTimer


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //  AUTO-LOGIN CHECK
        val user = FirebaseAuth.getInstance().currentUser
        val prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val isLoggedIn = prefs.getBoolean("isLoggedIn", false)

        if (user != null && isLoggedIn) {
            val role = prefs.getString("role", "")
            val intent = when (role) {
                "student" -> Intent(this, StudentHomeActivity::class.java)
                "educator" -> Intent(this, TutorHomeActivity::class.java)
                "institute" -> Intent(this, InstituteHomeActivity::class.java)
                else -> Intent(this, StudentHomeActivity::class.java)
            }
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return
        }

        setContentView(R.layout.activity_login)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true

        auth = FirebaseAuth.getInstance()

        phoneEditText = findViewById(R.id.phoneEditText)
        sendOtpButton = findViewById(R.id.sendOtpButton)
        verifyOtpButton = findViewById(R.id.verifyOtpButton)
        progressBar = findViewById(R.id.progressBar)
        goToSignup = findViewById(R.id.goToSignup)
        resendOtpText = findViewById(R.id.resendOtpText)
        resendOtpText.visibility = View.GONE


        otpFields = listOf(
            findViewById(R.id.otp1),
            findViewById(R.id.otp2),
            findViewById(R.id.otp3),
            findViewById(R.id.otp4),
            findViewById(R.id.otp5),
            findViewById(R.id.otp6)
        )

        otpContainer = findViewById(R.id.otpContainer)




        sendOtpButton.setOnClickListener {
            hideKeyboard()
            val phone = phoneEditText.text.toString().trim()
            if (phone.length != 10) {
                phoneEditText.error = "Enter valid 10-digit number"
                return@setOnClickListener
            }
            checkUserExists(phone)
        }
        resendOtpText.setOnClickListener {
            val phone = phoneEditText.text.toString().trim()
            val formattedPhone = if (phone.startsWith("+91")) phone else "+91$phone"
            if (resendOtpText.isEnabled) {
                sendOtp(formattedPhone)
                resendOtpText.isEnabled = false
                startResendCountdown()
            }
        }

        verifyOtpButton.setOnClickListener {
            hideKeyboard()
            verifyOtp()
        }

        goToSignup.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }

        startSmsRetriever()
        setupOtpAutoMove()
    }

    private fun checkUserExists(phone: String) {
        progressBar.visibility = View.VISIBLE
        sendOtpButton.isEnabled = false
        sendOtpButton.alpha = 0.5f

        val formattedPhone = if (phone.startsWith("+91")) phone else "+91$phone"

        ApiClient.instance.checkUserExists(formattedPhone).enqueue(object : Callback<UserExistsResponse> {
            override fun onResponse(call: Call<UserExistsResponse>, response: Response<UserExistsResponse>) {
                progressBar.visibility = View.GONE
                sendOtpButton.isEnabled = true
                sendOtpButton.alpha = 1f

                val body = response.body()
                if (body == null) {
                    Toast.makeText(this@LoginActivity, "Unexpected server response", Toast.LENGTH_SHORT).show()
                    return
                }

                if (body.exists) {
                    sendOtp(formattedPhone)
                } else {
                    Toast.makeText(this@LoginActivity, "No account found. Please sign up first.", Toast.LENGTH_LONG).show()
                    goToSignup.visibility = View.VISIBLE
                }
            }

            override fun onFailure(call: Call<UserExistsResponse>, t: Throwable) {
                progressBar.visibility = View.GONE
                sendOtpButton.isEnabled = true
                sendOtpButton.alpha = 1f
                Toast.makeText(this@LoginActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun sendOtp(phone: String) {
        progressBar.visibility = View.VISIBLE

        // Dim the button while sending OTP
        sendOtpButton.isEnabled = false
        sendOtpButton.alpha = 0.5f

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phone)
            .setTimeout(30L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }


    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            signInWithCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            progressBar.visibility = View.GONE

            // Restore button if failed
            sendOtpButton.isEnabled = true
            sendOtpButton.alpha = 1f

            Toast.makeText(this@LoginActivity, "Failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
        override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
            this@LoginActivity.verificationId = verificationId
            progressBar.visibility = View.GONE

            // Restore button
            sendOtpButton.isEnabled = true
            sendOtpButton.alpha = 1f
            sendOtpButton.visibility = View.GONE

            Toast.makeText(this@LoginActivity, "OTP Sent", Toast.LENGTH_SHORT).show()
            verifyOtpButton.visibility = View.VISIBLE
            otpContainer.visibility = View.VISIBLE
            resendOtpText.visibility = View.VISIBLE

            // Slight delay to prevent UI rendering conflict
            resendOtpText.postDelayed({
                startResendCountdown()
            }, 200)
        }



    }

    private fun verifyOtp() {
        val code = otpFields.joinToString("") { it.text.toString().trim() }
        verifyOtpButton.alpha = 0.5f
        verifyOtpButton.isEnabled = false

        if (code.length != 6) {
            verifyOtpButton.alpha = 1f
            verifyOtpButton.isEnabled = true

            Toast.makeText(this, "Enter valid 6-digit OTP", Toast.LENGTH_SHORT).show()
            return
        }

        val verificationId = this.verificationId ?: run {
            verifyOtpButton.alpha = 1f
            verifyOtpButton.isEnabled = true

            Toast.makeText(this, "Verification ID is null", Toast.LENGTH_SHORT).show()
            return
        }

        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        signInWithCredential(credential)
    }

    private fun signInWithCredential(credential: PhoneAuthCredential) {
        progressBar.visibility = View.VISIBLE
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                progressBar.visibility = View.GONE
                if (task.isSuccessful) {
                    val phone = "+91${phoneEditText.text.toString().trim()}"
                    fetchRoleFromBackend(phone)
                } else {
                    verifyOtpButton.alpha = 1f
                    verifyOtpButton.isEnabled = true
                    Toast.makeText(this, "Login failed:Invalid OTP", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun fetchRoleFromBackend(phone: String) {
        ApiClient.instance.getUserRole(phone).enqueue(object : Callback<UserRoleResponse> {
            override fun onResponse(call: Call<UserRoleResponse>, response: Response<UserRoleResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val role = response.body()!!.role
                    val userId = response.body()!!.userId
                    Log.d("LoginCheck", "Fetched userId = $userId, role = $role")
                    saveUserInfo(role,userId)
                    redirectToHome(role)
                } else {
                    verifyOtpButton.alpha = 1f
                    verifyOtpButton.isEnabled = true

                    Toast.makeText(this@LoginActivity, "Failed to fetch role", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UserRoleResponse>, t: Throwable) {
                verifyOtpButton.alpha = 1f
                verifyOtpButton.isEnabled = true

                Toast.makeText(this@LoginActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun saveUserInfo(role: String, userId: String) {
        getSharedPreferences("UserPrefs", MODE_PRIVATE).edit().apply {
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

    private fun startSmsRetriever() {
        val client = SmsRetriever.getClient(this)
        client.startSmsRetriever()

        smsReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (SmsRetriever.SMS_RETRIEVED_ACTION == intent?.action) {
                    val extras = intent.extras
                    val status = extras?.get(SmsRetriever.EXTRA_STATUS) as? com.google.android.gms.common.api.Status
                    if (status?.statusCode == com.google.android.gms.common.api.CommonStatusCodes.SUCCESS) {
                        val message = extras.get(SmsRetriever.EXTRA_SMS_MESSAGE) as String
                        val otp = extractOtpFromMessage(message)
                        for (i in otp.indices) {
                            otpFields[i].setText(otp[i].toString())
                        }
                        verifyOtp()
                    }
                }
            }
        }

        ContextCompat.registerReceiver(
            this,
            smsReceiver,
            IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    private fun extractOtpFromMessage(message: String): String {
        val pattern = Pattern.compile("\\b\\d{6}\\b")
        val matcher = pattern.matcher(message)
        return if (matcher.find()) matcher.group(0) ?: "" else ""
    }

    private fun setupOtpAutoMove() {
        for (i in otpFields.indices) {
            otpFields[i].addTextChangedListener(object : android.text.TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: android.text.Editable?) {
                    if (s?.length == 1 && i < otpFields.size - 1) {
                        otpFields[i + 1].requestFocus()
                    } else if (s?.isEmpty() == true && i > 0) {
                        otpFields[i - 1].requestFocus()
                    }
                }
            })

            otpFields[i].setOnKeyListener { _, keyCode, event ->
                if (event.action == android.view.KeyEvent.ACTION_DOWN && keyCode == android.view.KeyEvent.KEYCODE_DEL) {
                    if (otpFields[i].text.isEmpty() && i > 0) {
                        otpFields[i - 1].apply {
                            setText("")
                            requestFocus()
                        }
                    }
                }
                false
            }
        }
    }
    private fun startResendCountdown() {
        // Cancel existing timer if any
        if (::countdownTimer.isInitialized) {
            countdownTimer.cancel()
        }

        resendOtpText.isEnabled = false
        resendOtpText.text = "Resend OTP in 30s"
        resendOtpText.alpha = 0.5f

        countdownTimer = object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                resendOtpText.text = "Resend OTP in ${seconds}s"
            }

            override fun onFinish() {
                resendOtpText.alpha = 1f
                resendOtpText.text = "Resend OTP"
                resendOtpText.isEnabled = true
            }
        }

        // Delay the start to allow UI to settle
        resendOtpText.postDelayed({
            countdownTimer.start()
        }, 300) // ← 300ms delay to avoid UI conflict
    }



    private fun hideKeyboard() {
        val view = currentFocus
        if (view != null) {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(smsReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
