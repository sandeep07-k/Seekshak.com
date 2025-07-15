package com.example.seekshakcom.model

data class LoginRequest(
    val phone: String,
    val role: String,
    val firebaseToken: String
)
