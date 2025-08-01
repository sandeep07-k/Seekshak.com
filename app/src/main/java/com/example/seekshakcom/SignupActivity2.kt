package com.example.seekshakcom

import android.content.*
import android.os.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.seekshakcom.model.RegisterRequest
import com.example.seekshakcom.model.RegisterResponse
import com.example.seekshakcom.network.ApiClient
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

class SignupActivity2 : AppCompatActivity() {

    private lateinit var otpFields: List<EditText>
    private lateinit var verifyOtpButton: Button
    private lateinit var resendOtpButton: TextView
    private lateinit var backPage: ImageView
    private lateinit var auth: FirebaseAuth
    private lateinit var smsReceiver: BroadcastReceiver
    private lateinit var countdownTimer: CountDownTimer
    private lateinit var editNumber: ImageView

    private var verificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    private lateinit var role: String
    private lateinit var name: String
    private lateinit var phone: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup2)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = ContextCompat.getColor(this, R.color.soft_blue)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true

        auth = FirebaseAuth.getInstance()
        verifyOtpButton = findViewById(R.id.button_verify_otp)
        resendOtpButton = findViewById(R.id.button_resend_otp)
        backPage = findViewById(R.id.goToBackPage)
        editNumber = findViewById(R.id.edit_number)




        otpFields = listOf(
            findViewById(R.id.otp1),
            findViewById(R.id.otp2),
            findViewById(R.id.otp3),
            findViewById(R.id.otp4),
            findViewById(R.id.otp5),
            findViewById(R.id.otp6)
        )



        role = intent.getStringExtra("role") ?: ""
        name = intent.getStringExtra("name") ?: ""
        phone = intent.getStringExtra("phone") ?: ""
        verificationId = intent.getStringExtra("verificationId")
        resendToken = intent.getParcelableExtra("resendToken")

        verifyOtpButton.isEnabled = false
        findViewById<TextView>(R.id.subtitle).text = "We sent a 6-digit code to $phone"

        setupOtpAutoMove()
        setupVerifyButtonWatcher()
        startSmsRetriever()
        startResendCountdown()

        editNumber.setOnClickListener {
            finish()
        }

        verifyOtpButton.setOnClickListener {
            hideKeyboard()
            verifyOtpButton.alpha = 0.5f

            val code = otpFields.joinToString("") { it.text.toString().trim() }
            if (verificationId != null && code.length == 6) {
                val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
                signInWithPhoneAuthCredential(credential)
            } else {
                verifyOtpButton.alpha = 1f
                verifyOtpButton.isEnabled = true
                Toast.makeText(this, "Enter valid 6-digit OTP", Toast.LENGTH_SHORT).show()
            }
        }

        resendOtpButton.setOnClickListener {
            resendOtp()
        }

        backPage.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
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
                    verifyOtpButton.isEnabled = true
                    verifyOtpButton.alpha = 1f
//                    verifyOtpButton.isEnabled = otpFields.all { it.text.toString().trim().length == 1 }
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

    private fun setupVerifyButtonWatcher() {

        verifyOtpButton.isEnabled = true
        verifyOtpButton.alpha = 1f


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
                        verifyOtpButton.performClick()
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

    private fun resendOtp() {
        hideKeyboard()

        resendOtpButton.isEnabled = false
        resendOtpButton.alpha = 0.7f
        resendOtpButton.text = "resending code"

        val builder = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phone)
            .setTimeout(30L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    signInWithPhoneAuthCredential(credential)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    resendOtpButton.isEnabled = false
                    resendOtpButton.alpha = 0.7f
                    resendOtpButton.text = "Resend code"
                    Toast.makeText(this@SignupActivity2, "No internet: ${e.message}", Toast.LENGTH_LONG).show()
                }

                override fun onCodeSent(newVerificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                    verificationId = newVerificationId
                    resendToken = token
                    clearOtpFields()
                    startResendCountdown()
                    startSmsRetriever()
                    resendOtpButton.isEnabled = false
                    resendOtpButton.alpha = 0.7f
                    resendOtpButton.text = "Resend code"

                    Toast.makeText(this@SignupActivity2, "OTP resent successfully", Toast.LENGTH_SHORT).show()
                }
            })

        // ✅ Use existing resendToken if available
        if (resendToken != null) {
            builder.setForceResendingToken(resendToken!!)
        }

        PhoneAuthProvider.verifyPhoneNumber(builder.build())
    }





    private fun clearOtpFields() {
        otpFields.forEach { it.setText("") }
        otpFields[0].requestFocus()
    }

    private fun startResendCountdown() {
        if (::countdownTimer.isInitialized) countdownTimer.cancel()

        resendOtpButton.isEnabled = false
        resendOtpButton.alpha = 0.7f
        resendOtpButton.text = "Resend code in 30s"

        countdownTimer = object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                resendOtpButton.text = "Resend code in ${seconds}s"
            }

            override fun onFinish() {
                resendOtpButton.isEnabled = true
                resendOtpButton.text = "Resend code"
                resendOtpButton.alpha = 1f

            }
        }

        resendOtpButton.postDelayed({
            countdownTimer.start()
        }, 100)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        val loadingDialog = AlertDialog.Builder(this)
            .setView(R.layout.dialog_loading_otp)
            .setCancelable(false)
            .create()
        loadingDialog.show()

        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    sendToBackend()
                } else {

                    verifyOtpButton.alpha = 1f
                    verifyOtpButton.isEnabled = true
                    Toast.makeText(this, "Invalid OTP", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun sendToBackend() {
        val currentUser = auth.currentUser
        currentUser?.getIdToken(true)?.addOnSuccessListener { tokenResult ->
            val firebaseToken = tokenResult.token ?: ""

            val registerRequest = RegisterRequest(
                role = role,
                name = name,
                phone = phone,
                firebaseToken = firebaseToken
            )

            ApiClient.instance.registerUser(registerRequest)
                .enqueue(object : Callback<RegisterResponse> {
                    override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                        if (response.isSuccessful) {
                            val userId = response.body()?.userId
                            getSharedPreferences("UserPrefs", MODE_PRIVATE).edit().apply {
                                putString("user_id", userId)
                                putString("firebase_token", firebaseToken)
                                putString("user_role", role)
//                                putBoolean("isLoggedIn", true)
                                apply()
                            }
                            Toast.makeText(this@SignupActivity2, "Signup successful. Please Login", Toast.LENGTH_SHORT).show()
                            // 🔄 Redirect to SignupActivity (or LoginActivity if you meant that)
                            val intent = Intent(this@SignupActivity2, LoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)

                            finish()
//                            redirectToHome(role)
                        } else {
                            verifyOtpButton.alpha = 1f
                            verifyOtpButton.isEnabled = true
                            Toast.makeText(this@SignupActivity2, "Failed: ${response.errorBody()?.string()}", Toast.LENGTH_LONG).show()
                        }
                    }

                    override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {

                        verifyOtpButton.alpha = 1f
                        verifyOtpButton.isEnabled = true
                        Toast.makeText(this@SignupActivity2, "Error: ${t.message}", Toast.LENGTH_LONG).show()
                    }
                })
        } ?: run {

            verifyOtpButton.alpha = 1f
            verifyOtpButton.isEnabled = true
            Toast.makeText(this, "Failed to get Firebase token", Toast.LENGTH_SHORT).show()
        }
    }

//    private fun redirectToHome(role: String) {
//        val intent = when (role) {
//            "student" -> Intent(this, StudentHomeActivity::class.java)
//            "educator" -> Intent(this, TutorHomeActivity::class.java)
//            "institute" -> Intent(this, InstituteHomeActivity::class.java)
//            else -> Intent(this, StudentHomeActivity::class.java)
//        }
//        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//        startActivity(intent)
//    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(smsReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
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