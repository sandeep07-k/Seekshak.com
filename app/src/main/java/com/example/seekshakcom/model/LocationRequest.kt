package com.example.seekshakcom.model

data class LocationRequest(
    val latitude: Double,
    val longitude: Double,
    val area: String,
    val city: String,
    val state: String,
    val country: String
)
