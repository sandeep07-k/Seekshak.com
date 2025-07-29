import android.content.Context

class SharedPreferencesHelper(context: Context) {
    private val prefs = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

    fun saveUserName(name: String) {
        prefs.edit().putString("user_name", name).apply()
    }

    fun getUserName(): String {
        return prefs.getString("user_name", "") ?: ""
    }

    fun savePhoneNumber(phone: String) {
        prefs.edit().putString("phone_number", phone).apply()
    }

    fun getPhoneNumber(): String {
        return prefs.getString("phone_number", "") ?: ""
    }

    fun saveEmail(email: String) {
        prefs.edit().putString("email", email).apply()
    }

    fun getEmail(): String {
        return prefs.getString("email", "") ?: ""
    }
}
