package com.example.seekshakcom.model

import java.io.Serializable

data class LocationRequest(
    val latitude: Double,
    val longitude: Double,
    val area: String?,
    val city: String?,
    val state: String?,
    val country: String?,
    val sublocality: String? = null
) : Serializable
