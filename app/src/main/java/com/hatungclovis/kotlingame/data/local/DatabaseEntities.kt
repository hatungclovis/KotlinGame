package com.hatungclovis.kotlingame.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hatungclovis.kotlingame.domain.models.DifficultyLevel

/**
 * Room entity for user game settings
 */
@Entity(tableName = "game_settings")
@TypeConverters(Converters::class)
data class GameSettingsEntity(
    @PrimaryKey val id: Int = 1, // Single row for app settings
    val difficulty: DifficultyLevel,
    val wordLength: Int,
    val hapticEnabled: Boolean,
    val darkMode: Boolean,
    val soundEnabled: Boolean
)

/**
 * Room entity for game statistics
 */
@Entity(tableName = "game_statistics")
@TypeConverters(Converters::class)
data class GameStatisticsEntity(
    @PrimaryKey val id: Int = 1, // Single row for app statistics
    val gamesPlayed: Int,
    val gamesWon: Int,
    val currentStreak: Int,
    val maxStreak: Int,
    val totalScore: Int,
    val averageScore: Double,
    val winPercentage: Double,
    val averageGuesses: Double,
    val guessDistribution: Map<Int, Int> // Number of guesses -> count
)

/**
 * Room entity for game history (optional - for detailed tracking)
 */
@Entity(tableName = "game_history")
@TypeConverters(Converters::class)
data class GameHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val wordLength: Int,
    val difficulty: DifficultyLevel,
    val targetWord: String,
    val guesses: List<String>,
    val won: Boolean,
    val score: Int,
    val attemptsUsed: Int,
    val hintsUsed: Int,
    val startTime: Long,
    val endTime: Long,
    val duration: Long // in milliseconds
)

/**
 * Type converters for Room database
 */
class Converters {
    private val gson = Gson()
    
    @TypeConverter
    fun fromDifficultyLevel(value: DifficultyLevel): String {
        return value.name
    }
    
    @TypeConverter
    fun toDifficultyLevel(value: String): DifficultyLevel {
        return DifficultyLevel.valueOf(value)
    }
    
    @TypeConverter
    fun fromGuessDistribution(value: Map<Int, Int>): String {
        return gson.toJson(value)
    }
    
    @TypeConverter
    fun toGuessDistribution(value: String): Map<Int, Int> {
        val type = object : TypeToken<Map<Int, Int>>() {}.type
        return gson.fromJson(value, type) ?: emptyMap()
    }
    
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return gson.toJson(value)
    }
    
    @TypeConverter
    fun toStringList(value: String): List<String> {
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, type) ?: emptyList()
    }
}
