package com.example.seekshakcom.model

data class MyPostResponse(
    val success: Boolean,
    val count: Int,
    val data: List<MyPost>
)

