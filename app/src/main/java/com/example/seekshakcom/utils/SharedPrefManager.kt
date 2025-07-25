package com.example.seekshakcom.utils

import android.content.Context
import com.example.seekshakcom.model.UserResponse

object SharedPrefManager {
    private const val PREF_NAME = "user_profile"
    private const val KEY_USER_ID = "userId"
    private const val KEY_NAME = "name"
    private const val KEY_PHONE = "phone"

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

        return if (userId != null && name != null && phone != null) {
            UserResponse(userId, name, phone)
        } else null
    }
}
