package mikleaka

import mikleaka.data.database.DatabaseFactory
import mikleaka.security.configureSecurity
import mikleaka.routes.authRoutes
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.http.*

fun main() {
    embeddedServer(
        Netty,
        port = System.getenv("PORT")?.toInt() ?: 8080,
        host = System.getenv("HOST") ?: "0.0.0.0",
        module = Application::module
    ).start(wait = true)
}

fun Application.module() {
    // Инициализируем базу данных
    DatabaseFactory.init()
    
    // Устанавливаем JSON сериализацию
    install(ContentNegotiation) {
        json()
    }
    
    // Настраиваем JWT аутентификацию
    configureSecurity()
    
    // Настраиваем маршруты
    routing {
        // Добавляем тестовый эндпоинт для проверки работы сервера
        get("/") {
            call.respond(HttpStatusCode.OK, mapOf("message" to "Сервер запущен успешно!"))
        }
        
        // Основные маршруты для авторизации
        authRoutes()
    }
    
    // Логируем запуск сервера
    println("===========================================================")
    println("Server started on http://0.0.0.0:8080")
    println("===========================================================")
} 