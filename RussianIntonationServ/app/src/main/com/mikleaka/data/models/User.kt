package mikleaka.data.models

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val username: String,
    val email: String,
    val passwordHash: String,
    val isVerified: Boolean = false
)

@Serializable
data class UserCredentials(
    val usernameOrEmail: String,
    val password: String
)

@Serializable
data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String
)

@Serializable
data class VerificationRequest(
    val email: String,
    val code: String
)

@Serializable
data class AuthResponse(
    val token: String,
    val user: User
) 