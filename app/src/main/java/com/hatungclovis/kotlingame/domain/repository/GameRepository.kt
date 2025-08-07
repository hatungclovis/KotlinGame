package com.hatungclovis.kotlingame.domain.repository

import com.hatungclovis.kotlingame.data.local.GameHistoryEntity
import com.hatungclovis.kotlingame.domain.models.*
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for game state, settings, and statistics
 */
interface GameRepository {
    
    // Settings
    suspend fun getSettings(): GameSettings?
    suspend fun saveSettings(settings: GameSettings)
    suspend fun resetSettings()
    
    // Statistics
    suspend fun getStatistics(): GameStatistics?
    suspend fun saveStatistics(stats: GameStatistics)
    suspend fun resetStatistics()
    
    // Game History
    suspend fun getGameHistory(): List<GameHistory>
    suspend fun saveGameHistory(history: GameHistory)
    suspend fun clearGameHistory()
    
    // Game State Persistence (for current game)
    suspend fun saveCurrentGameState(gameState: GameState)
    suspend fun loadCurrentGameState(): GameState?
    suspend fun clearCurrentGameState()
}
