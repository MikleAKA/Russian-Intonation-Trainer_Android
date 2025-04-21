package com.mikleaka.russianintonation.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mikleaka.russianintonation.data.models.RegisterResponse
import com.mikleaka.russianintonation.data.models.User
import com.mikleaka.russianintonation.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel для экрана авторизации
 */
class AuthViewModel : ViewModel() {

    private val userRepository = UserRepository()
    
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()
    
    init {
        checkInitialAuthState()
    }
    
    /**
     * Проверка начального состояния авторизации при запуске
     */
    private fun checkInitialAuthState() {
        viewModelScope.launch {
            // Проверяем, есть ли уже пользователь в репозитории (мог остаться с предыдущего сеанса)
            // userRepository.getCurrentUser() вернет null, если токена нет или он невалидный
            userRepository.getCurrentUser().collect { user ->
                if (user != null) {
                    _uiState.update { it.copy(isAuthenticated = true) }
                }
                 // Если user == null, оставляем isAuthenticated = false (значение по умолчанию)
            }
        }
    }
    
    /**
     * Авторизация пользователя
     * @param usernameOrEmail имя пользователя или email
     * @param password пароль
     */
    fun login(usernameOrEmail: String, password: String) {
        if (!validateLoginInputs(usernameOrEmail, password)) {
            return
        }
        
        _uiState.update { it.copy(isLoading = true, error = null) }
        
        viewModelScope.launch {
            userRepository.login(usernameOrEmail, password).collect { result ->
                result.fold(
                    onSuccess = {
                        _uiState.update { it.copy(isLoading = false, isAuthenticated = true) }
                    },
                    onFailure = {
                        _uiState.update { it.copy(isLoading = false, error = "Неверные учетные данные") }
                    }
                )
            }
        }
    }
    
    /**
     * Начало регистрации нового пользователя
     * @param username имя пользователя
     * @param email электронная почта
     * @param password пароль
     */
    fun register(username: String, email: String, password: String) {
        println("AuthViewModel: начата регистрация: $username, $email")
        
        if (!validateRegisterInputs(username, email, password)) {
            println("AuthViewModel: ошибка валидации данных")
            return
        }
        
        _uiState.update { it.copy(isLoading = true, error = null) }
        println("AuthViewModel: данные валидны, отправляем запрос")
        
        viewModelScope.launch {
            userRepository.register(username, email, password).collect { result ->
                println("AuthViewModel: получен результат регистрации: $result")
                
                result.fold(
                    onSuccess = { response ->
                        println("AuthViewModel: регистрация успешна, response: $response")
                        _uiState.update { 
                            it.copy(
                                isLoading = false, 
                                registrationResponse = response,
                                needsVerification = true,
                                registeredEmail = email
                            ) 
                        }
                        println("AuthViewModel: состояние обновлено, needsVerification=${_uiState.value.needsVerification}")
                    },
                    onFailure = { throwable ->
                        println("AuthViewModel: ошибка регистрации: ${throwable.message}")
                        _uiState.update { it.copy(isLoading = false, error = "Пользователь с таким именем или email'ом уже существует") }
                    }
                )
            }
        }
    }
    
    /**
     * Проверка валидности полей для авторизации
     */
    private fun validateLoginInputs(usernameOrEmail: String, password: String): Boolean {
        var isValid = true
        
        _uiState.update {
            it.copy(
                usernameOrEmailError = null,
                passwordError = null,
                error = null
            )
        }
        
        if (usernameOrEmail.isBlank()) {
            _uiState.update { it.copy(usernameOrEmailError = "Имя пользователя или Email не может быть пустым") }
            isValid = false
        }
        
        if (password.isBlank()) {
            _uiState.update { it.copy(passwordError = "Пароль не может быть пустым") }
            isValid = false
        } else if (password.length < 6) {
            _uiState.update { it.copy(passwordError = "Пароль должен содержать минимум 6 символов") }
            isValid = false
        }
        
        return isValid
    }
    
    /**
     * Проверка валидности полей для регистрации
     */
    private fun validateRegisterInputs(username: String, email: String, password: String): Boolean {
        var isValid = true
        
        _uiState.update {
            it.copy(
                usernameError = null,
                emailError = null,
                passwordError = null,
                error = null
            )
        }
        
        if (username.isBlank()) {
            _uiState.update { it.copy(usernameError = "Имя пользователя не может быть пустым") }
            isValid = false
        } else if (username.length < 3) {
            _uiState.update { it.copy(usernameError = "Имя пользователя не может быть меньше 3 символов") }
            isValid = false
        } else if (username.length > 25) {
            _uiState.update { it.copy(usernameError = "Имя пользователя не может быть больше 25 символов") }
            isValid = false
        }
        
        if (email.isBlank()) {
            _uiState.update { it.copy(emailError = "Email не может быть пустым") }
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.update { it.copy(emailError = "Неверный формат email") }
            isValid = false
        } else if (email.length > 35) {
            _uiState.update { it.copy(emailError = "Некорректный email") }
            isValid = false
        }
        
        if (password.isBlank()) {
            _uiState.update { it.copy(passwordError = "Пароль не может быть пустым") }
            isValid = false
        } else if (password.length < 6) {
            _uiState.update { it.copy(passwordError = "Пароль должен содержать минимум 6 символов") }
            isValid = false
        }
        
        return isValid
    }
    
    /**
     * Сброс состояния верификации (например, при возврате на экран входа)
     */
    fun resetVerificationState() {
        _uiState.update { 
            it.copy(needsVerification = false, registrationResponse = null, registeredEmail = null) 
        }
    }
}

/**
 * Состояние UI экрана авторизации
 */
data class AuthUiState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val needsVerification: Boolean = false,
    val registrationResponse: RegisterResponse? = null,
    val registeredEmail: String? = null,
    val usernameError: String? = null,
    val emailError: String? = null,
    val usernameOrEmailError: String? = null,
    val passwordError: String? = null,
    val error: String? = null
) 