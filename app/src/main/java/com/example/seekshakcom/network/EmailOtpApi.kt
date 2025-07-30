package com.example.seekshakcom.network

import com.example.seekshakcom.model.*
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface EmailOtpApi {
    @POST("/api/email/send-email-otp")
    fun sendOtp(@Body request: EmailRequest): Call<ApiResponse>

    @POST("/api/email/verify-email-otp")
    fun verifyOtp(@Body request: OtpVerificationRequest): Call<ApiResponse>
}
