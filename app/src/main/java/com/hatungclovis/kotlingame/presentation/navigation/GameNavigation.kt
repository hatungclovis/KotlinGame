package com.hatungclovis.kotlingame.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.hatungclovis.kotlingame.domain.models.DifficultyLevel
import com.hatungclovis.kotlingame.presentation.screens.HomeScreen
import com.hatungclovis.kotlingame.presentation.screens.GameScreen
import com.hatungclovis.kotlingame.presentation.screens.SettingsScreen
import com.hatungclovis.kotlingame.presentation.screens.StatisticsScreen
import com.hatungclovis.kotlingame.presentation.screens.WordAnalysisScreen

/**
 * Navigation routes for the game
 */
sealed class GameRoute(val route: String) {
    object Home : GameRoute("home")
    object Game : GameRoute("game/{difficulty}/{wordLength}") {
        fun createRoute(difficulty: DifficultyLevel, wordLength: Int): String {
            return "game/${difficulty.name}/${wordLength}"
        }
    }
    object Settings : GameRoute("settings")
    object Statistics : GameRoute("statistics")
    object WordAnalysis : GameRoute("word_analysis")
    object About : GameRoute("about")
}

/**
 * Main navigation host for the game
 */
@Composable
fun GameNavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: String = GameRoute.Home.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Home Screen
        composable(GameRoute.Home.route) {
            HomeScreen(
                onNavigateToGame = { difficulty, wordLength ->
                    navController.navigate(
                        GameRoute.Game.createRoute(difficulty, wordLength)
                    )
                },
                onNavigateToSettings = {
                    navController.navigate(GameRoute.Settings.route)
                },
                onNavigateToStatistics = {
                    navController.navigate(GameRoute.Statistics.route)
                },
                onNavigateToWordAnalysis = {
                    navController.navigate(GameRoute.WordAnalysis.route)
                }
            )
        }
        
        // Game Screen
        composable(
            route = GameRoute.Game.route,
            arguments = listOf(
                androidx.navigation.navArgument("difficulty") {
                    type = androidx.navigation.NavType.StringType
                },
                androidx.navigation.navArgument("wordLength") {
                    type = androidx.navigation.NavType.IntType
                }
            )
        ) { backStackEntry ->
            val difficultyString = backStackEntry.arguments?.getString("difficulty") ?: ""
            val wordLength = backStackEntry.arguments?.getInt("wordLength") ?: 5
            val difficulty = try {
                DifficultyLevel.valueOf(difficultyString)
            } catch (e: IllegalArgumentException) {
                DifficultyLevel.MEDIUM
            }
            
            GameScreen(
                difficulty = difficulty,
                wordLength = wordLength,
                navController = navController
            )
        }
        
        // Settings Screen
        composable(GameRoute.Settings.route) {
            SettingsScreen(navController = navController)
        }
        
        // Statistics Screen  
        composable(GameRoute.Statistics.route) {
            StatisticsScreen(navController = navController)
        }
        
        // Word Analysis Screen
        composable(GameRoute.WordAnalysis.route) {
            WordAnalysisScreen(navController = navController)
        }
        
        // About Screen
        composable(GameRoute.About.route) {
            PlaceholderScreen("About Screen - Coming Soon") {
                navController.popBackStack()
            }
        }
    }
}

/**
 * Placeholder screen component
 */

@Composable
private fun PlaceholderScreen(
    title: String,
    onNavigateBack: () -> Unit
) {
    androidx.compose.foundation.layout.Box(
        modifier = androidx.compose.foundation.layout.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        androidx.compose.foundation.layout.Column(
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
        ) {
            androidx.compose.material3.Text(title)
            androidx.compose.material3.Button(onClick = onNavigateBack) {
                androidx.compose.material3.Text("Back")
            }
        }
    }
}
