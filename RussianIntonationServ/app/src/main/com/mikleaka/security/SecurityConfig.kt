package mikleaka.security

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import mikleaka.data.repository.UserRepository

/**
 * Настраивает JWT аутентификацию
 */
fun Application.configureSecurity() {
    install(Authentication) {
        jwt("auth-jwt") {
            realm = JwtConfig.REALM
            verifier(JwtConfig.getVerifier())
            
            validate { credential ->
                val userId = credential.payload.subject ?: return@validate null
                val username = credential.payload.getClaim("username").asString() ?: return@validate null
                
                // Проверяем существование пользователя в базе данных
                val userRepository = UserRepository()
                val user = userRepository.getUserById(userId)
                
                if (user != null && user.username == username && user.isVerified) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
            
            challenge { _, _ ->
                call.respond(mapOf("error" to "Неверный или истекший токен. Пожалуйста, войдите снова."))
            }
        }
    }
} 