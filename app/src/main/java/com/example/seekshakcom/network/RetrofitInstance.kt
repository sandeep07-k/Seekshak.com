package com.example.seekshakcom.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private const val BASE_URL = "https://seekshak-backend.onrender.com" // ✅ your backend

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // 🔹 Location-specific API service
    val locationApi: LocationApiService by lazy {
        retrofit.create(LocationApiService::class.java)
    }

    val emailOtpApi: EmailOtpApi by lazy {
        retrofit.create(EmailOtpApi::class.java)
    }
}
