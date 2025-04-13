package com.example.seekshakcom.model

data class RegisterRequest(
    val role: String,
    val name: String,
    val email: String?,
    val phone: String,
    val password: String,
    val firebaseToken: String
)
