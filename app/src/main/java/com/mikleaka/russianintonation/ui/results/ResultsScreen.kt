package com.mikleaka.russianintonation.ui.results

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mikleaka.russianintonation.data.models.IntonationPoint
import com.mikleaka.russianintonation.ui.components.LoadingDialog
import com.mikleaka.russianintonation.ui.components.RiButton
import com.mikleaka.russianintonation.ui.components.RiTopAppBar
import com.mikleaka.russianintonation.ui.components.RiTopAppBarWithProfile

/**
 * Экран результатов практики
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsScreen(
    resultId: String,
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToProfile: () -> Unit,
    viewModel: ResultsViewModel = viewModel(factory = ResultsViewModelFactory(resultId))
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            RiTopAppBarWithProfile(
                title = "Результаты",
                onBackClick = onNavigateBack,
                onProfileClick = onNavigateToProfile
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            LoadingDialog()
        } else {
            uiState.practiceResult?.let { result ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Оценка
                    ScoreCard(score = result.score)
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // График интонации
                    IntonationChart(
                        userPoints = result.intonationPoints,
                        referencePoints = result.referencePoints
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Обратная связь
                    FeedbackCard(feedback = result.feedback)
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Кнопки действий
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RiButton(
                            text = "Повторить",
                            onClick = onNavigateBack,
                            modifier = Modifier.weight(1f)
                        )
                        
                        RiButton(
                            text = "На главную",
                            onClick = onNavigateToHome,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Воспроизведение записи
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(
                                onClick = { viewModel.playRecordedAudio() },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = if (uiState.isPlayingRecording) 
                                        Icons.Default.Stop 
                                    else 
                                        Icons.Default.PlayArrow,
                                    contentDescription = if (uiState.isPlayingRecording) 
                                        "Остановить воспроизведение" 
                                    else 
                                        "Прослушать запись",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            
                            Text(
                                text = if (uiState.isPlayingRecording) 
                                    "Воспроизведение записи..." 
                                else 
                                    "Прослушать вашу запись",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Карточка с оценкой
 */
@Composable
fun ScoreCard(score: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Ваш результат",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(getScoreColor(score)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$score%",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = getScoreDescription(score),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Карточка с обратной связью
 */
@Composable
fun FeedbackCard(feedback: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Рекомендации",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = feedback,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

/**
 * График интонации
 */
@Composable
fun IntonationChart(
    userPoints: List<IntonationPoint>,
    referencePoints: List<IntonationPoint>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "График интонации",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val width = size.width
                    val height = size.height
                    
                    // Находим максимальные значения для нормализации
                    val maxTime = maxOf(
                        userPoints.maxOfOrNull { it.timeMs } ?: 0,
                        referencePoints.maxOfOrNull { it.timeMs } ?: 0
                    ).toFloat()
                    
                    val maxFreq = maxOf(
                        userPoints.maxOfOrNull { it.frequency } ?: 0.0,
                        referencePoints.maxOfOrNull { it.frequency } ?: 0.0
                    ).toFloat()
                    
                    // Рисуем график эталонной интонации
                    if (referencePoints.isNotEmpty()) {
                        val referencePath = Path()
                        val firstPoint = referencePoints.first()
                        val startX = (firstPoint.timeMs / maxTime) * width
                        val startY = height - ((firstPoint.frequency / maxFreq) * height).toFloat()
                        
                        referencePath.moveTo(startX, startY)
                        
                        for (i in 1 until referencePoints.size) {
                            val point = referencePoints[i]
                            val x = (point.timeMs / maxTime) * width
                            val y = height - ((point.frequency / maxFreq) * height).toFloat()
                            referencePath.lineTo(x, y)
                        }
                        
                        drawPath(
                            path = referencePath,
                            color = Color.Blue,
                            style = Stroke(width = 3f)
                        )
                    }
                    
                    // Рисуем график пользовательской интонации
                    if (userPoints.isNotEmpty()) {
                        val userPath = Path()
                        val firstPoint = userPoints.first()
                        val startX = (firstPoint.timeMs / maxTime) * width
                        val startY = height - ((firstPoint.frequency / maxFreq) * height).toFloat()
                        
                        userPath.moveTo(startX, startY)
                        
                        for (i in 1 until userPoints.size) {
                            val point = userPoints[i]
                            val x = (point.timeMs / maxTime) * width
                            val y = height - ((point.frequency / maxFreq) * height).toFloat()
                            userPath.lineTo(x, y)
                        }
                        
                        drawPath(
                            path = userPath,
                            color = Color.Red,
                            style = Stroke(width = 3f)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Легенда
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(Color.Blue)
                    )
                    
                    Spacer(modifier = Modifier.padding(start = 4.dp))
                    
                    Text(
                        text = "Эталон",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(Color.Red)
                    )
                    
                    Spacer(modifier = Modifier.padding(start = 4.dp))
                    
                    Text(
                        text = "Ваша интонация",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

/**
 * Возвращает цвет в зависимости от оценки
 */
@Composable
fun getScoreColor(score: Int): Color {
    return when {
        score >= 90 -> Color(0xFF4CAF50) // Зеленый
        score >= 75 -> Color(0xFF8BC34A) // Светло-зеленый
        score >= 60 -> Color(0xFFFFC107) // Желтый
        else -> Color(0xFFFF5722) // Оранжевый
    }
}

/**
 * Возвращает текстовое описание оценки
 */
fun getScoreDescription(score: Int): String {
    return when {
        score >= 90 -> "Отлично! Ваша интонация очень близка к эталонной."
        score >= 75 -> "Хорошо! Есть небольшие отклонения в интонации, но в целом правильно."
        score >= 60 -> "Неплохо, но требуется дополнительная практика."
        else -> "Нужна дополнительная практика. Постарайтесь четче следовать образцу интонации."
    }
} 