package com.hatungclovis.kotlingame.domain.repository

import com.hatungclovis.kotlingame.data.local.GameHistoryEntity
import com.hatungclovis.kotlingame.domain.models.*
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for game state, settings, and statistics
 */
interface GameRepository {
    
    // Settings
    suspend fun getGameSettings(): Result<GameSettings>
    fun getGameSettingsFlow(): Flow<GameSettings>
    suspend fun updateGameSettings(settings: GameSettings): Result<Unit>
    suspend fun resetGameSettings(): Result<Unit>
    
    // Statistics
    suspend fun getGameStatistics(): Result<GameStatistics>
    fun getGameStatisticsFlow(): Flow<GameStatistics>
    suspend fun updateGameStatistics(stats: GameStatistics): Result<Unit>
    suspend fun addGameResult(
        difficulty: DifficultyLevel,
        wordLength: Int,
        targetWord: String,
        guesses: List<String>,
        won: Boolean,
        score: Int,
        attemptsUsed: Int,
        hintsUsed: Int,
        startTime: Long,
        endTime: Long
    ): Result<Unit>
    suspend fun resetGameStatistics(): Result<Unit>
    
    // Game History
    suspend fun getGameHistory(): Result<List<GameHistoryEntity>>
    fun getGameHistoryFlow(): Flow<List<GameHistoryEntity>>
    suspend fun getGameHistoryByDifficulty(difficulty: DifficultyLevel): Result<List<GameHistoryEntity>>
    suspend fun clearGameHistory(): Result<Unit>
    
    // Game State Persistence (for current game)
    suspend fun saveCurrentGameState(gameState: GameState): Result<Unit>
    suspend fun loadCurrentGameState(): Result<GameState?>
    suspend fun clearCurrentGameState(): Result<Unit>
}
