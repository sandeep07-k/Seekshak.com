package com.example.seekshakcom

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.util.Patterns
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.seekshakcom.model.LoginRequest
import com.example.seekshakcom.model.LoginResponse
import com.example.seekshakcom.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var btnStudent: Button
    private lateinit var btnTeacher: Button
    private lateinit var btnInstitute: Button
    private lateinit var loginIdentifier: EditText
    private lateinit var loginPassword: EditText
    private lateinit var loginButton: Button
    private lateinit var passwordToggle: ImageView
    private lateinit var progressBar: ProgressBar
    private lateinit var goToSignup: TextView
    private lateinit var forgotPasswordText: TextView

    private var selectedRole: String = "Null"
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // 1. Allow layout to draw behind system bars
        WindowCompat.setDecorFitsSystemWindows(window, false)
        // 2. Make status bar transparent
        Color.TRANSPARENT.also { window.statusBarColor = it }
        // 3. Optional: Change status bar icon color (dark icons = true)
        val insetsController = WindowInsetsControllerCompat(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = true

        // Initialize views
        btnStudent = findViewById(R.id.btn_student)
        btnTeacher = findViewById(R.id.btn_teacher)
        btnInstitute = findViewById(R.id.btn_institute)
        loginIdentifier = findViewById(R.id.loginIdentifier)
        loginPassword = findViewById(R.id.loginPassword)
        loginButton = findViewById(R.id.loginButton)
        passwordToggle = findViewById(R.id.passwordToggle)
        progressBar = findViewById(R.id.progressBar)
        goToSignup = findViewById(R.id.goToSignup)
        forgotPasswordText = findViewById(R.id.forgotPasswordText)


        // Role button click handlers
        btnStudent.setOnClickListener { updateRole("student") }
        btnTeacher.setOnClickListener { updateRole("educator") }
        btnInstitute.setOnClickListener { updateRole("institute") }


        // Password toggle logic
        passwordToggle.setImageResource(R.drawable.ic_visibility_off)
        passwordToggle.setOnClickListener { togglePasswordVisibility() }
//        setInitialRoleUI()


        // Login
        loginButton.setOnClickListener {
            val identifier = loginIdentifier.text.toString().trim()
            val password = loginPassword.text.toString().trim()

            if (identifier.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!isValidIdentifier(identifier)) {
                Toast.makeText(this, "Invalid identifier", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (selectedRole == "Null") {
                Toast.makeText(this, "Please Choose Role", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            processLogin(selectedRole, identifier, password)
        }

        // Auto-login
        val prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        if (prefs.getBoolean("isLoggedIn", false)) {
            redirectBasedOnRole(prefs.getString("role", "Student") ?: "Student")
        }

        // Sign up
        goToSignup.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }

        // Forgot password
        forgotPasswordText.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

    }

    private fun updateRole(role: String) {
        selectedRole = role

        fun setButtonState(button: Button, isSelected: Boolean) {
            button.backgroundTintList = getColorStateList(if (isSelected) R.color.orange else R.color.azureblue)
            button.setTextColor(getColor(if (isSelected) R.color.black else R.color.white))
        }

        setButtonState(btnStudent, role == "student")
        setButtonState(btnTeacher, role == "educator")
        setButtonState(btnInstitute, role == "institute")
    }


    private fun togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible
        if (isPasswordVisible) {
            loginPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            passwordToggle.setImageResource(R.drawable.ic_visibility)
        } else {
            loginPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            passwordToggle.setImageResource(R.drawable.ic_visibility_off)
        }
        loginPassword.setSelection(loginPassword.text.length)
    }

    private fun isValidIdentifier(identifier: String): Boolean {
        return when {
            identifier.matches(Regex("^\\d{10}$")) -> true
            identifier.matches(Regex("^\\+\\d+$")) -> true
            Patterns.EMAIL_ADDRESS.matcher(identifier).matches() -> true
            identifier.isNotEmpty() -> true
            else -> false
        }
    }

    private fun processLogin(role: String, identifier: String, password: String) {
        val formattedIdentifier = when {
            identifier.matches(Regex("^\\d{10}$")) -> "+91$identifier"
            identifier.matches(Regex("^\\+\\d+$")) -> identifier
            Patterns.EMAIL_ADDRESS.matcher(identifier).matches() -> identifier
            else -> identifier
        }

        loginUser(role, formattedIdentifier, password)
    }

    private fun loginUser(role: String, identifier: String, password: String) {
        showLoading(true)

        val request = when {
            identifier.matches(Regex("^\\d{10}$")) -> LoginRequest(role = role, phone = "+91$identifier", password = password)
            identifier.matches(Regex("^\\+\\d+$")) -> LoginRequest(role = role, phone = identifier, password = password)
            identifier.contains("@") -> LoginRequest(email = identifier, role = role, password = password)
            else -> LoginRequest(role = role, userId = identifier, password = password)
        }

        ApiClient.instance.loginUser(request).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                showLoading(false)
                if (response.isSuccessful && response.body() != null) {
                    val loginResponse = response.body()!!
                    saveUserInfo(loginResponse.userId, loginResponse.role)
                    redirectBasedOnRole(loginResponse.role)
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Login failed"
                    Toast.makeText(this@LoginActivity, errorMsg, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                showLoading(false)
                Toast.makeText(this@LoginActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun saveUserInfo(userId: String, role: String) {
        val prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString("userId", userId)
            putString("role", role)
            putBoolean("isLoggedIn", true)
            apply()
        }
    }

    private fun redirectBasedOnRole(role: String) {
        val intent = when (role) {
            "student" -> Intent(this, StudentHomeActivity::class.java)
            "educator" -> Intent(this, TutorHomeActivity::class.java)
            "institute" -> Intent(this, InstituteHomeActivity::class.java)
            else -> Intent(this, StudentHomeActivity::class.java)
        }

        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        loginButton.isEnabled = !isLoading
    }
}