package com.example.seekshakcom.network

import com.example.seekshakcom.model.GenericResponse
import com.example.seekshakcom.model.LocationRequest
import retrofit2.Response


import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface LocationApiService {
    @POST("/api/location/update") // confirm URL from you
    suspend fun updateLocation(
        @Body locationRequest: LocationRequest,
        @Header("Authorization") token: String
    ): Response<GenericResponse>
}
