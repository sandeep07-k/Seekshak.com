package com.example.seekshakcom.network

import LoginResponse
import com.example.seekshakcom.model.GenericResponse
import com.example.seekshakcom.model.ImageRemoveRequest
import com.example.seekshakcom.model.LoginRequest
import com.example.seekshakcom.model.MyPost
import com.example.seekshakcom.model.PostRequest
import com.example.seekshakcom.model.RegisterRequest
import com.example.seekshakcom.model.RegisterResponse
import com.example.seekshakcom.model.TuitionPost
import com.example.seekshakcom.model.UpdateProfileResponse
import com.example.seekshakcom.model.UploadResponse
import com.example.seekshakcom.model.UserExistsResponse
import com.example.seekshakcom.model.UserResponse
import com.example.seekshakcom.model.UserRoleResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

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

    @Multipart
    @POST("/api/image/upload-profile")
    suspend fun uploadProfileImage(
        @Part profileImage: MultipartBody.Part,
        @Part("firebaseUid") firebaseUid: RequestBody
    ): Response<UploadResponse>

    @POST("/api/image/remove-profile")
    fun removeImage(@Body request: ImageRemoveRequest): Call<GenericResponse>

    @PUT("api/user/update")
    fun updateUserProfilePartial(@Body updates: Map<String, String>): Call<UpdateProfileResponse>


    interface TuitionApiService {
        @GET("/api/tuitions/nearby-posts")
        fun getNearbyPosts(
            @Query("lat") lat: Double,
            @Query("lon") lon: Double
        ): Call<List<TuitionPost>>
    }







}




