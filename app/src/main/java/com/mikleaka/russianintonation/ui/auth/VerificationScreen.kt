package com.mikleaka.russianintonation.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mikleaka.russianintonation.ui.components.LoadingDialog
import com.mikleaka.russianintonation.ui.components.RiButton
import com.mikleaka.russianintonation.ui.components.RiTextField

/**
 * Экран подтверждения регистрации
 */
@Composable
fun VerificationScreen(
    email: String,
    verificationCode: String?,
    onVerificationSuccess: () -> Unit,
    onBackToLogin: () -> Unit,
    viewModel: VerificationViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var code by remember { mutableStateOf("") }
    
    LaunchedEffect(uiState.isVerified) {
        if (uiState.isVerified) {
            onVerificationSuccess()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Подтверждение регистрации",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Пожалуйста, введите код подтверждения,\nотправленный на адрес $email",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        RiTextField(
            value = code,
            onValueChange = { if (it.length <= 6) code = it },
            label = "Код подтверждения",
            isError = uiState.codeError != null,
            errorText = uiState.codeError ?: "",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
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
            text = "Подтвердить",
            onClick = { viewModel.verifyCode(email, code) },
            isLoading = uiState.isLoading
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        RiButton(
            text = "Назад к входу",
            onClick = onBackToLogin,
            enabled = !uiState.isLoading
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        TextButton(
            onClick = { viewModel.resendCode(email) },
            enabled = !uiState.isLoading
        ) {
            Text("Отправить код повторно")
        }
    }
    
    if (uiState.isLoading) {
        LoadingDialog()
    }
} 