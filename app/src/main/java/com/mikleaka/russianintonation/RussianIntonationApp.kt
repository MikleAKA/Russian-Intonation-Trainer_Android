package com.mikleaka.russianintonation

import android.app.Application
import com.mikleaka.russianintonation.data.storage.UserPreferences

/**
 * Класс приложения
 */
class RussianIntonationApp : Application() {
    
    companion object {
        private lateinit var instance: RussianIntonationApp
        
        fun getInstance(): RussianIntonationApp {
            return instance
        }
        
        lateinit var userPreferences: UserPreferences
            private set
    }
    
    override fun onCreate() {
        super.onCreate()
        instance = this
        userPreferences = UserPreferences(applicationContext)
    }
} 