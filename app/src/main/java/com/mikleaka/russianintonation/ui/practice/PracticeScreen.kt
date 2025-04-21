package com.mikleaka.russianintonation.ui.practice

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mikleaka.russianintonation.ui.components.LoadingDialog
import com.mikleaka.russianintonation.ui.components.RiButton
import com.mikleaka.russianintonation.ui.components.RiTopAppBar
import com.mikleaka.russianintonation.ui.components.RiTopAppBarWithProfile

/**
 * Экран практики произношения
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeScreen(
    constructionId: String,
    levelId: String,
    onNavigateBack: () -> Unit,
    onNavigateToResults: (String) -> Unit,
    onNavigateToProfile: () -> Unit,
    viewModel: PracticeViewModel = viewModel(factory = PracticeViewModelFactory(constructionId, levelId))
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    // Состояние разрешений
    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    
    var showPermissionDialog by remember { mutableStateOf(false) }
    
    // Лаунчер для запроса разрешений
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasAudioPermission = isGranted
        if (!isGranted) {
            showPermissionDialog = true
        }
    }
    
    // Запрашиваем разрешение при первом открытии экрана
    LaunchedEffect(Unit) {
        if (!hasAudioPermission) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }
    
    // При уходе с экрана останавливаем воспроизведение/запись
    DisposableEffect(key1 = viewModel) {
        onDispose {
            if (uiState.isPlayingReference) {
                viewModel.stopPlayingReference()
            }
            if (uiState.isPlayingRecording) {
                viewModel.stopPlayingRecording()
            }
            if (uiState.isRecording) {
                viewModel.stopRecording()
            }
        }
    }
    
    Scaffold(
        topBar = {
            RiTopAppBarWithProfile(
                title = uiState.level?.title ?: "Загрузка...",
                onBackClick = onNavigateBack,
                onProfileClick = onNavigateToProfile
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (uiState.isLoading) {
                LoadingDialog()
            } else {
                uiState.level?.let { level ->
                    // Фраза для произнесения
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        shape = MaterialTheme.shapes.medium,
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Произнесите фразу:",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = level.phrase,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { 
                                        if (uiState.isPlayingReference) {
                                            viewModel.stopPlayingReference()
                                        } else {
                                            viewModel.playReferenceAudio()
                                        }
                                    },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(
                                        imageVector = if (uiState.isPlayingReference) Icons.Default.Stop else Icons.Default.PlayArrow,
                                        contentDescription = if (uiState.isPlayingReference) "Остановить" else "Прослушать",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                
                                if (uiState.isPlayingReference) {
                                    Text(
                                        text = "Воспроизведение...",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                } else {
                                    Text(
                                        text = "Нажмите, чтобы прослушать",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Инструкции
                    Text(
                        text = if (!hasAudioPermission) {
                            "Необходимо разрешение на запись аудио"
                        } else if (uiState.isRecording) {
                            "Говорите сейчас..."
                        } else {
                            "Нажмите на кнопку микрофона, чтобы начать запись"
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Кнопка записи
                    FloatingActionButton(
                        onClick = { 
                            if (hasAudioPermission) {
                                if (uiState.isRecording) {
                                    viewModel.stopRecording()
                                } else {
                                    viewModel.startRecording()
                                }
                            } else {
                                // Если нет разрешения, запрашиваем его
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        },
                        modifier = Modifier.size(72.dp),
                        containerColor = if (uiState.isRecording) Color.Red else MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    ) {
                        Icon(
                            imageVector = if (uiState.isRecording) Icons.Default.Stop else Icons.Default.Mic,
                            contentDescription = if (uiState.isRecording) "Остановить запись" else "Начать запись",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Кнопка анализа
                    if (uiState.recordingFile != null && !uiState.isRecording) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { 
                                        if (uiState.isPlayingRecording) {
                                            viewModel.stopPlayingRecording()
                                        } else {
                                            viewModel.playRecordedAudio()
                                        }
                                    },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(
                                        imageVector = if (uiState.isPlayingRecording) Icons.Default.Stop else Icons.Default.PlayArrow,
                                        contentDescription = if (uiState.isPlayingRecording) "Остановить" else "Прослушать запись",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                
                                if (uiState.isPlayingRecording) {
                                    Text(
                                        text = "Воспроизведение записи...",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                } else {
                                    Text(
                                        text = "Прослушать вашу запись",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            RiButton(
                                text = if (uiState.isAnalyzing) "Анализ..." else "Анализировать интонацию",
                                onClick = { viewModel.analyzeIntonation() },
                                isLoading = uiState.isAnalyzing,
                                enabled = !uiState.isAnalyzing
                            )
                        }
                    }
                }
                
                // Переход к результатам
                if (uiState.practiceResult != null) {
                    LaunchedEffect(uiState.practiceResult) {
                        uiState.practiceResult?.let {
                            onNavigateToResults(it.id)
                            // Сбрасываем результат после перехода к экрану результатов
                            viewModel.clearPracticeResult()
                        }
                    }
                }
            }
        }
    }
    
    // Диалог с объяснением необходимости разрешения
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("Необходимо разрешение") },
            text = { Text("Для записи вашего произношения приложению требуется доступ к микрофону. Пожалуйста, предоставьте это разрешение в настройках приложения.") },
            confirmButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text("Понятно")
                }
            }
        )
    }
} 