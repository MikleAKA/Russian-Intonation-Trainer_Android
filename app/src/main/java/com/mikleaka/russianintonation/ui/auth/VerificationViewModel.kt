package com.mikleaka.russianintonation.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mikleaka.russianintonation.data.models.RegisterRequest
import com.mikleaka.russianintonation.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel для экрана подтверждения регистрации
 */
class VerificationViewModel : ViewModel() {
    
    private val userRepository = UserRepository()
    
    private val _uiState = MutableStateFlow(VerificationUiState())
    val uiState: StateFlow<VerificationUiState> = _uiState.asStateFlow()
    
    /**
     * Подтверждение кода верификации
     * @param email электронная почта
     * @param code код подтверждения
     */
    fun verifyCode(email: String, code: String) {
        if (!validateCode(code)) {
            return
        }
        
        _uiState.update { it.copy(isLoading = true, error = null) }
        
        viewModelScope.launch {
            userRepository.verifyRegistration(email, code).collect { result ->
                result.fold(
                    onSuccess = { response ->
                        _uiState.update { 
                            it.copy(isLoading = false, isVerified = true, message = response.message) 
                        }
                    },
                    onFailure = { throwable ->
                        _uiState.update { 
                            it.copy(isLoading = false, error = "Неверный код подтверждения") 
                        }
                    }
                )
            }
        }
    }
    
    /**
     * Повторная отправка кода верификации
     * @param email электронная почта
     */
    fun resendCode(email: String) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        
        viewModelScope.launch {
            // Используем тот же метод регистрации, что запустит процесс повторно
            // и сгенерирует новый код
            userRepository.register("", email, "").collect { result ->
                result.fold(
                    onSuccess = { response ->
                        _uiState.update { 
                            it.copy(
                                isLoading = false, 
                                message = "Новый код отправлен на ваш email"
                            ) 
                        }
                    },
                    onFailure = { throwable ->
                        _uiState.update { 
                            it.copy(isLoading = false, error = throwable.message) 
                        }
                    }
                )
            }
        }
    }
    
    /**
     * Проверка валидности кода подтверждения
     */
    private fun validateCode(code: String): Boolean {
        var isValid = true
        
        _uiState.update {
            it.copy(
                codeError = null,
                error = null
            )
        }
        
        if (code.isBlank()) {
            _uiState.update { it.copy(codeError = "Код подтверждения не может быть пустым") }
            isValid = false
        } else if (code.length != 6 || !code.all { c -> c.isDigit() }) {
            _uiState.update { it.copy(codeError = "Код должен состоять из 6 цифр") }
            isValid = false
        }
        
        return isValid
    }
}

/**
 * Состояние UI экрана подтверждения регистрации
 */
data class VerificationUiState(
    val isLoading: Boolean = false,
    val isVerified: Boolean = false,
    val codeError: String? = null,
    val error: String? = null,
    val message: String? = null
) 