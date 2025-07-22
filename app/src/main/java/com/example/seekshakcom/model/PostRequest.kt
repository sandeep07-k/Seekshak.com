package com.example.seekshakcom.model

data class PostRequest(
    val className: String,
    val subject: String,
    val educationBoard: String,
    val fee: String,
    val classSchedule: String,
    val classTiming: String,
    val gender: String,
    val demoClassDate: String,
    val modeOfClass: String,
    val qualification: String,
    val specialReq: String,

    // Location fields
    val latitude: Double,
    val longitude: Double,
    val sublocality: String,
    val area: String,
    val city: String,
    val state: String,
    val country: String
)
