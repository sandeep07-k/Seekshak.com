package com.example.seekshakcom

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.google.android.gms.auth.api.phone.SmsRetriever
import java.util.regex.Pattern

class SmsBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (SmsRetriever.SMS_RETRIEVED_ACTION == intent?.action) {
            val extras = intent.extras
            val status = extras?.get(SmsRetriever.EXTRA_STATUS) as? com.google.android.gms.common.api.Status

            if (status?.statusCode == com.google.android.gms.common.api.CommonStatusCodes.SUCCESS) {
                val message = extras.get(SmsRetriever.EXTRA_SMS_MESSAGE) as? String
                val otp = extractOtp(message)
                Log.d("SmsReceiver", "OTP: $otp")

                // Optional: Show Toast or store in SharedPreferences to access in LoginActivity
                Toast.makeText(context, "OTP received: $otp", Toast.LENGTH_SHORT).show()
                // You can also use LocalBroadcastManager or send a custom broadcast to LoginActivity
            } else {
                Log.e("SmsReceiver", "SMS Retriever failed: ${status?.statusCode}")
            }
        }
    }

    private fun extractOtp(message: String?): String {
        if (message == null) return ""
        val pattern = Pattern.compile("\\b\\d{6}\\b")
        val matcher = pattern.matcher(message)
        return if (matcher.find()) matcher.group(0) ?: "" else ""
    }
}
