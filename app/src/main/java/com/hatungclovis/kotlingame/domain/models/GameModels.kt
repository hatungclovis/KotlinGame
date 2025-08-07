package com.hatungclovis.kotlingame.domain.models

/**
 * Game difficulty levels with attempt counts
 */
enum class DifficultyLevel(val attempts: Int, val label: String) {
    EASY(7, "Easy"),
    MEDIUM(5, "Medium"),
    HARD(3, "Hard");
    
    companion object {
        fun fromString(value: String): DifficultyLevel = when (value.lowercase()) {
            "easy" -> EASY
            "medium" -> MEDIUM  
            "hard" -> HARD
            else -> MEDIUM
        }
    }
}

/**
 * Current game status
 */
enum class GameStatus {
    PLAYING,
    WON,
    LOST,
    PAUSED
}

/**
 * Letter state in the word grid
 */
enum class LetterState {
    CORRECT,   // Green - correct letter in correct position
    PRESENT,   // Yellow - correct letter in wrong position  
    ABSENT,    // Gray - letter not in word
    EMPTY      // Default state
}

/**
 * Individual guess with word and letter states
 */
data class Guess(
    val word: String,
    val states: List<LetterState>
)

/**
 * Complete game state
 */
data class GameState(
    val currentWord: String = "",
    val guesses: List<Guess> = emptyList(),
    val currentGuess: String = "",
    val gameStatus: GameStatus = GameStatus.PLAYING,
    val difficulty: DifficultyLevel = DifficultyLevel.MEDIUM,
    val wordLength: Int = 5,
    val hintsUsed: Int = 0,
    val score: Int = 0,
    val attemptsLeft: Int = 5,
    val maxAttempts: Int = 5,
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long? = null
)

/**
 * User game settings
 */
data class GameSettings(
    val difficulty: DifficultyLevel = DifficultyLevel.MEDIUM,
    val wordLength: Int = 5,
    val hapticEnabled: Boolean = true,
    val darkMode: Boolean = false,
    val soundEnabled: Boolean = true
)

/**
 * Player statistics
 */
data class GameStatistics(
    val gamesPlayed: Int = 0,
    val gamesWon: Int = 0,
    val currentStreak: Int = 0,
    val maxStreak: Int = 0,
    val totalScore: Int = 0,
    val averageScore: Double = 0.0,
    val winPercentage: Double = 0.0,
    val averageGuesses: Double = 0.0,
    val guessDistribution: Map<Int, Int> = emptyMap()
)

/**
 * Hint data with letter and optional position
 */
data class HintData(
    val letter: String,
    val position: Int? = null,
    val revealed: Boolean = true
)

/**
 * Word analysis data
 */
data class WordAnalysis(
    val byLength: Map<Int, Int> = emptyMap(),
    val byFirstLetter: Map<String, Int> = emptyMap()
)

/**
 * Word statistics information
 */
data class WordStatistics(
    val totalCommonWords: Int,
    val totalAllWords: Int,
    val lengthRange: IntRange,
    val availableLengths: List<Int>,
    val source: String = "json"
)
