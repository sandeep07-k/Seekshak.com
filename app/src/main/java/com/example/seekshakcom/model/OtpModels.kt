package com.example.seekshakcom.model

data class EmailRequest(val email: String)
data class OtpVerificationRequest(val email: String, val otp: String)

