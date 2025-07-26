package com.example.seekshakcom.utils

import android.content.Context
import com.example.seekshakcom.model.UserResponse

object SharedPrefManager {
    private const val PREF_NAME = "user_profile"
    private const val KEY_USER_ID = "userId"
    private const val KEY_NAME = "name"
    private const val KEY_PHONE = "phone"
    private const val KEY_PROFILE_IMAGE = "profile_image_url"



    fun saveUser(context: Context, user: UserResponse) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString(KEY_USER_ID, user.userId)
            putString(KEY_NAME, user.name)
            putString(KEY_PHONE, user.phone)
            apply()
        }
    }

    fun getUser(context: Context): UserResponse? {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val userId = prefs.getString(KEY_USER_ID, null)
        val name = prefs.getString(KEY_NAME, null)
        val phone = prefs.getString(KEY_PHONE, null)
        val profileImage = prefs.getString(KEY_PROFILE_IMAGE, null) // ✅ Add this

        return if (userId != null && name != null && phone != null) {
            UserResponse(userId, name, phone, profileImage) // ✅ Pass it here
        } else null
    }




    fun saveImageUrl(context: Context, url: String?) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_PROFILE_IMAGE, url).apply()
    }

    fun getImageUrl(context: Context): String? {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_PROFILE_IMAGE, null)
    }




}
