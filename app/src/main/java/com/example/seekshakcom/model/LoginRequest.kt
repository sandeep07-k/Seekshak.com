package com.example.seekshakcom.model

data class LoginRequest(

    val phone: String? = null,
    val email: String? = null,
    val userId: String? = null,
    val password: String,
    val role: String,

)

