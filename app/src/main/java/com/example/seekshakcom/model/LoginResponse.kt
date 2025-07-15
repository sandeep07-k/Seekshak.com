data class LoginResponse(
    val message: String,
    val userId: String,
    val role: String,
    val name: String? = null,
    val email: String? = null
)


