package com.mikleaka.russianintonation.ui.intonation

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mikleaka.russianintonation.data.models.Difficulty
import com.mikleaka.russianintonation.data.models.Level
import com.mikleaka.russianintonation.ui.components.LoadingDialog
import com.mikleaka.russianintonation.ui.components.RiTopAppBar
import com.mikleaka.russianintonation.ui.components.RiTopAppBarWithProfile

/**
 * Экран с деталями и уровнями интонационной конструкции
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntonationDetailsScreen(
    constructionId: String,
    onNavigateBack: () -> Unit,
    onNavigateToPractice: (String, String) -> Unit,
    onNavigateToProfile: () -> Unit,
    viewModel: IntonationDetailsViewModel = viewModel(factory = IntonationDetailsViewModelFactory(constructionId))
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            RiTopAppBarWithProfile(
                title = uiState.construction?.title ?: "Загрузка...",
                onBackClick = onNavigateBack,
                onProfileClick = onNavigateToProfile
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            LoadingDialog()
        } else {
            uiState.construction?.let { construction ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        // Заголовок и описание интонационной конструкции
                        Text(
                            text = construction.title,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = construction.description,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Text(
                            text = "Уровни",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    items(construction.levels) { level ->
                        LevelCard(
                            level = level,
                            onPlayClick = { viewModel.playReferenceAudio(level) },
                            onStopClick = { viewModel.stopPlayback() },
                            isPlaying = uiState.currentlyPlayingLevelId == level.id,
                            onStartPractice = { onNavigateToPractice(constructionId, level.id) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Карточка уровня интонационной конструкции
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LevelCard(
    level: Level,
    onPlayClick: () -> Unit,
    onStopClick: () -> Unit,
    isPlaying: Boolean,
    onStartPractice: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onStartPractice
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = level.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = getDifficultyText(level.difficulty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = getDifficultyColor(level.difficulty)
                    )
                }
                
                IconButton(
                    onClick = { if (isPlaying) onStopClick() else onPlayClick() },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Остановить" else "Прослушать",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            
            Text(
                text = level.description,
                style = MaterialTheme.typography.bodySmall
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.small,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(
                    text = level.phrase,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = if (isPlaying) "Воспроизведение..." else "Нажмите, чтобы начать практику",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Получает текст для отображения сложности
 */
@Composable
fun getDifficultyText(difficulty: Difficulty): String {
    return when (difficulty) {
        Difficulty.EASY -> "Легкий уровень"
        Difficulty.MEDIUM -> "Средний уровень"
        Difficulty.HARD -> "Сложный уровень"
    }
}

/**
 * Получает цвет для отображения сложности
 */
@Composable
fun getDifficultyColor(difficulty: Difficulty) = when (difficulty) {
    Difficulty.EASY -> MaterialTheme.colorScheme.tertiary
    Difficulty.MEDIUM -> MaterialTheme.colorScheme.secondary
    Difficulty.HARD -> MaterialTheme.colorScheme.error
} 