data class LoginResponse(
    val message: String,
    val userId: String,
    val role: String,
    val token: String,  // ⬅️ required for authorization
    val name: String? = null,
    val email: String? = null
)
