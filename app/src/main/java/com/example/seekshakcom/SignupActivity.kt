package com.example.seekshakcom

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
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

class SignupActivity : AppCompatActivity() {

    private lateinit var nameEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var sendOtpButton: Button
    private lateinit var loginRedirectText: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var termsCheckBox: CheckBox

    private lateinit var btnStudent: Button
    private lateinit var btnTeacher: Button
    private lateinit var btnInstitute: Button

    private lateinit var auth: FirebaseAuth
    private var name = ""
    private var phone = ""
    private var selectedRole: String = ""

    private val otpResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val otpMessage = result.data?.getStringExtra("otp_status")
            if (!otpMessage.isNullOrEmpty()) {
                Toast.makeText(this, otpMessage, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true

        nameEditText = findViewById(R.id.editTextName)
        phoneEditText = findViewById(R.id.editTextMobile)
        sendOtpButton = findViewById(R.id.button_send_otp)
        progressBar = findViewById(R.id.progressBar)
        loginRedirectText = findViewById(R.id.textView_login_redirect)
        termsCheckBox = findViewById(R.id.checkbox_terms)

        btnStudent = findViewById(R.id.btnStudent)
        btnTeacher = findViewById(R.id.btnTeacher)
        btnInstitute = findViewById(R.id.btnInstitute)

        auth = FirebaseAuth.getInstance()

        btnStudent.setOnClickListener { updateRole("student") }
        btnTeacher.setOnClickListener { updateRole("educator") }
        btnInstitute.setOnClickListener { updateRole("institute") }

        sendOtpButton.setOnClickListener {
            resetFieldErrors()

            if (selectedRole.isEmpty()) {
                Toast.makeText(this, "Please select a role", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            name = nameEditText.text.toString().trim()
            val rawPhone = phoneEditText.text.toString().trim()
            phone = if (rawPhone.startsWith("+91")) rawPhone else "+91$rawPhone"

            // === Validation ===
            if (name.isEmpty()) {
                nameEditText.error = "Please enter your name"
                nameEditText.requestFocus()
                return@setOnClickListener
            }



            if (phoneEditText.text.toString().trim().isEmpty()) {
                phoneEditText.error = "Please enter your mobile number"
                phoneEditText.requestFocus()
                return@setOnClickListener
            }

            if (phone.length != 13) {
                phoneEditText.error = "Phone number must be 10 digits"
                phoneEditText.requestFocus()
                return@setOnClickListener
            }

            if (!termsCheckBox.isChecked) {
                Toast.makeText(this, "Please agree to the terms and conditions", Toast.LENGTH_SHORT).show()
                termsCheckBox.requestFocus()
                return@setOnClickListener
            }

            hideKeyboard()
            sendOtpButton.isEnabled = false
            checkUserExists(phone)
        }


        loginRedirectText.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            hideKeyboard()
            finish()
        }

        // ✅ Hide keyboard when tapping outside
        findViewById<View>(R.id.signup_root_view)?.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                hideKeyboard()
            }
            false
        }
    }

    private fun updateRole(role: String) {
        selectedRole = role

        fun setButtonState(button: Button, isSelected: Boolean) {
            button.backgroundTintList = getColorStateList(if (isSelected) R.color.darkorange else R.color.azureblue)
            button.setTextColor(getColor(if (isSelected) R.color.black else R.color.white))
        }

        setButtonState(btnStudent, role == "student")
        setButtonState(btnTeacher, role == "educator")
        setButtonState(btnInstitute, role == "institute")
    }

    private fun resetFieldErrors() {

        nameEditText.error = null
        phoneEditText.error = null

    }

    private fun hideKeyboard() {
        val view = currentFocus
        if (view != null) {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    private fun checkUserExists(phone: String) {
        progressBar.visibility = View.VISIBLE
        sendOtpButton.isEnabled = false
        sendOtpButton.alpha = 0.5f

        ApiClient.instance.checkUserExists(phone).enqueue(object : Callback<UserExistsResponse> {
            override fun onResponse(call: Call<UserExistsResponse>, response: Response<UserExistsResponse>) {


                val body = response.body()
                if (body?.exists == true) {
                    Toast.makeText(this@SignupActivity, body.message ?: "User already exists", Toast.LENGTH_LONG).show()
                    loginRedirectText.visibility = View.VISIBLE
                    progressBar.visibility = View.GONE
                    sendOtpButton.isEnabled = true
                    sendOtpButton.alpha = 1f
                } else {
                    sendOtpToPhone()
                }
            }

            override fun onFailure(call: Call<UserExistsResponse>, t: Throwable) {

                Toast.makeText(this@SignupActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                progressBar.visibility = View.GONE
                sendOtpButton.isEnabled = true
                sendOtpButton.alpha = 1f
            }
        })
    }

    private fun sendOtpToPhone() {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phone)
            .setTimeout(30L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {}

                override fun onVerificationFailed(e: FirebaseException) {
                    progressBar.visibility = View.GONE
                    sendOtpButton.isEnabled = true
                    sendOtpButton.alpha = 1f
                    Toast.makeText(this@SignupActivity, "OTP sending failed: ${e.message}", Toast.LENGTH_LONG).show()
                }

                override fun onCodeSent(id: String, token: PhoneAuthProvider.ForceResendingToken) {
                    sendOtpButton.isEnabled = true
                    sendOtpButton.alpha = 1f
                    progressBar.visibility = View.GONE
                    Toast.makeText(this@SignupActivity, "OTP Sent", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this@SignupActivity, SignupActivity2::class.java)
                    intent.putExtra("role", selectedRole)
                    intent.putExtra("name", name)
//                    intent.putExtra("email", email)
                    intent.putExtra("phone", phone)
                    intent.putExtra("verificationId", id)
                    otpResultLauncher.launch(intent)
                }
            })
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }
}
