package com.hatungclovis.kotlingame.presentation.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hatungclovis.kotlingame.domain.models.*
import com.hatungclovis.kotlingame.domain.repository.GameRepository
import com.hatungclovis.kotlingame.domain.repository.WordRepository
import com.hatungclovis.kotlingame.domain.usecase.GameEngine
import com.hatungclovis.kotlingame.utils.GameUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor(
    private val gameRepository: GameRepository,
    private val wordRepository: WordRepository
) : ViewModel() {

    // Current game state
    private val _gameState = mutableStateOf(
        GameState(
            targetWord = "",
            guesses = emptyList(),
            currentGuess = "",
            gameStatus = GameStatus.Playing,
            difficulty = DifficultyLevel.Easy,
            wordLength = 5,
            score = 0,
            hintsUsed = 0,
            startTime = System.currentTimeMillis()
        )
    )
    val gameState: State<GameState> = _gameState

    // UI state
    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    private val _toastMessage = mutableStateOf<String?>(null)
    val toastMessage: State<String?> = _toastMessage

    // Game events
    private val _gameEvents = MutableSharedFlow<GameEvent>()
    val gameEvents = _gameEvents.asSharedFlow()

    // Keyboard state - track letter states across all guesses
    private val _keyboardState = mutableStateOf<Map<Char, LetterState>>(emptyMap())
    val keyboardState: State<Map<Char, LetterState>> = _keyboardState

    init {
        clearToastMessage()
    }

    /**
     * Initialize a new game with the given difficulty and word length
     */
    fun startNewGame(difficulty: DifficultyLevel, wordLength: Int) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                // Get a random target word
                val targetWord = wordRepository.getRandomWordByLength(wordLength)
                    ?: throw Exception("No words found for length $wordLength")

                // Initialize game state
                _gameState.value = GameState(
                    targetWord = targetWord.uppercase(),
                    guesses = emptyList(),
                    currentGuess = "",
                    gameStatus = GameStatus.Playing,
                    difficulty = difficulty,
                    wordLength = wordLength,
                    score = 0,
                    hintsUsed = 0,
                    startTime = System.currentTimeMillis()
                )

                // Reset keyboard state
                _keyboardState.value = emptyMap()

                _isLoading.value = false

            } catch (e: Exception) {
                _isLoading.value = false
                _errorMessage.value = "Failed to start game: ${e.message}"
            }
        }
    }

    /**
     * Add a letter to the current guess
     */
    fun addLetter(letter: Char) {
        val currentState = _gameState.value
        if (currentState.gameStatus != GameStatus.Playing) return
        if (currentState.currentGuess.length >= currentState.wordLength) return

        _gameState.value = currentState.copy(
            currentGuess = currentState.currentGuess + letter.uppercase()
        )
    }

    /**
     * Remove the last letter from the current guess
     */
    fun deleteLetter() {
        val currentState = _gameState.value
        if (currentState.gameStatus != GameStatus.Playing) return
        if (currentState.currentGuess.isEmpty()) return

        _gameState.value = currentState.copy(
            currentGuess = currentState.currentGuess.dropLast(1)
        )
    }

    /**
     * Submit the current guess
     */
    fun submitGuess() {
        val currentState = _gameState.value
        if (currentState.gameStatus != GameStatus.Playing) return
        if (currentState.currentGuess.length != currentState.wordLength) {
            showToast("Word must be ${currentState.wordLength} letters long")
            viewModelScope.launch {
                _gameEvents.emit(GameEvent.InvalidGuess)
            }
            return
        }

        viewModelScope.launch {
            try {
                _isLoading.value = true

                // Check if word is valid
                val isValidWord = wordRepository.isValidWord(currentState.currentGuess)
                if (!isValidWord) {
                    showToast("Not a valid word")
                    _gameEvents.emit(GameEvent.InvalidGuess)
                    _isLoading.value = false
                    return@launch
                }

                // Process the guess
                val guess = GameEngine.createGuess(
                    currentState.currentGuess,
                    currentState.targetWord
                )

                val newGuesses = currentState.guesses + guess
                val isCorrect = GameEngine.isGameWon(guess)
                val attemptsUsed = newGuesses.size
                val maxAttempts = GameUtils.getMaxAttempts(currentState.difficulty)

                // Calculate score
                val newScore = GameEngine.calculateGameScore(
                    newGuesses,
                    maxAttempts - attemptsUsed,
                    currentState.hintsUsed
                )

                // Update keyboard state
                updateKeyboardState(guess)

                // Determine new game status
                val newGameStatus = when {
                    isCorrect -> GameStatus.Won
                    attemptsUsed >= maxAttempts -> GameStatus.Lost
                    else -> GameStatus.Playing
                }

                // Update game state
                _gameState.value = currentState.copy(
                    guesses = newGuesses,
                    currentGuess = "",
                    gameStatus = newGameStatus,
                    score = newScore
                )

                // Handle game completion
                if (newGameStatus != GameStatus.Playing) {
                    handleGameCompletion(newGameStatus == GameStatus.Won)
                }

                _isLoading.value = false

            } catch (e: Exception) {
                _isLoading.value = false
                _errorMessage.value = "Error processing guess: ${e.message}"
            }
        }
    }

    /**
     * Get a hint for the current game
     */
    fun getHint() {
        val currentState = _gameState.value
        if (currentState.gameStatus != GameStatus.Playing) return

        viewModelScope.launch {
            try {
                val availableHints = GameEngine.getAvailableHints(
                    currentState.targetWord,
                    currentState.guesses
                )

                if (availableHints.isEmpty()) {
                    showToast("No more hints available")
                    return@launch
                }

                val hint = GameEngine.getHint(
                    currentState.targetWord,
                    currentState.guesses
                )

                if (hint != null) {
                    _gameState.value = currentState.copy(
                        hintsUsed = currentState.hintsUsed + 1
                    )
                    val positionText = if (hint.position != null) " at position ${hint.position!! + 1}" else ""
                    showToast("Hint: '${hint.letter}'$positionText is in the word")
                    _gameEvents.emit(GameEvent.HintUsed(hint))
                } else {
                    showToast("No hints available")
                }

            } catch (e: Exception) {
                _errorMessage.value = "Error getting hint: ${e.message}"
            }
        }
    }

    /**
     * Update keyboard state based on all guesses
     */
    private fun updateKeyboardState(guess: Guess) {
        val allGuesses = _gameState.value.guesses + guess
        val keyboardStateMap = GameEngine.getKeyboardState(allGuesses)
        
        // Convert from String keys to Char keys
        val charKeyboardState = mutableMapOf<Char, LetterState>()
        keyboardStateMap.forEach { (letter, state) ->
            charKeyboardState[letter.first()] = state
        }
        
        _keyboardState.value = charKeyboardState
    }

    /**
     * Handle game completion (win/lose)
     */
    private suspend fun handleGameCompletion(won: Boolean) {
        val currentState = _gameState.value

        // Update statistics
        val stats = gameRepository.getStatistics() ?: GameStatistics()
        val updatedStats = GameUtils.updateStatistics(stats, currentState, won)
        gameRepository.saveStatistics(updatedStats)

        // Emit game completion event
        _gameEvents.emit(
            if (won) GameEvent.GameWon else GameEvent.GameLost
        )

        // Show completion message
        if (won) {
            showToast("Congratulations! You won!")
        } else {
            showToast("Game over! The word was '${currentState.targetWord}'")
        }
    }

    /**
     * Restart the current game with same settings
     */
    fun restartGame() {
        val currentState = _gameState.value
        startNewGame(currentState.difficulty, currentState.wordLength)
    }

    /**
     * Show a toast message
     */
    private fun showToast(message: String) {
        _toastMessage.value = message
    }

    /**
     * Clear the current toast message
     */
    fun clearToastMessage() {
        _toastMessage.value = null
    }

    /**
     * Clear error message
     */
    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    /**
     * Get the current game board state for UI
     */
    fun getGameBoardState(): List<List<GameTileState>> {
        val currentState = _gameState.value
        val maxAttempts = GameUtils.getMaxAttempts(currentState.difficulty)
        val board = mutableListOf<List<GameTileState>>()

        // Add completed guesses
        currentState.guesses.forEach { guess ->
            val row = guess.word.mapIndexed { index, letter ->
                GameTileState(
                    letter = letter,
                    state = guess.states[index]
                )
            }
            board.add(row)
        }

        // Add current guess row if game is still playing
        if (currentState.gameStatus == GameStatus.Playing) {
            val currentRow = (0 until currentState.wordLength).map { index ->
                GameTileState(
                    letter = if (index < currentState.currentGuess.length) {
                        currentState.currentGuess[index]
                    } else null,
                    state = LetterState.Empty
                )
            }
            board.add(currentRow)
        }

        // Add empty rows for remaining attempts
        while (board.size < maxAttempts) {
            val emptyRow = (0 until currentState.wordLength).map {
                GameTileState(letter = null, state = LetterState.Empty)
            }
            board.add(emptyRow)
        }

        return board
    }
}

/**
 * Events that can occur during gameplay
 */
sealed class GameEvent {
    object InvalidGuess : GameEvent()
    object GameWon : GameEvent()
    object GameLost : GameEvent()
    data class HintUsed(val hint: HintData) : GameEvent()
}

/**
 * State for individual game tiles
 */
data class GameTileState(
    val letter: Char?,
    val state: LetterState
)
