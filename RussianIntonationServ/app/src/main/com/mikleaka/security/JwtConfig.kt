package mikleaka.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import mikleaka.data.models.User
import java.util.*

/**
 * Конфигурация и утилиты для работы с JWT токенами
 */
object JwtConfig {
    // Читаем секрет из переменной окружения "JWT_SECRET". 
    // Если переменная не установлена (например, при локальном запуске), 
    // используется запасное значение ПО УМОЛЧАНИЮ. 
    // !!! В ПРОДАКШЕНЕ ЗАПАСНОЕ ЗНАЧЕНИЕ ИСПОЛЬЗОВАТЬ НЕЛЬЗЯ - ПЕРЕМЕННАЯ ОКРУЖЕНИЯ ДОЛЖНА БЫТЬ УСТАНОВЛЕНА !!!
    private val SECRET = System.getenv("JWT_SECRET") ?: "default-insecure-fallback-secret-please-set-env-var"
    
    private const val ISSUER = "russian-intonation-app"
    private const val AUDIENCE = "russian-intonation-users"
    const val REALM = "russian-intonation-api"
    
    // Срок действия токена - 7 дней
    private const val EXPIRATION_TIME = 7 * 24 * 60 * 60 * 1000L
    
    /**
     * Создает JWT токен для пользователя
     * @param user Пользователь
     * @return JWT токен
     */
    fun makeToken(user: User): String = JWT.create()
        .withSubject(user.id)
        .withIssuer(ISSUER)
        .withAudience(AUDIENCE)
        .withClaim("username", user.username)
        .withClaim("email", user.email)
        .withExpiresAt(Date(System.currentTimeMillis() + EXPIRATION_TIME))
        .sign(Algorithm.HMAC256(SECRET))
    
    /**
     * Возвращает алгоритм для верификации токена
     * @return Algorithm
     */
    fun getVerifier() = JWT.require(Algorithm.HMAC256(SECRET))
        .withIssuer(ISSUER)
        .withAudience(AUDIENCE)
        .build()
} 