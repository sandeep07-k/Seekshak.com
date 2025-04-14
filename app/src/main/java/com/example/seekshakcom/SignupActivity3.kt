package com.example.seekshakcom

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.seekshakcom.model.RegisterRequest
import com.example.seekshakcom.model.RegisterResponse
import com.example.seekshakcom.network.ApiClient
import com.google.firebase.auth.FirebaseAuth
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignupActivity3 : AppCompatActivity() {

    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var termsCheckBox: CheckBox
    private lateinit var registerButton: Button
    private lateinit var progressBar: ProgressBar

    private lateinit var role: String
    private lateinit var name: String
    private lateinit var email: String
    private lateinit var phone: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup3)

        passwordEditText = findViewById(R.id.editText_password)
        confirmPasswordEditText = findViewById(R.id.editText_confirm_password)
        termsCheckBox = findViewById(R.id.checkbox_terms)
        registerButton = findViewById(R.id.button_register)
        progressBar = findViewById(R.id.progressBar)

        role = intent.getStringExtra("role") ?: ""
        name = intent.getStringExtra("name") ?: ""
        email = intent.getStringExtra("email") ?: ""
        phone = intent.getStringExtra("phone") ?: ""

        registerButton.setOnClickListener {
            val password = passwordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()

            if (!termsCheckBox.isChecked) {
                Toast.makeText(this, "Please agree to the terms and conditions", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val strongPasswordPattern = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{6,}")
            if (!password.matches(strongPasswordPattern)) {
                Toast.makeText(this, "Password must be at least 6 characters with upper, lower, and a digit", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            progressBar.visibility = View.VISIBLE
            registerButton.isEnabled = false

            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser == null) {
                Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
                progressBar.visibility = View.GONE
                registerButton.isEnabled = true
                return@setOnClickListener
            }

            currentUser.getIdToken(true).addOnCompleteListener { tokenTask ->
                if (tokenTask.isSuccessful) {
                    val firebaseToken = tokenTask.result?.token ?: ""

                    val registerRequest = RegisterRequest(
                        role = role,
                        name = name,
                        email = email,
                        phone = phone,
                        password = password,
                        firebaseToken = firebaseToken
                    )

                    ApiClient.instance.registerUser(registerRequest).enqueue(object : Callback<RegisterResponse> {
                        override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                            progressBar.visibility = View.GONE
                            registerButton.isEnabled = true
                            if (response.isSuccessful) {
                                val userId = response.body()?.userId

                                val sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
                                sharedPreferences.edit().apply {
                                    putString("user_id", userId)
                                    putString("user_role", role)
                                    putString("user_name", name)
                                    putString("user_email", email)
                                    apply()
                                }

                                Toast.makeText(this@SignupActivity3, "Registration successful: $userId", Toast.LENGTH_LONG).show()
                                startActivity(Intent(this@SignupActivity3, LoginActivity::class.java))
                                finish()
                            } else {
                                val errorBody = response.errorBody()?.string()
                                Toast.makeText(this@SignupActivity3, "Failed: $errorBody", Toast.LENGTH_LONG).show()
                            }
                        }

                        override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                            progressBar.visibility = View.GONE
                            registerButton.isEnabled = true
                            Toast.makeText(this@SignupActivity3, "Error: ${t.message}", Toast.LENGTH_LONG).show()
                        }
                    })
                } else {
                    progressBar.visibility = View.GONE
                    registerButton.isEnabled = true
                    Toast.makeText(this, "Failed to get Firebase token", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
