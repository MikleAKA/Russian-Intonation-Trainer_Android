package com.mikleaka.russianintonation.data.api

import com.mikleaka.russianintonation.RussianIntonationApp
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit

/**
 * Клиент для работы с API
 */
object ApiClient {
    private const val BASE_URL = "http://10.0.2.2:8080/" // Для эмулятора - это localhost на хосте
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private val authInterceptor = Interceptor { chain ->
        val token = runBlocking { 
            RussianIntonationApp.userPreferences.getAuthToken() 
        }
        val request = chain.request()
        
        val authRequest = if (token != null) {
            // Добавляем токен в заголовок, если он существует
            println("ApiClient: Добавление токена авторизации в запрос")
            request.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            println("ApiClient: Запрос без авторизации")
            request
        }
        
        val response = chain.proceed(authRequest)
        
        // Логируем ответ сервера для отладки
        println("ApiClient: Получен ответ от сервера, код: ${response.code}")
        if (!response.isSuccessful) {
            try {
                val errorBody = response.peekBody(Long.MAX_VALUE).string()
                println("ApiClient: Ошибка от сервера: $errorBody")
            } catch (e: Exception) {
                println("ApiClient: Не удалось прочитать тело ошибки: ${e.message}")
            }
        }
        
        response
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(authInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    val apiService: ApiService = retrofit.create(ApiService::class.java)
} 