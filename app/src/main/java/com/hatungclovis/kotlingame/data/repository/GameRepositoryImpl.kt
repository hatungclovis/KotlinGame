package com.hatungclovis.kotlingame.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.hatungclovis.kotlingame.data.local.*
import com.hatungclovis.kotlingame.domain.models.*
import com.hatungclovis.kotlingame.domain.repository.GameRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "game_preferences")

/**
 * Implementation of GameRepository using Room and DataStore
 */
@Singleton
class GameRepositoryImpl @Inject constructor(
    private val context: Context,
    private val gameDatabase: GameDatabase,
    private val gson: Gson
) : GameRepository {
    
    private val settingsDao = gameDatabase.gameSettingsDao()
    private val statisticsDao = gameDatabase.gameStatisticsDao()
    private val historyDao = gameDatabase.gameHistoryDao()
    
    // DataStore keys for current game state
    private val CURRENT_GAME_KEY = stringPreferencesKey("current_game_state")
    
    // Default settings
    private val defaultSettings = GameSettings(
        difficulty = GameConstants.DEFAULT_DIFFICULTY,
        wordLength = GameConstants.DEFAULT_WORD_LENGTH,
        hapticEnabled = true,
        darkMode = false,
        soundEnabled = true
    )
    
    // Default statistics
    private val defaultStatistics = GameStatistics()
    
    override suspend fun getGameSettings(): Result<GameSettings> = withContext(Dispatchers.IO) {
        try {
            val entity = settingsDao.getSettings()
            if (entity != null) {
                val settings = GameSettings(
                    difficulty = entity.difficulty,
                    wordLength = entity.wordLength,
                    hapticEnabled = entity.hapticEnabled,
                    darkMode = entity.darkMode,
                    soundEnabled = entity.soundEnabled
                )
                Result.success(settings)
            } else {
                // Insert default settings and return them
                val entity = GameSettingsEntity(
                    difficulty = defaultSettings.difficulty,
                    wordLength = defaultSettings.wordLength,
                    hapticEnabled = defaultSettings.hapticEnabled,
                    darkMode = defaultSettings.darkMode,
                    soundEnabled = defaultSettings.soundEnabled
                )
                settingsDao.insertSettings(entity)
                Result.success(defaultSettings)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun getGameSettingsFlow(): Flow<GameSettings> {
        return settingsDao.getSettingsFlow().map { entity ->
            entity?.let {
                GameSettings(
                    difficulty = it.difficulty,
                    wordLength = it.wordLength,
                    hapticEnabled = it.hapticEnabled,
                    darkMode = it.darkMode,
                    soundEnabled = it.soundEnabled
                )
            } ?: defaultSettings
        }
    }
    
    override suspend fun updateGameSettings(settings: GameSettings): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val entity = GameSettingsEntity(
                difficulty = settings.difficulty,
                wordLength = settings.wordLength,
                hapticEnabled = settings.hapticEnabled,
                darkMode = settings.darkMode,
                soundEnabled = settings.soundEnabled
            )
            settingsDao.insertSettings(entity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun resetGameSettings(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            settingsDao.clearSettings()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getGameStatistics(): Result<GameStatistics> = withContext(Dispatchers.IO) {
        try {
            val entity = statisticsDao.getStatistics()
            if (entity != null) {
                val stats = GameStatistics(
                    gamesPlayed = entity.gamesPlayed,
                    gamesWon = entity.gamesWon,
                    currentStreak = entity.currentStreak,
                    maxStreak = entity.maxStreak,
                    totalScore = entity.totalScore,
                    averageScore = entity.averageScore,
                    winPercentage = entity.winPercentage,
                    averageGuesses = entity.averageGuesses,
                    guessDistribution = entity.guessDistribution
                )
                Result.success(stats)
            } else {
                // Insert default statistics and return them
                val entity = GameStatisticsEntity(
                    gamesPlayed = defaultStatistics.gamesPlayed,
                    gamesWon = defaultStatistics.gamesWon,
                    currentStreak = defaultStatistics.currentStreak,
                    maxStreak = defaultStatistics.maxStreak,
                    totalScore = defaultStatistics.totalScore,
                    averageScore = defaultStatistics.averageScore,
                    winPercentage = defaultStatistics.winPercentage,
                    averageGuesses = defaultStatistics.averageGuesses,
                    guessDistribution = defaultStatistics.guessDistribution
                )
                statisticsDao.insertStatistics(entity)
                Result.success(defaultStatistics)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun getGameStatisticsFlow(): Flow<GameStatistics> {
        return statisticsDao.getStatisticsFlow().map { entity ->
            entity?.let {
                GameStatistics(
                    gamesPlayed = it.gamesPlayed,
                    gamesWon = it.gamesWon,
                    currentStreak = it.currentStreak,
                    maxStreak = it.maxStreak,
                    totalScore = it.totalScore,
                    averageScore = it.averageScore,
                    winPercentage = it.winPercentage,
                    averageGuesses = it.averageGuesses,
                    guessDistribution = it.guessDistribution
                )
            } ?: defaultStatistics
        }
    }
    
    override suspend fun updateGameStatistics(stats: GameStatistics): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val entity = GameStatisticsEntity(
                gamesPlayed = stats.gamesPlayed,
                gamesWon = stats.gamesWon,
                currentStreak = stats.currentStreak,
                maxStreak = stats.maxStreak,
                totalScore = stats.totalScore,
                averageScore = stats.averageScore,
                winPercentage = stats.winPercentage,
                averageGuesses = stats.averageGuesses,
                guessDistribution = stats.guessDistribution
            )
            statisticsDao.insertStatistics(entity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun addGameResult(
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
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Add to history
            val historyEntity = GameHistoryEntity(
                wordLength = wordLength,
                difficulty = difficulty,
                targetWord = targetWord,
                guesses = guesses,
                won = won,
                score = score,
                attemptsUsed = attemptsUsed,
                hintsUsed = hintsUsed,
                startTime = startTime,
                endTime = endTime,
                duration = endTime - startTime
            )
            historyDao.insertHistory(historyEntity)
            
            // Update statistics
            val currentStats = getGameStatistics().getOrNull() ?: defaultStatistics
            val newGamesPlayed = currentStats.gamesPlayed + 1
            val newGamesWon = if (won) currentStats.gamesWon + 1 else currentStats.gamesWon
            val newWinPercentage = if (newGamesPlayed > 0) (newGamesWon.toDouble() / newGamesPlayed) * 100 else 0.0
            
            val newCurrentStreak = if (won) currentStats.currentStreak + 1 else 0
            val newMaxStreak = maxOf(currentStats.maxStreak, newCurrentStreak)
            
            val newTotalScore = currentStats.totalScore + score
            val newAverageScore = if (newGamesPlayed > 0) newTotalScore.toDouble() / newGamesPlayed else 0.0
            
            // Calculate average guesses (for won games only)
            val allHistory = historyDao.getWinHistory()
            val totalAttemptsForWins = allHistory.sumOf { it.attemptsUsed } + (if (won) attemptsUsed else 0)
            val totalWinGames = allHistory.size + (if (won) 1 else 0)
            val newAverageGuesses = if (totalWinGames > 0) totalAttemptsForWins.toDouble() / totalWinGames else 0.0
            
            // Update guess distribution
            val newGuessDistribution = currentStats.guessDistribution.toMutableMap()
            if (won) {
                newGuessDistribution[attemptsUsed] = newGuessDistribution.getOrDefault(attemptsUsed, 0) + 1
            }
            
            val updatedStats = GameStatistics(
                gamesPlayed = newGamesPlayed,
                gamesWon = newGamesWon,
                currentStreak = newCurrentStreak,
                maxStreak = newMaxStreak,
                totalScore = newTotalScore,
                averageScore = newAverageScore,
                winPercentage = newWinPercentage,
                averageGuesses = newAverageGuesses,
                guessDistribution = newGuessDistribution
            )
            
            updateGameStatistics(updatedStats)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun resetGameStatistics(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            statisticsDao.clearStatistics()
            historyDao.clearHistory()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getGameHistory(): Result<List<GameHistoryEntity>> = withContext(Dispatchers.IO) {
        try {
            val history = historyDao.getAllHistory()
            Result.success(history)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun getGameHistoryFlow(): Flow<List<GameHistoryEntity>> {
        return historyDao.getAllHistoryFlow()
    }
    
    override suspend fun getGameHistoryByDifficulty(difficulty: DifficultyLevel): Result<List<GameHistoryEntity>> = withContext(Dispatchers.IO) {
        try {
            val history = historyDao.getHistoryByDifficulty(difficulty.name)
            Result.success(history)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun clearGameHistory(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            historyDao.clearHistory()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun saveCurrentGameState(gameState: GameState): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val json = gson.toJson(gameState)
            context.dataStore.edit { preferences ->
                preferences[CURRENT_GAME_KEY] = json
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun loadCurrentGameState(): Result<GameState?> = withContext(Dispatchers.IO) {
        try {
            val preferences = context.dataStore.data.first()
            val json = preferences[CURRENT_GAME_KEY]
            if (json != null) {
                val gameState = gson.fromJson(json, GameState::class.java)
                Result.success(gameState)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun clearCurrentGameState(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            context.dataStore.edit { preferences ->
                preferences.remove(CURRENT_GAME_KEY)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
