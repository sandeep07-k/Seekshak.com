package com.example.seekshakcom.network

import com.example.seekshakcom.model.*
import com.example.seekshakcom.model.RegisterResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    @POST("/api/auth/signup")
    fun registerUser(@Body request: RegisterRequest): Call<RegisterResponse>

    @POST("/api/auth/login")
    fun loginUser(@Body request: LoginRequest): Call<LoginResponse>

    @GET("api/auth/check-user")
    fun checkUserExists(
        @Query("phone") phone: String,
        @Query("email") email: String
    ): Call<UserExistsResponse>


}
