package com.hatungclovis.kotlingame.domain.usecase

import com.hatungclovis.kotlingame.domain.models.*

/**
 * Game Engine - Handles word matching and game logic
 * Kotlin translation of the React Native GameEngine.ts
 */
object GameEngine {
    
    /**
     * Check a guess against the target word - equivalent to Python WordMatching.colouredWord()
     * Returns the letter states for each position
     */
    fun checkGuess(guess: String, targetWord: String): List<LetterState> {
        val guessUpper = guess.uppercase()
        val targetUpper = targetWord.uppercase()
        val result = MutableList(guess.length) { LetterState.ABSENT }
        
        // Track which letters have been used in the target word
        val targetLetterCounts = mutableMapOf<Char, Int>()
        
        // Count all letters in target word
        targetUpper.forEach { letter ->
            targetLetterCounts[letter] = targetLetterCounts.getOrDefault(letter, 0) + 1
        }
        
        // First pass: Mark correct positions (green) - equivalent to correctCharacters()
        for (i in guessUpper.indices) {
            if (guessUpper[i] == targetUpper[i]) {
                result[i] = LetterState.CORRECT
                targetLetterCounts[guessUpper[i]] = targetLetterCounts[guessUpper[i]]!! - 1
            }
        }
        
        // Second pass: Mark present but wrong position (yellow) - equivalent to halfCorrectCharacters()
        for (i in guessUpper.indices) {
            if (result[i] == LetterState.ABSENT) {
                val letter = guessUpper[i]
                if (targetLetterCounts.getOrDefault(letter, 0) > 0) {
                    result[i] = LetterState.PRESENT
                    targetLetterCounts[letter] = targetLetterCounts[letter]!! - 1
                }
            }
        }
        
        return result
    }
    
    /**
     * Create a Guess object with word and states
     */
    fun createGuess(word: String, targetWord: String): Guess {
        return Guess(
            word = word.uppercase(),
            states = checkGuess(word, targetWord)
        )
    }
    
    /**
     * Calculate game score - equivalent to Python score() function
     */
    fun calculateGameScore(
        guesses: List<Guess>,
        attemptsLeft: Int,
        hintsUsed: Int = 0
    ): Int {
        var correctChars = 0
        var presentChars = 0
        
        // Count all correct and present characters from all guesses
        guesses.forEach { guess ->
            guess.states.forEach { state ->
                when (state) {
                    LetterState.CORRECT -> correctChars++
                    LetterState.PRESENT -> presentChars++
                    else -> {}
                }
            }
        }
        
        // Apply hint penalty
        val hintPenalty = hintsUsed * GameConstants.HINT_PENALTY
        
        val baseScore = (correctChars * GameConstants.SCORE_PER_CORRECT_LETTER) +
                       (presentChars * GameConstants.SCORE_PER_PRESENT_LETTER) +
                       (attemptsLeft * GameConstants.SCORE_PER_REMAINING_ATTEMPT)
        
        return maxOf(0, (baseScore - hintPenalty).toInt())
    }
    
    /**
     * Get available hints for the current game state - equivalent to Python hintCharacters()
     */
    fun getAvailableHints(
        targetWord: String,
        guesses: List<Guess>
    ): List<String> {
        val guessedLetters = mutableSetOf<String>()
        val correctPositions = mutableSetOf<Int>()
        
        // Collect all guessed letters and correct positions
        guesses.forEach { guess ->
            guess.word.forEachIndexed { index, char ->
                guessedLetters.add(char.toString())
                if (guess.states[index] == LetterState.CORRECT) {
                    correctPositions.add(index)
                }
            }
        }
        
        val hints = mutableListOf<String>()
        
        targetWord.forEachIndexed { index, char ->
            val letter = char.toString().uppercase()
            
            // Only include if letter hasn't been guessed and position isn't correct
            if (!guessedLetters.contains(letter) && !correctPositions.contains(index)) {
                if (!hints.contains(letter)) {
                    hints.add(letter)
                }
            }
        }
        
        return hints
    }
    
    /**
     * Get a random hint - equivalent to Python hintedCharacter()
     */
    fun getHint(targetWord: String, guesses: List<Guess>): HintData? {
        val availableHints = getAvailableHints(targetWord, guesses)
        val randomLetter = availableHints.randomOrNull() ?: return null
        
        // Find positions where this letter appears
        val positions = mutableListOf<Int>()
        targetWord.forEachIndexed { index, char ->
            if (char.toString().uppercase() == randomLetter) {
                positions.add(index)
            }
        }
        
        return HintData(
            letter = randomLetter,
            position = positions.randomOrNull(),
            revealed = true
        )
    }
    
    /**
     * Check if the game is won
     */
    fun isGameWon(guess: Guess): Boolean {
        return guess.states.all { it == LetterState.CORRECT }
    }
    
    /**
     * Check if the game is lost
     */
    fun isGameLost(attemptsLeft: Int): Boolean {
        return attemptsLeft <= 0
    }
    
    /**
     * Get keyboard state based on all guesses - for UI keyboard coloring
     */
    fun getKeyboardState(guesses: List<Guess>): Map<String, LetterState> {
        val keyboardState = mutableMapOf<String, LetterState>()
        
        guesses.forEach { guess ->
            guess.word.forEachIndexed { index, char ->
                val letter = char.toString()
                val currentState = guess.states[index]
                val existingState = keyboardState[letter]
                
                // Priority: correct > present > absent
                if (existingState == null ||
                    currentState == LetterState.CORRECT ||
                    (currentState == LetterState.PRESENT && existingState == LetterState.ABSENT)
                ) {
                    keyboardState[letter] = currentState
                }
            }
        }
        
        return keyboardState
    }
    
    /**
     * Validate guess format and length
     */
    fun isValidGuessFormat(guess: String, targetLength: Int): Boolean {
        if (guess.length != targetLength) return false
        return guess.matches(Regex("[a-zA-Z]+"))
    }
    
    /**
     * Get game summary for sharing/statistics
     */
    fun getGameSummary(
        guesses: List<Guess>,
        targetWord: String,
        won: Boolean,
        score: Int,
        difficulty: DifficultyLevel,
        hintsUsed: Int
    ): GameSummary {
        val attempts = guesses.size
        val guessWords = guesses.map { it.word }
        
        val shareText = buildString {
            appendLine("Word Game CH - ${difficulty.label}")
            appendLine("Word: $targetWord")
            appendLine(if (won) "Solved in $attempts attempts" else "Not solved")
            appendLine("Score: $score")
            if (hintsUsed > 0) appendLine("Hints used: $hintsUsed")
            appendLine()
            appendLine("🎯 Enhanced Word Game")
        }
        
        return GameSummary(
            attempts = attempts,
            won = won,
            score = score,
            targetWord = targetWord,
            guessWords = guessWords,
            shareText = shareText
        )
    }
}

/**
 * Game summary data class for sharing
 */
data class GameSummary(
    val attempts: Int,
    val won: Boolean,
    val score: Int,
    val targetWord: String,
    val guessWords: List<String>,
    val shareText: String
)
