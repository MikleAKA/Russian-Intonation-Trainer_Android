package com.mikleaka.russianintonation.data.models

/**
 * Запрос для входа в систему
 */
data class LoginRequest(
    val usernameOrEmail: String,
    val password: String
)

/**
 * Запрос для регистрации
 */
data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String
)

/**
 * Ответ от сервера при успешной регистрации
 */
data class RegisterResponse(
    val message: String,
    val user_id: String,
    val verification_code: String? = null
)

/**
 * Запрос для подтверждения регистрации
 */
data class VerificationRequest(
    val email: String,
    val code: String
)

/**
 * Ответ на запрос верификации
 */
data class VerificationResponse(
    val message: String
)

/**
 * Ответ после успешной авторизации
 */
data class AuthResponse(
    val token: String,
    val user: UserDto
)

/**
 * DTO модель пользователя, получаемая с сервера
 */
data class UserDto(
    val id: String,
    val username: String,
    val email: String,
    val isVerified: Boolean
)

/**
 * Преобразование UserDto в User
 */
fun UserDto?.toUser(): User? {
    if (this == null) {
        println("AuthModels: toUser() получил null")
        return null
    }
    
    // Проверяем, что id и username не пустые
    if (id.isBlank() || username.isBlank()) {
        println("AuthModels: toUser() получил некорректные данные: id=$id, username=$username")
        return null
    }
    
    println("AuthModels: успешное преобразование UserDto в User: id=$id, username=$username")
    return User(
        id = this.id,
        username = this.username,
        email = this.email
    )
}

/**
 * Запрос для смены пароля пользователя
 */
data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)

/**
 * Ответ на запрос смены пароля
 */
data class ChangePasswordResponse(
    val message: String,
    val success: Boolean
) 