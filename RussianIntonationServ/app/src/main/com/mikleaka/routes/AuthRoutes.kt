package mikleaka.routes

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import mikleaka.data.models.UserCredentials
import mikleaka.data.models.RegisterRequest
import mikleaka.data.models.VerificationRequest
import mikleaka.data.models.AuthResponse
import mikleaka.data.models.ChangePasswordRequest
import mikleaka.data.models.ChangePasswordResponse
import mikleaka.data.repository.UserRepository
import mikleaka.security.JwtConfig
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.http.*

fun Route.authRoutes() {
    val userRepository = UserRepository()
    
    // Начало регистрации - получение данных и отправка кода подтверждения
    post("/register") {
        println("Received registration request")
        val registerRequest = call.receive<RegisterRequest>()
        println("Registration data: ${registerRequest.username}, ${registerRequest.email}")
        
        // Валидация данных
        if (registerRequest.username.length < 3) {
            call.respond(mapOf("error" to "Username must be at least 3 characters long"))
            return@post
        }
        
        if (registerRequest.password.length < 6) {
            call.respond(mapOf("error" to "Password must be at least 6 characters long"))
            return@post
        }
        
        // Регистрируем пользователя и отправляем код верификации
        val result = userRepository.registerUser(
            username = registerRequest.username,
            email = registerRequest.email,
            password = registerRequest.password
        )
        
        if (result == null) {
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "User with this username or email already exists"))
            return@post
        }
        
        val (user, code) = result

        // Отладка и тестирования
        println("Verification code for ${registerRequest.email}: $code")
        
        call.respond(mapOf(
            "message" to "Confirmation code sent to your email",
            "user_id" to user.id,
            "verification_code" to code
        ))
    }
    
    // Подтверждение регистрации с кодом верификации
    post("/verify") {
        val verificationRequest = call.receive<VerificationRequest>()
        
        val isVerified = userRepository.verifyUser(
            email = verificationRequest.email,
            code = verificationRequest.code
        )
        
        if (!isVerified) {
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid verification code or email"))
            return@post
        }
        
        call.respond(mapOf("message" to "Registration completed successfully! You can now log in."))
    }
    
    // Вход в систему
    post("/login") {
        val credentials = call.receive<UserCredentials>()
        
        println("Login request for: ${credentials.usernameOrEmail}")
        
        val user = userRepository.authenticate(
            usernameOrEmail = credentials.usernameOrEmail,
            password = credentials.password
        )
        
        println("Authentication result: ${user != null}")
        
        if (user == null) {
            println("Authentication failed for ${credentials.usernameOrEmail}")
            call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid credentials or account not verified"))
            return@post
        }

        // Создаем JWT токен
        val token = JwtConfig.makeToken(user)
        println("Token created for user ${user.username} (ID: ${user.id})")
        
        println("Sending response: AuthResponse(token=${token.take(15)}..., user=$user)")
        call.respond(AuthResponse(token = token, user = user))
    }
    
    // Эндпоинт для проверки статуса аккаунта
    get("/account-status") {
        val email = call.parameters["email"] ?: run {
            call.respond(mapOf("error" to "Email is required"))
            return@get
        }
        
        val user = userRepository.getUserById(email)
        
        if (user == null) {
            call.respond(mapOf("exists" to false))
            return@get
        }
        
        call.respond(mapOf(
            "exists" to true,
            "verified" to user.isVerified
        ))
    }
    
    // Получение профиля текущего пользователя (защищенный маршрут)
    authenticate("auth-jwt") {
        get("/user/profile") {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.subject
            
            if (userId == null) {
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid token"))
                return@get
            }
            
            val user = userRepository.getUserById(userId)
            
            if (user == null) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "User not found"))
                return@get
            }
            
            // Не отправляем хеш пароля клиенту
            call.respond(user.copy(passwordHash = ""))
        }
        
        // Смена пароля
        post("/change-password") {
            val principal = call.principal<JWTPrincipal>() // Эта проверка теперь избыточна, но можно оставить
            val userId = principal?.subject // Получаем ID из стандартного claim 'sub'
            
            if (userId == null) {
                // Это не должно произойти, если аутентификация прошла
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Failed to get user ID from token"))
                return@post
            }
            
            println("Password change request for user $userId")
            
            val request = call.receive<ChangePasswordRequest>()
            
            val success = userRepository.changePassword(
                userId = userId,
                currentPassword = request.currentPassword,
                newPassword = request.newPassword
            )
            
            if (success) {
                println("Password successfully changed for user $userId")
                call.respond(ChangePasswordResponse(message = "Password changed successfully", success = true))
            } else {
                println("Error changing password for user $userId")
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid current password"))
            }
        }
    }
} 