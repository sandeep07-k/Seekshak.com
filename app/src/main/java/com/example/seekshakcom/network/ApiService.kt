package com.example.seekshakcom.network

import LoginResponse
import com.example.seekshakcom.model.*
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // 🔐 Authentication
    @POST("/api/auth/signup")
    fun registerUser(@Body request: RegisterRequest): Call<RegisterResponse>

    @POST("/api/auth/login")
    fun loginUser(@Body request: LoginRequest): Call<LoginResponse>

    @GET("/api/auth/get-role")
    fun getUserRole(@Query("phone") phone: String): Call<UserRoleResponse>

    @GET("/api/auth/check-user")
    fun checkUserExists(@Query("phone") phone: String): Call<UserExistsResponse>

    // 📝 Add Post
    @POST("/api/posts")
    suspend fun addPost(
        @Header("Authorization") token: String,
        @Body postRequest: PostRequest
    ): Response<ApiResponse>

    // 📦 My Posts
    @GET("api/posts/my-posts")
    fun getMyPosts(
        @Query("userId") userId: String,
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): Call<List<MyPost>>

    @PUT("api/posts/update-post/{id}")
    fun updatePost(@Path("id") postId: String, @Body post: PostRequest): Call<ApiResponse>

    // 🔁 Reactivate Post
    @POST("api/posts/reactivate-post/{postId}")
    fun reactivatePost(@Path("postId") postId: String): Call<ApiResponse>

    // ✅ Mark as Filled
    @POST("api/posts/mark-filled/{postId}")
    fun markPostFilled(@Path("postId") postId: String): Call<ApiResponse>

    // ❌ Delete Post
    @DELETE("api/posts/delete-post/{postId}")
    fun deletePost(@Path("postId") postId: String): Call<ApiResponse>

    @GET("/api/user/profile")
    suspend fun getUserProfile(
        @Header("Authorization") token: String
    ): Response<UserResponse>






}

