package com.hatungclovis.kotlingame.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * DAO for game settings
 */
@Dao
interface GameSettingsDao {
    
    @Query("SELECT * FROM game_settings WHERE id = 1")
    suspend fun getSettings(): GameSettingsEntity?
    
    @Query("SELECT * FROM game_settings WHERE id = 1")
    fun getSettingsFlow(): Flow<GameSettingsEntity?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: GameSettingsEntity)
    
    @Update
    suspend fun updateSettings(settings: GameSettingsEntity)
    
    @Query("DELETE FROM game_settings")
    suspend fun clearSettings()
}

/**
 * DAO for game statistics
 */
@Dao
interface GameStatisticsDao {
    
    @Query("SELECT * FROM game_statistics WHERE id = 1")
    suspend fun getStatistics(): GameStatisticsEntity?
    
    @Query("SELECT * FROM game_statistics WHERE id = 1")
    fun getStatisticsFlow(): Flow<GameStatisticsEntity?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStatistics(statistics: GameStatisticsEntity)
    
    @Update
    suspend fun updateStatistics(statistics: GameStatisticsEntity)
    
    @Query("DELETE FROM game_statistics")
    suspend fun clearStatistics()
}

/**
 * DAO for game history
 */
@Dao
interface GameHistoryDao {
    
    @Query("SELECT * FROM game_history ORDER BY endTime DESC")
    suspend fun getAllHistory(): List<GameHistoryEntity>
    
    @Query("SELECT * FROM game_history ORDER BY endTime DESC")
    fun getAllHistoryFlow(): Flow<List<GameHistoryEntity>>
    
    @Query("SELECT * FROM game_history WHERE difficulty = :difficulty ORDER BY endTime DESC")
    suspend fun getHistoryByDifficulty(difficulty: String): List<GameHistoryEntity>
    
    @Query("SELECT * FROM game_history WHERE wordLength = :wordLength ORDER BY endTime DESC")
    suspend fun getHistoryByWordLength(wordLength: Int): List<GameHistoryEntity>
    
    @Query("SELECT * FROM game_history WHERE won = 1 ORDER BY endTime DESC")
    suspend fun getWinHistory(): List<GameHistoryEntity>
    
    @Query("SELECT * FROM game_history WHERE won = 0 ORDER BY endTime DESC")
    suspend fun getLossHistory(): List<GameHistoryEntity>
    
    @Query("SELECT * FROM game_history WHERE endTime >= :fromTime ORDER BY endTime DESC")
    suspend fun getHistoryFromTime(fromTime: Long): List<GameHistoryEntity>
    
    @Query("SELECT COUNT(*) FROM game_history")
    suspend fun getHistoryCount(): Int
    
    @Query("SELECT AVG(score) FROM game_history WHERE won = 1")
    suspend fun getAverageWinScore(): Double?
    
    @Query("SELECT AVG(attemptsUsed) FROM game_history WHERE won = 1")
    suspend fun getAverageAttemptsUsed(): Double?
    
    @Insert
    suspend fun insertHistory(history: GameHistoryEntity)
    
    @Query("DELETE FROM game_history")
    suspend fun clearHistory()
    
    @Query("DELETE FROM game_history WHERE id = :id")
    suspend fun deleteHistory(id: Long)
    
    @Query("DELETE FROM game_history WHERE endTime < :beforeTime")
    suspend fun deleteOldHistory(beforeTime: Long)
}
