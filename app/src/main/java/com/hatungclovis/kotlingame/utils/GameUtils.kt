package com.hatungclovis.kotlingame.utils

import android.content.Context
import android.content.Intent
import com.hatungclovis.kotlingame.domain.models.*
import kotlin.math.max

/**
 * Game utility functions
 * Kotlin translation of gameUtils.ts
 */
object GameUtils {
    
    /**
     * Calculate score based on the original Python logic:
     * - Each correct letter: 1 point
     * - Each half-correct letter: 0.5 points  
     * - Each remaining attempt: 3 points
     */
    fun calculateScore(
        correctChars: Int,
        presentChars: Int,
        attemptsLeft: Int
    ): Double {
        return (correctChars * GameConstants.SCORE_PER_CORRECT_LETTER) +
               (presentChars * GameConstants.SCORE_PER_PRESENT_LETTER) +
               (attemptsLeft * GameConstants.SCORE_PER_REMAINING_ATTEMPT)
    }
    
    /**
     * Get maximum attempts based on difficulty level
     */
    fun getMaxAttempts(difficulty: DifficultyLevel): Int {
        return difficulty.attempts
    }
    
    /**
     * Format time duration in a human-readable format
     */
    fun formatDuration(startTime: Long, endTime: Long? = null): String {
        val end = endTime ?: System.currentTimeMillis()
        val diffMs = end - startTime
        val diffMinutes = (diffMs / 60000).toInt()
        val diffSeconds = ((diffMs % 60000) / 1000).toInt()
        
        return if (diffMinutes > 0) {
            "${diffMinutes}m ${diffSeconds}s"
        } else {
            "${diffSeconds}s"
        }
    }
    
    /**
     * Generate a random word from a list of words with specific length
     */
    fun getRandomWord(words: List<String>, length: Int): String {
        val filteredWords = words.filter { it.length == length }
        if (filteredWords.isEmpty()) {
            throw IllegalArgumentException("No words found with length $length")
        }
        return filteredWords.random()
    }
    
    /**
     * Check if a word is valid (exists in the word list)
     */
    fun isValidWord(word: String, wordList: List<String>): Boolean {
        return wordList.contains(word.lowercase())
    }
    
    /**
     * Get available hint letters (letters in target word not yet guessed correctly)
     */
    fun getAvailableHints(
        targetWord: String,
        guessedLetters: Set<String>,
        correctPositions: Set<Int>
    ): List<String> {
        val hints = mutableListOf<String>()
        
        targetWord.forEachIndexed { i, char ->
            val letter = char.toString().uppercase()
            
            // Only include if letter hasn't been guessed and position isn't correct
            if (!guessedLetters.contains(letter) && !correctPositions.contains(i)) {
                if (!hints.contains(letter)) {
                    hints.add(letter)
                }
            }
        }
        
        return hints
    }
    
    /**
     * Get a random hint letter
     */
    fun getRandomHint(availableHints: List<String>): String? {
        return availableHints.randomOrNull()
    }
    
    /**
     * Share game results in a formatted string (like original Wordle)
     */
    fun formatShareText(
        difficulty: DifficultyLevel,
        wordLength: Int,
        attempts: Int,
        maxAttempts: Int,
        won: Boolean,
        score: Int
    ): String {
        val difficultyLabel = difficulty.label
        val result = if (won) "$attempts/$maxAttempts" else "X/$maxAttempts"
        
        return buildString {
            appendLine("Word Game CH - $difficultyLabel")
            appendLine("Word Length: $wordLength")
            appendLine("Result: $result")
            appendLine("Score: $score")
            appendLine()
            appendLine("🎯 Enhanced Word Game by Clovis")
        }
    }
    
    /**
     * Validate word length is within acceptable range
     */
    fun isValidWordLength(length: Int): Boolean {
        return length in GameConstants.WORD_LENGTH_OPTIONS
    }
    
    /**
     * Generate letter frequency analysis like in the Python version
     */
    fun analyzeWordFrequency(words: List<String>): WordAnalysis {
        val byLength = mutableMapOf<Int, Int>()
        val byFirstLetter = mutableMapOf<String, Int>()
        
        words.forEach { word ->
            // Count by length
            val length = word.length
            byLength[length] = byLength.getOrDefault(length, 0) + 1
            
            // Count by first letter
            val firstLetter = word.first().lowercase()
            byFirstLetter[firstLetter] = byFirstLetter.getOrDefault(firstLetter, 0) + 1
        }
        
        return WordAnalysis(
            byLength = byLength,
            byFirstLetter = byFirstLetter
        )
    }
    
    /**
     * Calculate responsive tile size based on screen width and word length
     */
    fun calculateTileSize(screenWidth: Int, wordLength: Int, horizontalPadding: Int = 40): Int {
        val tileSpacing = 4
        val availableWidth = screenWidth - horizontalPadding
        val maxTileSize = (availableWidth - (wordLength - 1) * tileSpacing) / wordLength
        
        return max(GameConstants.MIN_TILE_SIZE, maxOf(maxTileSize, GameConstants.MAX_TILE_SIZE))
    }
    
    /**
     * Get color for letter state
     */
    fun getColorForLetterState(state: LetterState, darkMode: Boolean = false): androidx.compose.ui.graphics.Color {
        return when (state) {
            LetterState.CORRECT -> GameColors.CORRECT
            LetterState.PRESENT -> GameColors.PRESENT
            LetterState.ABSENT -> GameColors.ABSENT
            LetterState.EMPTY -> if (darkMode) GameColors.BORDER_DARK else GameColors.EMPTY
        }
    }
    
    /**
     * Generate statistics summary
     */
    fun generateStatisticsSummary(stats: GameStatistics): String {
        return buildString {
            appendLine("📊 Game Statistics")
            appendLine("Games Played: ${stats.gamesPlayed}")
            appendLine("Games Won: ${stats.gamesWon}")
            appendLine("Win Percentage: ${"%.1f".format(stats.winPercentage)}%")
            appendLine("Current Streak: ${stats.currentStreak}")
            appendLine("Max Streak: ${stats.maxStreak}")
            appendLine("Average Score: ${"%.1f".format(stats.averageScore)}")
            if (stats.averageGuesses > 0) {
                appendLine("Average Guesses: ${"%.1f".format(stats.averageGuesses)}")
            }
        }
    }
    
    /**
     * Format time in milliseconds to readable format
     */
    fun formatTime(timeMs: Long): String {
        val totalSeconds = (timeMs / 1000).toInt()
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        
        return if (minutes > 0) {
            "${minutes}m ${seconds}s"
        } else {
            "${seconds}s"
        }
    }
    
    /**
     * Update game statistics with the results of a completed game
     */
    fun updateStatistics(stats: GameStatistics, gameState: GameState, won: Boolean): GameStatistics {
        val newGamesPlayed = stats.gamesPlayed + 1
        val newGamesWon = stats.gamesWon + if (won) 1 else 0
        val newWinPercentage = if (newGamesPlayed > 0) (newGamesWon.toDouble() / newGamesPlayed * 100) else 0.0
        
        val newCurrentStreak = if (won) stats.currentStreak + 1 else 0
        val newMaxStreak = maxOf(stats.maxStreak, newCurrentStreak)
        
        val newTotalScore = stats.totalScore + gameState.score
        val newAverageScore = if (newGamesPlayed > 0) newTotalScore.toDouble() / newGamesPlayed else 0.0
        
        val newTotalGuesses = stats.totalGuesses + gameState.guesses.size
        val newAverageGuesses = if (newGamesPlayed > 0) newTotalGuesses.toDouble() / newGamesPlayed else 0.0
        
        return stats.copy(
            gamesPlayed = newGamesPlayed,
            gamesWon = newGamesWon,
            winPercentage = newWinPercentage,
            currentStreak = newCurrentStreak,
            maxStreak = newMaxStreak,
            totalScore = newTotalScore,
            averageScore = newAverageScore,
            totalGuesses = newTotalGuesses,
            averageGuesses = newAverageGuesses
        )
    }
    
    /**
     * Share game result via Android share intent
     */
    fun shareGameResult(context: Context, gameState: GameState, won: Boolean) {
        val shareText = formatShareText(
            difficulty = gameState.difficulty,
            wordLength = gameState.wordLength,
            attempts = gameState.guesses.size,
            maxAttempts = getMaxAttempts(gameState.difficulty),
            won = won,
            score = gameState.score
        )
        
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        
        val chooser = Intent.createChooser(shareIntent, "Share Game Result")
        context.startActivity(chooser)
    }
    
    /**
     * Share statistics via Android share intent
     */
    fun shareStatistics(context: Context, statistics: GameStatistics) {
        val statsText = buildString {
            appendLine("📊 Word Game Statistics")
            appendLine("Games Played: ${statistics.gamesPlayed}")
            appendLine("Win Rate: ${statistics.winPercentage.toInt()}%")
            appendLine("Current Streak: ${statistics.currentStreak}")
            appendLine("Best Streak: ${statistics.maxStreak}")
            appendLine("Average Score: ${statistics.averageScore.toInt()}")
            if (statistics.averageGuesses > 0) {
                appendLine("Average Guesses: ${"%.1f".format(statistics.averageGuesses)}")
            }
            appendLine()
            appendLine("🎯 Enhanced Word Game by Clovis")
        }
        
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, statsText)
        }
        
        val chooser = Intent.createChooser(shareIntent, "Share Statistics")
        context.startActivity(chooser)
    }
    
    /**
     * Debounce function implementation using coroutines
     */
    class Debouncer(private val delayMs: Long) {
        private var debounceJob: kotlinx.coroutines.Job? = null
        
        fun debounce(action: suspend () -> Unit) {
            debounceJob?.cancel()
            debounceJob = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                kotlinx.coroutines.delay(delayMs)
                action()
            }
        }
        
        fun cancel() {
            debounceJob?.cancel()
        }
    }
}
