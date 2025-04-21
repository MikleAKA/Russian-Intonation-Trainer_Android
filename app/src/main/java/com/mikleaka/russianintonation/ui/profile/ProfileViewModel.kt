package com.mikleaka.russianintonation.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mikleaka.russianintonation.data.models.User
import com.mikleaka.russianintonation.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel для экрана профиля
 */
class ProfileViewModel : ViewModel() {
    
    private val userRepository = UserRepository()
    
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
    
    init {
        loadUserData()
    }
    
    /**
     * Загрузка данных пользователя
     */
    private fun loadUserData() {
        _uiState.update { it.copy(isLoading = true) }
        
        viewModelScope.launch {
            println("ProfileViewModel: Загрузка данных пользователя")
            userRepository.getCurrentUser().collect { user ->
                println("ProfileViewModel: Получены данные пользователя: $user")
                _uiState.update { it.copy(user = user, isLoading = false) }
            }
        }
    }
    
    /**
     * Изменение пароля пользователя
     */
    fun changePassword(currentPassword: String, newPassword: String) {
        if (!validatePasswordInput(currentPassword, newPassword)) {
            return
        }
        
        _uiState.update { it.copy(isLoading = true, passwordChangeError = null, passwordChangeSuccess = null) }
        
        viewModelScope.launch {
            userRepository.changePassword(currentPassword, newPassword).collect { result ->
                result.fold(
                    onSuccess = { response ->
                        _uiState.update { 
                            it.copy(
                                isLoading = false, 
                                passwordChangeSuccess = response.message,
                                currentPassword = "",
                                newPassword = "",
                                confirmNewPassword = ""
                            ) 
                        }
                    },
                    onFailure = { error ->
                        _uiState.update { it.copy(isLoading = false, passwordChangeError = error.message) }
                    }
                )
            }
        }
    }
    
    /**
     * Выход из аккаунта
     */
    fun logout() {
        viewModelScope.launch {
            userRepository.logout()
        }
    }
    
    /**
     * Валидация ввода пароля
     */
    private fun validatePasswordInput(currentPassword: String, newPassword: String): Boolean {
        if (currentPassword.isBlank()) {
            _uiState.update { it.copy(currentPasswordError = "Необходимо ввести текущий пароль") }
            return false
        }
        
        if (newPassword.isBlank()) {
            _uiState.update { it.copy(newPasswordError = "Необходимо ввести новый пароль") }
            return false
        }
        
        if (newPassword.length < 6) {
            _uiState.update { it.copy(newPasswordError = "Пароль должен содержать не менее 6 символов") }
            return false
        }
        
        val confirmNewPassword = _uiState.value.confirmNewPassword
        if (newPassword != confirmNewPassword) {
            _uiState.update { it.copy(confirmNewPasswordError = "Пароли не совпадают") }
            return false
        }
        
        // Сбрасываем ошибки
        _uiState.update { 
            it.copy(
                currentPasswordError = null,
                newPasswordError = null,
                confirmNewPasswordError = null
            ) 
        }
        
        return true
    }
    
    /**
     * Обновление текущего пароля в состоянии
     */
    fun updateCurrentPassword(password: String) {
        _uiState.update { it.copy(currentPassword = password, currentPasswordError = null) }
    }
    
    /**
     * Обновление нового пароля в состоянии
     */
    fun updateNewPassword(password: String) {
        _uiState.update { it.copy(newPassword = password, newPasswordError = null) }
    }
    
    /**
     * Обновление подтверждения нового пароля в состоянии
     */
    fun updateConfirmNewPassword(password: String) {
        _uiState.update { it.copy(confirmNewPassword = password, confirmNewPasswordError = null) }
    }
    
    fun clearPasswordChangeSuccess() {
        _uiState.update { it.copy(passwordChangeSuccess = null) }
    }
    
    fun clearPasswordChangeError() {
        _uiState.update { it.copy(passwordChangeError = null) }
    }
}

/**
 * Состояние UI экрана профиля
 */
data class ProfileUiState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmNewPassword: String = "",
    val currentPasswordError: String? = null,
    val newPasswordError: String? = null,
    val confirmNewPasswordError: String? = null,
    val passwordChangeError: String? = null,
    val passwordChangeSuccess: String? = null
) 