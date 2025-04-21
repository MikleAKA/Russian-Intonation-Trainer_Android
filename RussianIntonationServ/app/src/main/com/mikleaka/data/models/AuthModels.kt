package mikleaka.data.models

import kotlinx.serialization.Serializable

/**
 * Запрос на смену пароля
 */
@Serializable
data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)

/**
 * Ответ на запрос смены пароля
 */
@Serializable
data class ChangePasswordResponse(
    val message: String,
    val success: Boolean
) 