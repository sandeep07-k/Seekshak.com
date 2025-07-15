package com.example.seekshakcom.model

data class RegisterRequest(
    val role: String,
    val name: String,
    val email: String? = null,
    val phone: String,
    val firebaseToken: String
)
