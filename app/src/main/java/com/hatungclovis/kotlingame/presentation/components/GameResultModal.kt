package com.hatungclovis.kotlingame.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.hatungclovis.kotlingame.domain.models.GameState
import com.hatungclovis.kotlingame.utils.GameUtils
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun GameResultModal(
    gameWon: Boolean,
    gameState: GameState,
    onPlayAgain: () -> Unit,
    onGoHome: () -> Unit,
    onShare: () -> Unit
) {
    Dialog(
        onDismissRequest = { onGoHome() }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (gameWon) {
                    WinContent(gameState = gameState)
                } else {
                    LoseContent(gameState = gameState)
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                GameSummary(gameState = gameState)
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    OutlinedButton(
                        onClick = onGoHome,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Home")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = onPlayAgain,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Play Again")
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Share button
                TextButton(
                    onClick = onShare,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Share Result")
                }
            }
        }
    }
}

@Composable
fun WinContent(gameState: GameState) {
    // Celebration animation
    val infiniteTransition = rememberInfiniteTransition(label = "celebration")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Confetti background
        ConfettiEffect(modifier = Modifier.size(200.dp))
        
        Text(
            text = "🎉",
            fontSize = 64.sp,
            modifier = Modifier.scale(scale)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Congratulations!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = "You guessed the word!",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = gameState.targetWord,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun LoseContent(gameState: GameState) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "😔",
            fontSize = 64.sp
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Game Over",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error
        )
        
        Text(
            text = "Better luck next time!",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "The word was:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        
        Text(
            text = gameState.targetWord,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun GameSummary(gameState: GameState) {
    val attemptsUsed = gameState.guesses.size
    val maxAttempts = GameUtils.getMaxAttempts(gameState.difficulty)
    val timeElapsed = System.currentTimeMillis() - gameState.startTime
    val timeFormatted = GameUtils.formatTime(timeElapsed)
    
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Game Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            SummaryRow("Difficulty", gameState.difficulty.name)
            SummaryRow("Word Length", "${gameState.wordLength} letters")
            SummaryRow("Attempts", "$attemptsUsed / $maxAttempts")
            SummaryRow("Score", gameState.score.toString())
            SummaryRow("Time", timeFormatted)
            if (gameState.hintsUsed > 0) {
                SummaryRow("Hints Used", gameState.hintsUsed.toString())
            }
        }
    }
}

@Composable
fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun ConfettiEffect(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "confetti")
    
    val animationProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000),
            repeatMode = RepeatMode.Restart
        ),
        label = "progress"
    )
    
    Canvas(modifier = modifier) {
        val colors = listOf(
            Color.Red,
            Color.Green,
            Color.Blue,
            Color.Yellow,
            Color.Magenta,
            Color.Cyan
        )
        
        repeat(50) { index ->
            val random = Random(index)
            val color = colors[index % colors.size]
            val startX = size.width * random.nextFloat()
            val startY = -20f
            
            val endX = startX + (random.nextFloat() - 0.5f) * 200f
            val endY = size.height + 20f
            
            val currentY = startY + (endY - startY) * animationProgress
            val currentX = startX + (endX - startX) * animationProgress * 0.5f
            
            // Add some rotation and scaling
            val rotation = animationProgress * 360f * (random.nextFloat() * 2f - 1f)
            val scale = 1f + sin(animationProgress * 3.14f) * 0.5f
            
            if (currentY < size.height + 20f) {
                drawCircle(
                    color = color,
                    radius = 3f * scale,
                    center = Offset(currentX, currentY)
                )
            }
        }
    }
}
