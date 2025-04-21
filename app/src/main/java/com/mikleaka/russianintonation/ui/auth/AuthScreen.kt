package com.mikleaka.russianintonation.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mikleaka.russianintonation.ui.components.LoadingDialog
import com.mikleaka.russianintonation.ui.components.RiButton
import com.mikleaka.russianintonation.ui.components.RiTextField

/**
 * Экран авторизации
 */
@Composable
fun AuthScreen(
    onNavigateToHome: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    println("AuthScreen: состояние - isLoading=${uiState.isLoading}, needsVerification=${uiState.needsVerification}, isAuthenticated=${uiState.isAuthenticated}")
    
    var isLoginMode by remember { mutableStateOf(true) }
    var username by remember { mutableStateOf("") }
    var usernameOrEmail by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    
    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) {
            println("AuthScreen: переход на главный экран")
            onNavigateToHome()
        }
    }
    
    if (uiState.needsVerification && uiState.registeredEmail != null) {
        println("AuthScreen: переход на экран верификации с email=${uiState.registeredEmail}, код=${uiState.registrationResponse?.verification_code}")
        VerificationScreen(
            email = uiState.registeredEmail!!,
            verificationCode = uiState.registrationResponse?.verification_code,
            onVerificationSuccess = {
                // После успешной верификации переходим к экрану входа
                println("AuthScreen: верификация успешна, возврат к экрану входа")
                viewModel.resetVerificationState()
                isLoginMode = true
            },
            onBackToLogin = {
                // Возврат к экрану входа
                println("AuthScreen: возврат к экрану входа без верификации")
                viewModel.resetVerificationState()
                isLoginMode = true
            }
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Русская Интонация",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Приложение для обучения фразовой интонации русского языка",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = if (isLoginMode) "Вход в аккаунт" else "Регистрация",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (isLoginMode) {
                // Поля для входа
                RiTextField(
                    value = usernameOrEmail,
                    onValueChange = { usernameOrEmail = it },
                    label = "Имя пользователя или Email",
                    isError = uiState.usernameOrEmailError != null,
                    errorText = uiState.usernameOrEmailError ?: ""
                )
            } else {
                // Поля для регистрации
                RiTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = "Имя пользователя",
                    isError = uiState.usernameError != null,
                    errorText = uiState.usernameError ?: ""
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                RiTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email",
                    isError = uiState.emailError != null,
                    errorText = uiState.emailError ?: ""
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            RiTextField(
                value = password,
                onValueChange = { password = it },
                label = "Пароль",
                isError = uiState.passwordError != null,
                errorText = uiState.passwordError ?: "",
                isPassword = true
            )
            
            if (uiState.error != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = uiState.error ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            RiButton(
                text = if (isLoginMode) "Войти" else "Зарегистрироваться",
                onClick = {
                    if (isLoginMode) {
                        viewModel.login(usernameOrEmail, password)
                    } else {
                        viewModel.register(username, email, password)
                    }
                },
                isLoading = uiState.isLoading
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            RiButton(
                text = if (isLoginMode) "Нет аккаунта? Зарегистрироваться" else "Уже есть аккаунт? Войти",
                onClick = { 
                    isLoginMode = !isLoginMode
                    // Очищаем поля при переключении между экранами
                    if (isLoginMode) {
                        username = ""
                        email = ""
                    } else {
                        usernameOrEmail = ""
                    }
                    password = ""
                },
                enabled = !uiState.isLoading
            )
        }
        
        if (uiState.isLoading) {
            LoadingDialog()
        }
    }
} 