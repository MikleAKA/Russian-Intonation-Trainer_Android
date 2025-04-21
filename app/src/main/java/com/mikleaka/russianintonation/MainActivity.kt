package com.mikleaka.russianintonation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mikleaka.russianintonation.ui.auth.AuthScreen
import com.mikleaka.russianintonation.ui.home.HomeScreen
import com.mikleaka.russianintonation.ui.intonation.IntonationDetailsScreen
import com.mikleaka.russianintonation.ui.practice.PracticeScreen
import com.mikleaka.russianintonation.ui.results.ResultsScreen
import com.mikleaka.russianintonation.ui.theme.RussianIntonationTheme
import com.mikleaka.russianintonation.ui.profile.ProfileScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RussianIntonationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

/**
 * Навигация приложения
 */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    
    NavHost(navController = navController, startDestination = "auth") {
        // Экран авторизации
        composable("auth") {
            AuthScreen(
                onNavigateToHome = {
                    navController.navigate("home") {
                        popUpTo("auth") { inclusive = true }
                    }
                }
            )
        }
        
        // Экран профиля
        composable("profile") {
            ProfileScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onLogout = {
                    navController.navigate("auth") {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
        
        // Главный экран
        composable("home") {
            HomeScreen(
                onNavigateToIntonationDetails = { constructionId ->
                    navController.navigate("intonation_details/$constructionId")
                },
                onNavigateToProfile = {
                    navController.navigate("profile")
                }
            )
        }
        
        // Экран деталей интонационной конструкции
        composable(
            route = "intonation_details/{constructionId}",
            arguments = listOf(
                navArgument("constructionId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val constructionId = backStackEntry.arguments?.getString("constructionId") ?: ""
            
            IntonationDetailsScreen(
                constructionId = constructionId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToPractice = { constructionId, levelId ->
                    navController.navigate("practice/$constructionId/$levelId")
                },
                onNavigateToProfile = {
                    navController.navigate("profile")
                }
            )
        }
        
        // Экран практики
        composable(
            route = "practice/{constructionId}/{levelId}",
            arguments = listOf(
                navArgument("constructionId") { type = NavType.StringType },
                navArgument("levelId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val constructionId = backStackEntry.arguments?.getString("constructionId") ?: ""
            val levelId = backStackEntry.arguments?.getString("levelId") ?: ""
            
            PracticeScreen(
                constructionId = constructionId,
                levelId = levelId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToResults = { resultId ->
                    navController.navigate("results/$resultId")
                },
                onNavigateToProfile = {
                    navController.navigate("profile")
                }
            )
        }
        
        // Экран результатов
        composable(
            route = "results/{resultId}",
            arguments = listOf(
                navArgument("resultId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val resultId = backStackEntry.arguments?.getString("resultId") ?: ""
            
            ResultsScreen(
                resultId = resultId,
                onNavigateBack = {
                    navController.popBackStack("results/$resultId", inclusive = true)
                },
                onNavigateToHome = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = false }
                    }
                },
                onNavigateToProfile = {
                    navController.navigate("profile")
                }
            )
        }
    }
}