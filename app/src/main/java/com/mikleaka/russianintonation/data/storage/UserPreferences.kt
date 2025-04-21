package com.mikleaka.russianintonation.data.storage

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.mikleaka.russianintonation.data.models.User
import com.mikleaka.russianintonation.data.models.UserDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Класс для хранения данных пользователя в SharedPreferences
 */
class UserPreferences(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        PREF_NAME, Context.MODE_PRIVATE
    )
    
    private val gson = Gson()
    
    /**
     * Сохранение токена авторизации
     */
    suspend fun saveAuthToken(token: String) = withContext(Dispatchers.IO) {
        println("UserPreferences: Сохранение токена авторизации: ${token.take(10)}...")
        sharedPreferences.edit().putString(KEY_AUTH_TOKEN, token).apply()
    }
    
    /**
     * Получение токена авторизации
     */
    suspend fun getAuthToken(): String? = withContext(Dispatchers.IO) {
        val token = sharedPreferences.getString(KEY_AUTH_TOKEN, null)
        println("UserPreferences: Получение токена авторизации: ${token?.take(10) ?: "null"}...")
        token
    }
    
    /**
     * Сохранение данных пользователя
     */
    suspend fun saveUser(user: User) = withContext(Dispatchers.IO) {
        val userJson = gson.toJson(user)
        sharedPreferences.edit().putString(KEY_USER, userJson).apply()
    }
    
    /**
     * Получение данных пользователя
     */
    suspend fun getUser(): User? = withContext(Dispatchers.IO) {
        val userJson = sharedPreferences.getString(KEY_USER, null)
        if (userJson == null) {
            null
        } else {
            try {
                gson.fromJson(userJson, User::class.java)
            } catch (e: Exception) {
                println("UserPreferences: Ошибка парсинга User JSON: $e")
                null
            }
        }
    }
    
    /**
     * Очистка данных пользователя при выходе
     */
    suspend fun clearUserData() = withContext(Dispatchers.IO) {
        sharedPreferences.edit()
            .remove(KEY_AUTH_TOKEN)
            .remove(KEY_USER)
            .apply()
    }
    
    companion object {
        private const val PREF_NAME = "user_preferences"
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_USER = "user"
    }
} 