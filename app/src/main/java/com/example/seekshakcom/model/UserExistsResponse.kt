package com.example.seekshakcom.model

data class UserExistsResponse(
    val exists: Boolean,
    val message: String? = null  // <- Add this line
)
