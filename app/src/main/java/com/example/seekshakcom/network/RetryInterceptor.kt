package com.example.seekshakcom.network

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class RetryInterceptor(private val maxRetries: Int = 3) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var attempt = 0
        var response: Response
        var exception: IOException? = null

        while (attempt < maxRetries) {
            try {
                response = chain.proceed(chain.request())
                if (response.isSuccessful) {
                    return response
                }
            } catch (e: IOException) {
                exception = e
            }
            attempt++
        }
        throw exception ?: IOException("Unknown network error")
    }
}
