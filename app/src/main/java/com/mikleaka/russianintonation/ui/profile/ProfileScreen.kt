package com.mikleaka.russianintonation.ui.profile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mikleaka.russianintonation.ui.components.LoadingDialog
import com.mikleaka.russianintonation.ui.components.RiButton
import com.mikleaka.russianintonation.ui.components.RiTextField
import com.mikleaka.russianintonation.ui.components.RiTopAppBar

/**
 * Экран профиля пользователя
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(uiState.passwordChangeSuccess) {
        uiState.passwordChangeSuccess?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short
            )
            viewModel.clearPasswordChangeSuccess()
        }
    }
    
    LaunchedEffect(uiState.passwordChangeError) {
        uiState.passwordChangeError?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Long,
                withDismissAction = true
            )
            viewModel.clearPasswordChangeError()
        }
    }
    
    Scaffold(
        topBar = {
            RiTopAppBar(
                title = "Профиль",
                onBackClick = onNavigateBack
            )
        },
        snackbarHost = { 
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(16.dp)
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            LoadingDialog()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Информация о пользователе
                UserInfoSection(uiState)
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Раздел смены пароля
                PasswordChangeSection(uiState, viewModel)
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Кнопка выхода из аккаунта
                Button(
                    onClick = {
                        viewModel.logout()
                        onLogout()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("Выйти из аккаунта")
                }
            }
        }
    }
}

/**
 * Раздел с информацией о пользователе
 */
@Composable
private fun UserInfoSection(uiState: ProfileUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Информация о пользователе",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            uiState.user?.let { user ->
                // Имя пользователя
                InfoRow(
                    icon = Icons.Default.Person,
                    label = "Имя пользователя",
                    value = user.username
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Email
                InfoRow(
                    icon = Icons.Default.Email,
                    label = "Email",
                    value = user.email
                )
            } ?: run {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Информация о пользователе недоступна")
                }
            }
        }
    }
}

/**
 * Строка с информацией пользователя
 */
@Composable
private fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

/**
 * Раздел смены пароля
 */
@Composable
private fun PasswordChangeSection(
    uiState: ProfileUiState,
    viewModel: ProfileViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Сменить пароль",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Текущий пароль
            RiTextField(
                value = uiState.currentPassword,
                onValueChange = { viewModel.updateCurrentPassword(it) },
                label = "Текущий пароль",
                isPassword = true,
                isError = uiState.currentPasswordError != null,
                errorText = uiState.currentPasswordError ?: ""
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Новый пароль
            RiTextField(
                value = uiState.newPassword,
                onValueChange = { viewModel.updateNewPassword(it) },
                label = "Новый пароль",
                isPassword = true,
                isError = uiState.newPasswordError != null,
                errorText = uiState.newPasswordError ?: ""
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Подтверждение нового пароля
            RiTextField(
                value = uiState.confirmNewPassword,
                onValueChange = { viewModel.updateConfirmNewPassword(it) },
                label = "Подтвердите новый пароль",
                isPassword = true,
                isError = uiState.confirmNewPasswordError != null,
                errorText = uiState.confirmNewPasswordError ?: ""
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Кнопка смены пароля
            RiButton(
                text = "Сменить пароль",
                onClick = { 
                    viewModel.changePassword(uiState.currentPassword, uiState.newPassword)
                },
                isLoading = uiState.isLoading
            )
        }
    }
} 