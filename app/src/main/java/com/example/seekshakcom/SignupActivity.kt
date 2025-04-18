package com.example.seekshakcom

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
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
    private lateinit var emailEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var sendOtpButton: Button
    private lateinit var loginRedirectText: TextView
    private lateinit var progressBar: ProgressBar

    private lateinit var btnStudent: Button
    private lateinit var btnTeacher: Button
    private lateinit var btnInstitute: Button

    private lateinit var auth: FirebaseAuth
    private var role = ""
    private var name = ""
    private var email = ""
    private var phone = ""
    private var selectedRole: String = "Null"

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


        nameEditText = findViewById(R.id.editTextName)
        emailEditText = findViewById(R.id.editTextEmail)
        phoneEditText = findViewById(R.id.editTextMobile)
        sendOtpButton = findViewById(R.id.button_send_otp)
        loginRedirectText = findViewById(R.id.textView_login_redirect)


        btnStudent = findViewById(R.id.btnStudent)
        btnTeacher = findViewById(R.id.btnTeacher)
        btnInstitute = findViewById(R.id.btnInstitute)

        auth = FirebaseAuth.getInstance()


        btnStudent.setOnClickListener { updateRole("Student") }
        btnTeacher.setOnClickListener { updateRole("Teacher") }
        btnInstitute.setOnClickListener { updateRole("Institute") }

        sendOtpButton.setOnClickListener {
            if (role.isEmpty()) {
                Toast.makeText(this, "Please select a role", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            name = nameEditText.text.toString().trim()
            email = emailEditText.text.toString().trim()
            val rawPhone = phoneEditText.text.toString().trim()
            phone = if (rawPhone.startsWith("+91")) rawPhone else "+91$rawPhone"

            if (name.isEmpty()) {
                Toast.makeText(this, "Please fill the name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (email.isNotEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (phone.isEmpty() || phone.length != 13) {
                Toast.makeText(this, "Please enter a valid phone number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            sendOtpButton.isEnabled = false // disable while processing
            checkUserExists(phone, email)
        }

        loginRedirectText.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
    private fun updateRole(role: String) {
        selectedRole = role

        fun setButtonState(button: Button, isSelected: Boolean) {
            button.backgroundTintList = getColorStateList(if (isSelected) R.color.darkorange else R.color.azureblue)
            button.setTextColor(getColor(if (isSelected) R.color.black else R.color.white))
        }

        setButtonState(btnStudent, role == "Student")
        setButtonState(btnTeacher, role == "Teacher")
        setButtonState(btnInstitute, role == "Institute")
    }


    private fun checkUserExists(phone: String, email: String) {
        progressBar.visibility = View.VISIBLE

        ApiClient.instance.checkUserExists(phone, email).enqueue(object : Callback<UserExistsResponse> {
            override fun onResponse(call: Call<UserExistsResponse>, response: Response<UserExistsResponse>) {
                sendOtpButton.isEnabled = true

                val body = response.body()
                if (body == null) {
                    Toast.makeText(this@SignupActivity, "Unexpected server response", Toast.LENGTH_SHORT).show()
                    return
                }

                val userExists = body.exists
                val message = body.message ?: "No response"

                if (userExists) {
                    Toast.makeText(this@SignupActivity, message, Toast.LENGTH_LONG).show()
                    loginRedirectText.visibility = View.VISIBLE
                } else {
                    sendOtpToPhone()
                }
            }

            override fun onFailure(call: Call<UserExistsResponse>, t: Throwable) {
                progressBar.visibility = View.GONE
                sendOtpButton.isEnabled = true
                Toast.makeText(this@SignupActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun sendOtpToPhone() {


        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    // Optional: Handle auto-verification
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    progressBar.visibility = View.GONE
                    sendOtpButton.isEnabled = true
                    Toast.makeText(this@SignupActivity, "OTP sending failed: ${e.message}", Toast.LENGTH_LONG).show()
                }

                override fun onCodeSent(id: String, token: PhoneAuthProvider.ForceResendingToken) {

                    sendOtpButton.isEnabled = true
                    Toast.makeText(this@SignupActivity, "OTP Sent", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this@SignupActivity, SignupActivity2::class.java)
                    intent.putExtra("role", role)
                    intent.putExtra("name", name)
                    intent.putExtra("email", email)
                    intent.putExtra("phone", phone)
                    intent.putExtra("verificationId", id)
                    otpResultLauncher.launch(intent)
                }
            })
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }
}
