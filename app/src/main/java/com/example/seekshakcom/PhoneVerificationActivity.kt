package com.example.seekshakcom

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.seekshakcom.model.UserExistsResponse
import com.example.seekshakcom.network.ApiClient
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.TimeUnit

class PhoneVerificationActivity : AppCompatActivity() {

    private lateinit var mobileEditText: AutoCompleteTextView
    private lateinit var nextButton: Button
    private lateinit var backArrow: ImageView
    private lateinit var auth: FirebaseAuth
    private var progressDialog: AlertDialog? = null

    private var lastVerificationId: String? = null
    private var lastSentNumber: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private lateinit var otpLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone_verification)

        mobileEditText = findViewById(R.id.mobile_no)
        nextButton = findViewById(R.id.next_button)
        backArrow = findViewById(R.id.back_arrow)
        auth = FirebaseAuth.getInstance()

        val existingNumber = intent.getStringExtra("existingNumber")?.replace("+91", "")?.trim()
        if (!existingNumber.isNullOrEmpty()) {
            mobileEditText.setText(existingNumber)
        }

        mobileEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                mobileEditText.clearFocus()
                hideKeyboard()
                true
            } else false
        }

        backArrow.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
            dismissProgressDialog()
        }

        otpLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val verifiedPhone = result.data?.getStringExtra("verifiedPhoneNumber")
                val returnIntent = Intent()
                returnIntent.putExtra("verifiedPhoneNumber", verifiedPhone)
                setResult(RESULT_OK, returnIntent)
                finish()
            }
        }

        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // Auto verification case
            }

            override fun onVerificationFailed(e: FirebaseException) {
                dismissProgressDialog()
                Toast.makeText(this@PhoneVerificationActivity, "Failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }

            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                dismissProgressDialog()
                lastVerificationId = verificationId
                lastSentNumber = "+91${mobileEditText.text.toString().trim()}"
                resendToken = token

                val intent = Intent(this@PhoneVerificationActivity, OtpVerificationActivity::class.java).apply {
                    putExtra("verificationId", verificationId)
                    putExtra("phoneNumber", lastSentNumber)
                    putExtra("resendToken", token)
                }
                otpLauncher.launch(intent)
            }
        }

        nextButton.setOnClickListener {
            val number = mobileEditText.text.toString().trim()

            if (number.length != 10) {
                Toast.makeText(this, "Enter a valid 10-digit number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val fullNumber = "+91$number"

            if (fullNumber == lastSentNumber && lastVerificationId != null) {
                val intent = Intent(this@PhoneVerificationActivity, OtpVerificationActivity::class.java).apply {
                    putExtra("verificationId", lastVerificationId)
                    putExtra("phoneNumber", lastSentNumber)
                    resendToken?.let { putExtra("resendToken", it) }
                }
                otpLauncher.launch(intent)
                return@setOnClickListener
            }

            showProgressDialog()
            checkUserExists(number)
        }
    }

    private fun checkUserExists(phone: String) {
        nextButton.isEnabled = false
        nextButton.alpha = 0.5f

        ApiClient.instance.checkUserExists(phone).enqueue(object : Callback<UserExistsResponse> {
            override fun onResponse(call: Call<UserExistsResponse>, response: Response<UserExistsResponse>) {
                nextButton.isEnabled = true
                nextButton.alpha = 1f

                val body = response.body()
                if (body?.exists == true) {
                    dismissProgressDialog()
                    Toast.makeText(this@PhoneVerificationActivity, "This number is already in use", Toast.LENGTH_LONG).show()
                } else {
                    startVerification("+91$phone")
                }
            }

            override fun onFailure(call: Call<UserExistsResponse>, t: Throwable) {
                Toast.makeText(this@PhoneVerificationActivity, "No internet: ${t.message}", Toast.LENGTH_SHORT).show()
                nextButton.isEnabled = true
                nextButton.alpha = 1f
                dismissProgressDialog()
            }
        })
    }

    private fun startVerification(phone: String) {
        lastSentNumber = phone
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phone)
            .setTimeout(30L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
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
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(mobileEditText.windowToken, 0)
    }

    override fun onResume() {
        super.onResume()
        dismissProgressDialog()
        nextButton.isEnabled = true
        nextButton.alpha = 1f
    }
}
