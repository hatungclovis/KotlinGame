package com.hatungclovis.kotlingame.domain.models

import androidx.compose.ui.graphics.Color

/**
 * Game constants and configuration values
 */
object GameConstants {
    
    // Word length options
    val WORD_LENGTH_OPTIONS = listOf(3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14)
    
    // Default values
    const val DEFAULT_WORD_LENGTH = 5
    val DEFAULT_DIFFICULTY = DifficultyLevel.MEDIUM
    
    // File names
    const val COMMON_WORDS_FILE = "common-words.json"
    const val ALL_WORDS_FOLDER = "all_words"
    const val ALL_WORDS_FILE_PREFIX = "words_"
    const val ALL_WORDS_FILE_COUNT = 75
    
    // Game mechanics
    const val SCORE_PER_CORRECT_LETTER = 1.0
    const val SCORE_PER_PRESENT_LETTER = 0.5
    const val SCORE_PER_REMAINING_ATTEMPT = 3.0
    const val HINT_PENALTY = 0.5
    
    // Animation durations (milliseconds)
    const val TILE_FLIP_DURATION = 600L
    const val TILE_FLIP_DELAY_PER_TILE = 100L
    const val SHAKE_DURATION = 300L
    const val TOAST_DURATION = 3000L
    
    // UI Constants
    const val MIN_TILE_SIZE = 20
    const val MAX_TILE_SIZE = 70
    const val KEYBOARD_HEIGHT = 320
    
    // QWERTY keyboard layout
    val QWERTY_ROWS = listOf(
        listOf("Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P"),
        listOf("A", "S", "D", "F", "G", "H", "J", "K", "L"),
        listOf("ENTER", "Z", "X", "C", "V", "B", "N", "M", "DELETE")
    )
}

/**
 * Color scheme for the game
 */
object GameColors {
    // Letter states
    val CORRECT = Color(0xFF6AAA64)      // Green
    val PRESENT = Color(0xFFC9B458)      // Yellow
    val ABSENT = Color(0xFF787C7E)       // Gray
    val EMPTY = Color(0xFFFFFFFF)        // White
    
    // Borders and outlines
    val BORDER_DEFAULT = Color(0xFFD3D6DA)
    val BORDER_DARK = Color(0xFF3A3A3C)
    
    // Text colors
    val TEXT_PRIMARY = Color(0xFF1A1A1B)
    val TEXT_DARK = Color(0xFFFFFFFF)
    val TEXT_SECONDARY = Color(0xFF787C7E)
    
    // Background colors
    val BACKGROUND_LIGHT = Color(0xFFFFFFFF)
    val BACKGROUND_DARK = Color(0xFF1A1A1B)
    val CARD_BACKGROUND_LIGHT = Color(0xFFF6F7F8)
    val CARD_BACKGROUND_DARK = Color(0xFF2A2A2C)
    
    // Accent colors
    val PRIMARY = Color(0xFF6AAA64)
    val SUCCESS = Color(0xFF6AAA64)
    val WARNING = Color(0xFFC9B458)
    val ERROR = Color(0xFFD73A49)
}
