package com.example.seekshakcom

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.seekshakcom.model.EmailRequest
import com.example.seekshakcom.network.ApiResponse
import com.example.seekshakcom.network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EnterEmailActivity : AppCompatActivity() {

    private lateinit var emailInput: EditText
    private lateinit var nextButton: Button
    private var progressDialog: AlertDialog? = null
    private lateinit var backArrow: ImageView

    private val verifyEmailLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val verifiedEmail = result.data?.getStringExtra("verified_email")
            verifiedEmail?.let {
                val resultIntent = Intent()
                resultIntent.putExtra("verified_Email", it)
                setResult(Activity.RESULT_OK, resultIntent)
                finish() // ✅ Return directly to EditProfileActivity
            }
        } else {
            // ⛔ If cancelled or failed, focus email input again
            emailInput.requestFocus()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enter_email)

        emailInput = findViewById(R.id.email_input)
        nextButton = findViewById(R.id.next_button)
        backArrow = findViewById(R.id.back_arrow)

        nextButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            if (!isValidEmail(email)) {
                emailInput.error = "Enter a valid email"
                emailInput.requestFocus() // 👈 Auto focus if invalid
                return@setOnClickListener
            }
            showProgressDialog()
            sendEmailOtp(email)
        }
        // ✅ Restore email if passed back
        val lastEmail = intent.getStringExtra("email")
        lastEmail?.let {
            emailInput.setText(it)
            emailInput.setSelection(it.length) // 👈 put cursor at end
        }

        backArrow.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun sendEmailOtp(email: String) {
        nextButton.isEnabled = false
        val request = EmailRequest(email)

        RetrofitInstance.emailOtpApi.sendOtp(request).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                dismissProgressDialog()
                nextButton.isEnabled = true

                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(this@EnterEmailActivity, "OTP sent to $email", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this@EnterEmailActivity, EmailVerificationActivity::class.java)
                    intent.putExtra("email", email)
                    verifyEmailLauncher.launch(intent)

                } else {
                    Toast.makeText(
                        this@EnterEmailActivity,
                        response.body()?.message ?: "Failed to send OTP",
                        Toast.LENGTH_SHORT
                    ).show()
                    emailInput.requestFocus()
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                dismissProgressDialog()
                nextButton.isEnabled = true
                Toast.makeText(this@EnterEmailActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                emailInput.requestFocus()
            }
        })
    }

    private fun showProgressDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_progress, null)
        progressDialog = AlertDialog.Builder(this)
            .setView(view)
            .setCancelable(false)
            .create()
        progressDialog?.show()
    }

    private fun dismissProgressDialog() {
        progressDialog?.dismiss()
        progressDialog = null
    }
}
