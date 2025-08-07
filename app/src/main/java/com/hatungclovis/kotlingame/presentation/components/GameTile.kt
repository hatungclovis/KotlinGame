package com.hatungclovis.kotlingame.presentation.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hatungclovis.kotlingame.domain.models.LetterState
import com.hatungclovis.kotlingame.presentation.theme.*
import kotlinx.coroutines.delay

/**
 * Individual letter tile component for the game board
 */
@Composable
fun GameTile(
    letter: String,
    state: LetterState = LetterState.EMPTY,
    size: Dp = 60.dp,
    animateReveal: Boolean = false,
    animationDelay: Long = 0L,
    modifier: Modifier = Modifier
) {
    var revealed by remember { mutableStateOf(!animateReveal) }
    
    // Animation for tile flip
    val flipRotation by animateFloatAsState(
        targetValue = if (revealed) 0f else 180f,
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        ),
        label = "flip_rotation"
    )
    
    // Animation for scale (pop effect)
    var hasLetter by remember { mutableStateOf(letter.isNotEmpty()) }
    val scale by animateFloatAsState(
        targetValue = if (hasLetter) 1.05f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        finishedListener = {
            if (hasLetter && it == 1.05f) {
                hasLetter = false
            }
        },
        label = "tile_scale"
    )
    
    // Trigger reveal animation after delay
    LaunchedEffect(animateReveal, animationDelay) {
        if (animateReveal && !revealed) {
            delay(animationDelay)
            revealed = true
        }
    }
    
    // Update letter presence for animation
    LaunchedEffect(letter) {
        if (letter.isNotEmpty()) {
            hasLetter = true
        }
    }
    
    // Get tile colors based on state
    val (backgroundColor, borderColor, textColor) = getTileColors(state)
    
    Box(
        modifier = modifier
            .size(size)
            .scale(scale),
        contentAlignment = Alignment.Center
    ) {
        // Tile background with border
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = backgroundColor,
                    shape = GameComponentShapes.Tile
                )
                .border(
                    width = 2.dp,
                    color = borderColor,
                    shape = GameComponentShapes.Tile
                ),
            contentAlignment = Alignment.Center
        ) {
            // Letter text
            Text(
                text = letter.uppercase(),
                style = GameTextStyles.TileLetter.copy(
                    fontSize = (size.value * 0.5).sp,
                    color = textColor
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun getTileColors(state: LetterState): Triple<Color, Color, Color> {
    val gameColors = MaterialTheme.gameColors
    
    return when (state) {
        LetterState.CORRECT -> Triple(
            gameColors.correctTile,
            gameColors.correctTile,
            Color.White
        )
        LetterState.PRESENT -> Triple(
            gameColors.presentTile,
            gameColors.presentTile,
            Color.White
        )
        LetterState.ABSENT -> Triple(
            gameColors.absentTile,
            gameColors.absentTile,
            Color.White
        )
        LetterState.EMPTY -> Triple(
            gameColors.tileBackground,
            gameColors.tileBorder,
            MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Row of game tiles for a single guess
 */
@Composable
fun GameTileRow(
    word: String,
    states: List<LetterState> = emptyList(),
    wordLength: Int = 5,
    tileSize: Dp = 60.dp,
    spacing: Dp = 4.dp,
    animateReveal: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        repeat(wordLength) { index ->
            val letter = word.getOrNull(index)?.toString() ?: ""
            val state = states.getOrNull(index) ?: LetterState.EMPTY
            val animationDelay = if (animateReveal) index * 100L else 0L
            
            GameTile(
                letter = letter,
                state = state,
                size = tileSize,
                animateReveal = animateReveal,
                animationDelay = animationDelay
            )
        }
    }
}

/**
 * Current guess row with typing animation
 */
@Composable
fun CurrentGuessRow(
    currentGuess: String,
    wordLength: Int = 5,
    tileSize: Dp = 60.dp,
    spacing: Dp = 4.dp,
    modifier: Modifier = Modifier
) {
    var lastGuessLength by remember { mutableStateOf(0) }
    
    LaunchedEffect(currentGuess.length) {
        lastGuessLength = currentGuess.length
    }
    
    Row(
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        repeat(wordLength) { index ->
            val letter = currentGuess.getOrNull(index)?.toString() ?: ""
            val showTypingAnimation = index == currentGuess.length - 1 && 
                    currentGuess.length > lastGuessLength
            
            GameTile(
                letter = letter,
                state = LetterState.EMPTY,
                size = tileSize,
                animateReveal = showTypingAnimation
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GameTilePreview() {
    KotlinGameTheme {
        Surface {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Empty tile
                GameTile(letter = "", state = LetterState.EMPTY)
                
                // Letter tiles with different states
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GameTile(letter = "C", state = LetterState.CORRECT)
                    GameTile(letter = "O", state = LetterState.PRESENT)
                    GameTile(letter = "D", state = LetterState.ABSENT)
                    GameTile(letter = "E", state = LetterState.EMPTY)
                }
                
                // Complete word row
                GameTileRow(
                    word = "HELLO",
                    states = listOf(
                        LetterState.CORRECT,
                        LetterState.ABSENT,
                        LetterState.PRESENT,
                        LetterState.PRESENT,
                        LetterState.CORRECT
                    ),
                    wordLength = 5
                )
                
                // Current guess row
                CurrentGuessRow(
                    currentGuess = "WOR",
                    wordLength = 5
                )
            }
        }
    }
}
