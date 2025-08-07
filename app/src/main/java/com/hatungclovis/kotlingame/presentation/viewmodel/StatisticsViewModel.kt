package com.hatungclovis.kotlingame.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hatungclovis.kotlingame.domain.models.GameStatistics
import com.hatungclovis.kotlingame.domain.models.GameHistory
import com.hatungclovis.kotlingame.domain.repository.GameRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val gameRepository: GameRepository
) : ViewModel() {

    private val _statistics = MutableStateFlow(GameStatistics())
    val statistics: StateFlow<GameStatistics> = _statistics.asStateFlow()

    private val _gameHistory = MutableStateFlow<List<GameHistory>>(emptyList())
    val gameHistory: StateFlow<List<GameHistory>> = _gameHistory.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    /**
     * Load statistics from repository
     */
    fun loadStatistics() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val stats = gameRepository.getStatistics() ?: GameStatistics()
                _statistics.value = stats
            } catch (e: Exception) {
                _message.value = "Failed to load statistics: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Load game history from repository
     */
    fun loadGameHistory() {
        viewModelScope.launch {
            try {
                val history = gameRepository.getGameHistory()
                _gameHistory.value = history.sortedByDescending { it.timestamp }
            } catch (e: Exception) {
                _message.value = "Failed to load game history: ${e.message}"
            }
        }
    }

    /**
     * Reset all statistics and game history
     */
    fun resetAllStatistics() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                gameRepository.resetStatistics()
                gameRepository.clearGameHistory()
                
                // Reset local state
                _statistics.value = GameStatistics()
                _gameHistory.value = emptyList()
                
                _message.value = "All statistics have been reset"
                clearMessage()
            } catch (e: Exception) {
                _message.value = "Failed to reset statistics: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Export statistics to a shareable format
     */
    fun exportStatistics(): String {
        val stats = _statistics.value
        val history = _gameHistory.value
        
        return buildString {
            appendLine("📊 Word Game Statistics")
            appendLine("=" .repeat(30))
            appendLine()
            
            // Overall stats
            appendLine("Overall Performance:")
            appendLine("Games Played: ${stats.gamesPlayed}")
            appendLine("Games Won: ${stats.gamesWon}")
            appendLine("Win Rate: ${"%.1f".format(stats.winPercentage)}%")
            appendLine("Current Streak: ${stats.currentStreak}")
            appendLine("Best Streak: ${stats.maxStreak}")
            appendLine("Average Score: ${"%.1f".format(stats.averageScore)}")
            appendLine("Average Guesses: ${"%.1f".format(stats.averageGuesses)}")
            appendLine()
            
            // Guess distribution
            if (stats.guessDistribution.isNotEmpty()) {
                appendLine("Guess Distribution:")
                stats.guessDistribution.toSortedMap().forEach { (guesses, count) ->
                    val percentage = if (stats.gamesWon > 0) (count * 100f / stats.gamesWon) else 0f
                    appendLine("$guesses guesses: $count games (${percentage.toInt()}%)")
                }
                appendLine()
            }
            
            // Performance by difficulty
            if (history.isNotEmpty()) {
                appendLine("Performance by Difficulty:")
                val performanceByDifficulty = history.groupBy { it.difficulty }
                
                performanceByDifficulty.forEach { (difficulty, games) ->
                    val wins = games.count { it.won }
                    val winRate = if (games.isNotEmpty()) (wins * 100f / games.size) else 0f
                    val avgScore = games.map { it.score }.average().takeIf { !it.isNaN() } ?: 0.0
                    
                    appendLine("${difficulty.label}: ${games.size} games, ${winRate.toInt()}% win rate, avg ${avgScore.toInt()} pts")
                }
                appendLine()
            }
            
            appendLine("🎯 Enhanced Word Game by Clovis")
        }
    }

    /**
     * Get statistics for a specific difficulty
     */
    fun getStatisticsForDifficulty(difficulty: com.hatungclovis.kotlingame.domain.models.DifficultyLevel): DifficultyStats {
        val relevantGames = _gameHistory.value.filter { it.difficulty == difficulty }
        val wins = relevantGames.count { it.won }
        val totalGames = relevantGames.size
        val winRate = if (totalGames > 0) (wins * 100f / totalGames) else 0f
        val avgScore = relevantGames.map { it.score }.average().takeIf { !it.isNaN() } ?: 0.0
        val avgGuesses = relevantGames.filter { it.won }.map { it.attempts }.average().takeIf { !it.isNaN() } ?: 0.0
        
        return DifficultyStats(
            difficulty = difficulty,
            gamesPlayed = totalGames,
            gamesWon = wins,
            winRate = winRate,
            averageScore = avgScore,
            averageGuesses = avgGuesses
        )
    }

    /**
     * Clear message after a delay
     */
    private fun clearMessage() {
        viewModelScope.launch {
            kotlinx.coroutines.delay(3000)
            _message.value = null
        }
    }

    /**
     * Manually clear message
     */
    fun clearMessage() {
        _message.value = null
    }
}

/**
 * Statistics for a specific difficulty level
 */
data class DifficultyStats(
    val difficulty: com.hatungclovis.kotlingame.domain.models.DifficultyLevel,
    val gamesPlayed: Int,
    val gamesWon: Int,
    val winRate: Float,
    val averageScore: Double,
    val averageGuesses: Double
)
