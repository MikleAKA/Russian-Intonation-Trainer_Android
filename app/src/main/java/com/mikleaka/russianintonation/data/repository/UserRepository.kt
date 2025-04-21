package com.mikleaka.russianintonation.data.repository

import com.mikleaka.russianintonation.RussianIntonationApp
import com.mikleaka.russianintonation.data.api.ApiClient
import com.mikleaka.russianintonation.data.models.*
import com.mikleaka.russianintonation.data.storage.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Репозиторий для работы с пользователями
 */
class UserRepository {
    private var currentUser: User? = null
    private var authToken: String? = null
    
    private val apiService = ApiClient.apiService
    private val userPreferences: UserPreferences = RussianIntonationApp.userPreferences
    
    /**
     * Авторизация пользователя
     * @param usernameOrEmail имя пользователя или email
     * @param password пароль
     * @return Flow с результатом авторизации
     */
    fun login(usernameOrEmail: String, password: String): Flow<Result<User>> = flow {
        try {
            println("UserRepository: попытка авторизации $usernameOrEmail")
            val response = apiService.login(LoginRequest(usernameOrEmail, password))
            
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                println("UserRepository: успешный ответ сервера: $authResponse")
                
                if (authResponse.token.isNotBlank()) {
                    authToken = authResponse.token
                    val user = authResponse.user.toUser()
                    if (user != null) {
                        currentUser = user
                        
                        // Сохраняем данные в SharedPreferences асинхронно
                        userPreferences.saveAuthToken(authToken!!)
                        userPreferences.saveUser(user)
                        
                        println("UserRepository: успешная авторизация, сохранен пользователь $user")
                        emit(Result.success(user))
                    } else {
                        println("UserRepository: ошибка преобразования данных пользователя")
                        emit(Result.failure(Exception("Ошибка преобразования данных пользователя")))
                    }
                } else {
                    println("UserRepository: сервер вернул пустой токен или user-объект")
                    val errorMsg = if (authResponse.token.isBlank()) "Сервер вернул пустой токен" else "Сервер вернул пустой user-объект"
                    emit(Result.failure(Exception(errorMsg)))
                }
            } else {
                // Оставляем упрощенную обработку ошибки, как просил пользователь
                 val errorBody = response.errorBody()?.string()
                 var errorMessage = "Неверные учетные данные" // Значение по умолчанию
                 if (errorBody != null) {
                     try {
                         val jsonElement = Json.parseToJsonElement(errorBody)
                         val errorMsgFromJson = jsonElement.jsonObject["error"]?.jsonPrimitive?.contentOrNull
                         if (!errorMsgFromJson.isNullOrBlank()) {
                             errorMessage = errorMsgFromJson
                         } else { 
                             errorMessage = errorBody 
                         }
                     } catch (e: Exception) {
                         errorMessage = errorBody
                         println("UserRepository: Не удалось распарсить JSON ошибки: $e")
                     }
                 }
                println("UserRepository: ошибка авторизации: $errorMessage, код: ${response.code()}")
                emit(Result.failure(Exception(errorMessage)))
            }
        } catch (e: Exception) {
            println("UserRepository: исключение при авторизации: ${e.message}")
            e.printStackTrace()
            emit(Result.failure(Exception("Ошибка при подключении к серверу: ${e.message}")))
        }
    }

    /**
     * Регистрация нового пользователя
     * @param username имя пользователя
     * @param email электронная почта
     * @param password пароль
     * @return Flow с результатом начала регистрации
     */
    fun register(username: String, email: String, password: String): Flow<Result<RegisterResponse>> = flow {
        try {
            println("UserRepository: отправка запроса на регистрацию: $username, $email")
            val request = RegisterRequest(username, email, password)
            println("UserRepository: запрос сформирован: $request")
            
            val response = apiService.register(request)
            println("UserRepository: получен ответ от сервера, успешный: ${response.isSuccessful}")
            
            if (response.isSuccessful && response.body() != null) {
                val responseBody = response.body()!!
                println("UserRepository: тело ответа: $responseBody")
                emit(Result.success(responseBody))
            } else {
                val errorBody = response.errorBody()?.string()
                println("UserRepository: ошибка регистрации: $errorBody, код: ${response.code()}")
                val errorMessage = errorBody ?: "Ошибка регистрации"
                emit(Result.failure(Exception(errorMessage)))
            }
        } catch (e: Exception) {
            println("UserRepository: исключение при регистрации: ${e.message}")
            e.printStackTrace()
            emit(Result.failure(e))
        }
    }
    
    /**
     * Подтверждение регистрации с кодом верификации
     * @param email электронная почта
     * @param code код подтверждения
     * @return Flow с результатом подтверждения
     */
    fun verifyRegistration(email: String, code: String): Flow<Result<VerificationResponse>> = flow {
        try {
            val response = apiService.verify(VerificationRequest(email, code))
            
            if (response.isSuccessful && response.body() != null) {
                emit(Result.success(response.body()!!))
            } else {
                // Используем парсинг JSON, добавленный ранее
                val errorBody = response.errorBody()?.string()
                var errorMessage = "Ошибка верификации" // Значение по умолчанию
                if (errorBody != null) {
                    try {
                        // Пытаемся распарсить как {"error": "..."}
                        val jsonElement = Json.parseToJsonElement(errorBody)
                        val errorMsgFromJson = jsonElement.jsonObject["error"]?.jsonPrimitive?.contentOrNull
                        if (!errorMsgFromJson.isNullOrBlank()) {
                            errorMessage = errorMsgFromJson
                        } else {
                            errorMessage = errorBody 
                        }
                    } catch (e: Exception) {
                        errorMessage = errorBody
                        println("UserRepository: Не удалось распарсить JSON ошибки: $e")
                    }
                }
                println("UserRepository: Ошибка верификации: $errorMessage, код: ${response.code()}")
                emit(Result.failure(Exception(errorMessage)))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    /**
     * Получение текущего пользователя
     * @return Flow с текущим пользователем или null, если не авторизован
     */
    fun getCurrentUser(): Flow<User?> = flow {
        println("UserRepository: получение текущего пользователя")
        
        // Сначала проверяем кэш в памяти
        if (currentUser != null) {
             println("UserRepository: найден пользователь в кэше $currentUser")
             emit(currentUser)
             return@flow
        }

        // Если нет в кэше, пытаемся загрузить из SharedPreferences асинхронно
        val localUser = userPreferences.getUser()
        if (localUser != null) {
            println("UserRepository: найден локальный пользователь $localUser")
            currentUser = localUser
            emit(localUser)
            return@flow
        }
        
        // Если нет и в SharedPreferences, проверяем наличие токена и запрашиваем с сервера
        val localToken = authToken ?: userPreferences.getAuthToken()
        if (localToken != null) {
            println("UserRepository: найден токен, запрашиваем данные с сервера")
            authToken = localToken
            
            try {
                val response = apiService.getUserProfile()
                if (response.isSuccessful && response.body() != null) {
                    val userDto = response.body()!!
                    val user = userDto.toUser()
                    if (user != null) {
                        currentUser = user
                        userPreferences.saveUser(user)
                        println("UserRepository: получены данные пользователя с сервера $user")
                        emit(user)
                    } else {
                        println("UserRepository: ошибка преобразования данных с сервера")
                        emit(null)
                    }
                } else {
                    println("UserRepository: сервер вернул ошибку ${response.code()}")
                    // Возможно, стоит очистить невалидный токен?
                    // logout() // Подумать, нужно ли это здесь
                    emit(null)
                }
            } catch (e: Exception) {
                println("UserRepository: ошибка при запросе данных с сервера: ${e.message}")
                emit(null)
            }
        } else {
            println("UserRepository: нет данных пользователя и токена")
            emit(null)
        }
    }

    /**
     * Выход из аккаунта
     */
    suspend fun logout() {
        println("UserRepository: выход из аккаунта")
        currentUser = null
        authToken = null
        userPreferences.clearUserData()
    }
    
    /**
     * Обновление прогресса пользователя
     * @param constructionId идентификатор интонационной конструкции
     * @param levelId идентификатор уровня
     * @param score полученный балл
     * @return Flow с обновленным пользователем
     */
    fun updateProgress(constructionId: String, levelId: String, score: Int): Flow<User?> = flow {
        val user = currentUser ?: return@flow emit(null)
        
        val currentProgress = user.progress[constructionId] ?: ConstructionProgress(
            constructionId = constructionId,
            completedLevels = emptyList(),
            averageScore = 0f
        )
        
        // Добавляем уровень в завершенные, если его еще нет
        val completedLevels = if (currentProgress.completedLevels.contains(levelId)) {
            currentProgress.completedLevels
        } else {
            currentProgress.completedLevels + levelId
        }
        
        // Рассчитываем новый средний балл
        val totalScore = currentProgress.averageScore * currentProgress.completedLevels.size + score
        val newAverageScore = totalScore / completedLevels.size
        
        // Создаем обновленный прогресс
        val updatedProgress = currentProgress.copy(
            completedLevels = completedLevels,
            averageScore = newAverageScore
        )
        
        // Обновляем прогресс пользователя
        val updatedUser = user.copy(
            progress = user.progress + (constructionId to updatedProgress)
        )
        
        currentUser = updatedUser
        emit(updatedUser)
    }

    /**
     * Изменение пароля пользователя
     * @param currentPassword текущий пароль
     * @param newPassword новый пароль
     * @return Flow с результатом операции
     */
    fun changePassword(currentPassword: String, newPassword: String): Flow<Result<ChangePasswordResponse>> = flow {
        try {
            // Вызов suspend функции
            val token = authToken ?: userPreferences.getAuthToken()
            if (token == null) {
                println("UserRepository: Попытка смены пароля без токена авторизации")
                emit(Result.failure(Exception("Необходима авторизация")))
                return@flow
            }
            
            println("UserRepository: Отправка запроса на смену пароля")
            val response = apiService.changePassword(ChangePasswordRequest(currentPassword, newPassword))
            
            if (response.isSuccessful && response.body() != null) {
                emit(Result.success(response.body()!!))
            } else {
                // Оставляем упрощенную обработку ошибки
                val errorBody = response.errorBody()?.string()
                var errorMessage = "Ошибка смены пароля" // Значение по умолчанию
                if (errorBody != null) {
                    try {
                        val jsonElement = Json.parseToJsonElement(errorBody)
                        val errorMsgFromJson = jsonElement.jsonObject["error"]?.jsonPrimitive?.contentOrNull
                        errorMessage = if (!errorMsgFromJson.isNullOrBlank()) {
                            errorMsgFromJson
                        } else {
                            errorBody
                        }
                    } catch (e: Exception) {
                        errorMessage = errorBody
                        println("UserRepository: Не удалось распарсить JSON ошибки: $e")
                    }
                }
                emit(Result.failure(Exception(errorMessage)))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    /**
     * Временные мок-данные пользователей
     */
    private val mockUsers = listOf(
        User(
            id = "user1",
            username = "Тестовый пользователь",
            email = "test@example.com",
            progress = mapOf(
                "ik1" to ConstructionProgress(
                    constructionId = "ik1",
                    completedLevels = listOf("level1_1", "level1_2"),
                    averageScore = 80f
                )
            ),
            achievements = listOf(
                Achievement(
                    id = "achievement1",
                    title = "Первые шаги",
                    description = "Пройдите первый уровень любой интонационной конструкции",
                    iconUrl = "https://example.com/icons/achievement1.png",
                    isUnlocked = true,
                    unlockedAt = System.currentTimeMillis() - 86400000 // Вчера
                )
            )
        )
    )
} 