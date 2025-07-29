package com.example.seekshakcom.model


data class UserResponse(
    val userId: String,
    val name: String,
    val phone: String,
    val email: String?,
    val profileImage: String?
)
