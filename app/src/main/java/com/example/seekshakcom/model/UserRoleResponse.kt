
package com.example.seekshakcom.model

data class UserRoleResponse(
    val success: Boolean,
    val role: String,
    val userId: String,
    val message: String?
)
