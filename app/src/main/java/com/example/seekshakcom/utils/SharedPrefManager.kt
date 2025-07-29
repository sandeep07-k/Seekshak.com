package com.example.seekshakcom.utils

import android.content.Context
import com.example.seekshakcom.model.UserResponse

object SharedPrefManager {
    private const val PREF_NAME = "user_profile"
    private const val KEY_USER_ID = "userId"
    private const val KEY_NAME = "name"
    private const val KEY_PHONE = "phone"
    private const val KEY_EMAIL = "email"
    private const val KEY_PROFILE_IMAGE = "profile_image_url"

    fun saveUser(context: Context, user: UserResponse) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString(KEY_USER_ID, user.userId)
            putString(KEY_NAME, user.name)
            putString(KEY_PHONE, user.phone)
            putString(KEY_EMAIL, user.email) // nullable email handled
            putString(KEY_PROFILE_IMAGE, user.profileImage)
            apply()
        }
    }

    fun getUser(context: Context): UserResponse? {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val userId = prefs.getString(KEY_USER_ID, null)
        val name = prefs.getString(KEY_NAME, null)
        val phone = prefs.getString(KEY_PHONE, null)
        val email = prefs.getString(KEY_EMAIL, null) // optional
        val profileImage = prefs.getString(KEY_PROFILE_IMAGE, null)

        return if (userId != null && name != null && phone != null) {
            UserResponse(userId, name, phone, email, profileImage)
        } else null
    }

    fun saveImageUrl(context: Context, url: String?) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_PROFILE_IMAGE, url).apply()
    }

    fun getImageUrl(context: Context): String? {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_PROFILE_IMAGE, null)
    }
}
