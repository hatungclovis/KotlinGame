package com.hatungclovis.kotlingame.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.hatungclovis.kotlingame.domain.models.LetterState
import com.hatungclovis.kotlingame.presentation.viewmodel.GameTileState
import kotlinx.coroutines.delay

@Composable
fun GameBoard(
    board: List<List<GameTileState>>,
    currentRow: Int,
    showInvalidAnimation: Boolean = false,
    modifier: Modifier = Modifier,
    onAnimationComplete: () -> Unit = {}
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    
    // Calculate tile size based on screen size and word length
    val wordLength = board.firstOrNull()?.size ?: 5
    val maxTileSize = 56.dp
    val minTileSize = 32.dp
    val padding = 16.dp
    val spacing = 4.dp
    
    val availableWidth = screenWidth - (padding * 2)
    val totalSpacing = spacing * (wordLength - 1)
    val calculatedTileSize = ((availableWidth - totalSpacing) / wordLength)
        .coerceIn(minTileSize, maxTileSize)

    // Animation states
    var triggerFlipAnimation by remember { mutableStateOf(false) }
    var animatingRow by remember { mutableIntStateOf(-1) }
    
    // Shake animation for invalid guesses
    val shakeAnimation = remember { Animatable(0f) }
    
    LaunchedEffect(showInvalidAnimation) {
        if (showInvalidAnimation) {
            repeat(3) {
                shakeAnimation.animateTo(10f, tween(50))
                shakeAnimation.animateTo(-10f, tween(50))
            }
            shakeAnimation.animateTo(0f, tween(50))
            onAnimationComplete()
        }
    }
    
    // Trigger flip animation when a new guess is submitted
    LaunchedEffect(currentRow) {
        if (currentRow > 0) {
            animatingRow = currentRow - 1
            triggerFlipAnimation = !triggerFlipAnimation
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .offset(x = shakeAnimation.value.dp)
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(wordLength),
            horizontalArrangement = Arrangement.spacedBy(spacing),
            verticalArrangement = Arrangement.spacedBy(spacing),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = padding)
        ) {
            itemsIndexed(
                board.flatten().chunked(wordLength).flatten()
            ) { index, tileState ->
                val rowIndex = index / wordLength
                val colIndex = index % wordLength
                
                // Determine if this tile should animate
                val shouldAnimate = rowIndex == animatingRow
                val animationDelay = if (shouldAnimate) colIndex * 100 else 0
                
                GameBoardTile(
                    state = tileState,
                    size = calculatedTileSize,
                    shouldAnimate = shouldAnimate,
                    animationDelay = animationDelay,
                    trigger = triggerFlipAnimation
                )
            }
        }
    }
}

@Composable
fun GameBoardTile(
    state: GameTileState,
    size: androidx.compose.ui.unit.Dp,
    shouldAnimate: Boolean = false,
    animationDelay: Int = 0,
    trigger: Boolean = false
) {
    // Flip animation
    var isFlipped by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped && shouldAnimate) 180f else 0f,
        animationSpec = tween(
            durationMillis = 600,
            delayMillis = animationDelay,
            easing = EaseInOut
        ),
        finishedListener = {
            if (shouldAnimate && isFlipped) {
                // Animation completed
            }
        }
    )
    
    // Trigger flip animation when needed
    LaunchedEffect(trigger) {
        if (shouldAnimate) {
            delay(animationDelay.toLong())
            isFlipped = true
        }
    }
    
    // Show the back side (with color) when rotation > 90 degrees
    val showBack = rotation > 90f
    
    Box(
        modifier = Modifier
            .size(size)
            .rotate(rotation),
        contentAlignment = Alignment.Center
    ) {
        GameTile(
            letter = state.letter,
            state = if (shouldAnimate && !showBack) LetterState.Empty else state.state,
            size = size,
            animate = false // We're handling animation here
        )
    }
}

@Composable
fun ResponsiveGameBoard(
    board: List<List<GameTileState>>,
    currentRow: Int,
    showInvalidAnimation: Boolean = false,
    modifier: Modifier = Modifier,
    onAnimationComplete: () -> Unit = {}
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.screenWidthDp > configuration.screenHeightDp
    
    if (isLandscape) {
        // In landscape, use more compact spacing
        GameBoard(
            board = board,
            currentRow = currentRow,
            showInvalidAnimation = showInvalidAnimation,
            modifier = modifier,
            onAnimationComplete = onAnimationComplete
        )
    } else {
        // In portrait, use standard spacing
        GameBoard(
            board = board,
            currentRow = currentRow,
            showInvalidAnimation = showInvalidAnimation,
            modifier = modifier,
            onAnimationComplete = onAnimationComplete
        )
    }
}
