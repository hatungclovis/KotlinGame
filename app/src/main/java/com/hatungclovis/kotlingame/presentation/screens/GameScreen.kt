package com.hatungclovis.kotlingame.presentation.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.hatungclovis.kotlingame.domain.models.DifficultyLevel
import com.hatungclovis.kotlingame.domain.models.GameStatus
import com.hatungclovis.kotlingame.domain.models.LetterState
import com.hatungclovis.kotlingame.presentation.components.*
import com.hatungclovis.kotlingame.presentation.theme.GameTextStyles
import com.hatungclovis.kotlingame.presentation.theme.GameComponentShapes
import com.hatungclovis.kotlingame.presentation.theme.gameColors
import com.hatungclovis.kotlingame.presentation.viewmodel.GameEvent
import com.hatungclovis.kotlingame.presentation.viewmodel.GameViewModel
import com.hatungclovis.kotlingame.utils.GameUtils
import com.hatungclovis.kotlingame.utils.HapticUtils
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    difficulty: DifficultyLevel,
    wordLength: Int,
    navController: NavController,
    viewModel: GameViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val gameState by viewModel.gameState
    val isLoading by viewModel.isLoading
    val errorMessage by viewModel.errorMessage
    val toastMessage by viewModel.toastMessage
    val keyboardState by viewModel.keyboardState
    
    // UI state
    var showInvalidAnimation by remember { mutableStateOf(false) }
    var showGameResult by remember { mutableStateOf(false) }
    var gameWon by remember { mutableStateOf(false) }
    
    // Initialize game when screen opens
    LaunchedEffect(difficulty, wordLength) {
        viewModel.startNewGame(difficulty, wordLength)
    }
    
    // Handle game events
    LaunchedEffect(viewModel) {
        viewModel.gameEvents.collectLatest { event ->
            when (event) {
                is GameEvent.InvalidGuess -> {
                    HapticUtils.error(context)
                    showInvalidAnimation = true
                }
                is GameEvent.GameWon -> {
                    HapticUtils.success(context)
                    gameWon = true
                    showGameResult = true
                }
                is GameEvent.GameLost -> {
                    HapticUtils.error(context)
                    gameWon = false
                    showGameResult = true
                }
                is GameEvent.HintUsed -> {
                    HapticUtils.success(context)
                }
            }
        }
    }
    
    // Handle back button
    BackHandler {
        navController.navigateUp()
    }
    
    // Show toast messages
    LaunchedEffect(toastMessage) {
        toastMessage?.let { message ->
            // Here you would show a toast or snackbar
            // For now, we'll clear it after a delay
            kotlinx.coroutines.delay(2000)
            viewModel.clearToastMessage()
        }
    }

    Scaffold(
        topBar = {
            GameTopBar(
                difficulty = gameState.difficulty,
                wordLength = gameState.wordLength,
                score = gameState.score,
                attemptsLeft = GameUtils.getMaxAttempts(gameState.difficulty) - gameState.guesses.size,
                hintsUsed = gameState.hintsUsed,
                onBackClick = { navController.navigateUp() },
                onHintClick = { viewModel.getHint() }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Game Board
                ResponsiveGameBoard(
                    board = viewModel.getGameBoardState(),
                    currentRow = gameState.guesses.size,
                    showInvalidAnimation = showInvalidAnimation,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    onAnimationComplete = {
                        showInvalidAnimation = false
                    }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Current guess display
                if (gameState.currentGuess.isNotEmpty() && gameState.gameStatus == GameStatus.Playing) {
                    Text(
                        text = gameState.currentGuess,
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(8.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Game Keyboard
                GameKeyboard(
                    onKeyPressed = { key ->
                        HapticUtils.light(context)
                        viewModel.addLetter(key.first())
                    },
                    onEnterPressed = {
                        HapticUtils.impact(context)
                        viewModel.submitGuess()
                    },
                    onDeletePressed = {
                        HapticUtils.light(context)
                        viewModel.deleteLetter()
                    },
                    keyStates = keyboardState.mapKeys { it.key.toString() },
                    currentGuess = gameState.currentGuess,
                    enabled = gameState.gameStatus == GameStatus.Playing && !isLoading,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // Loading overlay
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.padding(32.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Processing...")
                        }
                    }
                }
            }
            
            // Error message
            errorMessage?.let { message ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.padding(32.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Error",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = message,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.clearErrorMessage() }
                            ) {
                                Text("OK")
                            }
                        }
                    }
                }
            }
            
            // Toast message
            toastMessage?.let { message ->
                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically() + fadeIn(),
                    exit = slideOutVertically() + fadeOut(),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.inverseSurface
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = message,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.inverseOnSurface
                        )
                    }
                }
            }
        }
    }
    
    // Game Result Modal
    if (showGameResult) {
        GameResultModal(
            gameWon = gameWon,
            gameState = gameState,
            onPlayAgain = {
                showGameResult = false
                viewModel.restartGame()
            },
            onGoHome = {
                showGameResult = false
                navController.navigateUp()
            },
            onShare = {
                // Implement share functionality
                GameUtils.shareGameResult(context, gameState, gameWon)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameTopBar(
    difficulty: DifficultyLevel,
    wordLength: Int,
    score: Int,
    attemptsLeft: Int,
    hintsUsed: Int,
    onBackClick: () -> Unit,
    onHintClick: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = "${difficulty.name} • ${wordLength} Letters",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Score: $score • $attemptsLeft attempts left",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        actions = {
            // Hint button
            IconButton(
                onClick = onHintClick,
                enabled = attemptsLeft > 0
            ) {
                Badge(
                    modifier = Modifier.offset(x = 8.dp, y = (-8).dp)
                ) {
                    if (hintsUsed > 0) {
                        Text(
                            text = hintsUsed.toString(),
                            fontSize = 10.sp
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = "Hint",
                    tint = if (hintsUsed > 0) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
            }
        }
    )
}
