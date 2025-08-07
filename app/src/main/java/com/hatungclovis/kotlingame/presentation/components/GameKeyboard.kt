package com.hatungclovis.kotlingame.presentation.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hatungclovis.kotlingame.domain.models.GameConstants
import com.hatungclovis.kotlingame.domain.models.LetterState
import com.hatungclovis.kotlingame.presentation.theme.*
import com.hatungclovis.kotlingame.utils.triggerLightHaptic

/**
 * QWERTY keyboard component for the game
 */
@Composable
fun GameKeyboard(
    onKeyPressed: (String) -> Unit,
    onEnterPressed: () -> Unit,
    onDeletePressed: () -> Unit,
    keyStates: Map<String, LetterState> = emptyMap(),
    currentGuess: String = "",
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.gameColors.keyboardBackground)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Current guess display
        if (currentGuess.isNotEmpty()) {
            Text(
                text = currentGuess.uppercase(),
                style = GameTextStyles.CurrentGuess,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
        
        // Keyboard rows
        GameConstants.QWERTY_ROWS.forEachIndexed { rowIndex, row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(
                    if (rowIndex == 2) 4.dp else 6.dp
                ),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                row.forEach { key ->
                    when (key) {
                        "ENTER" -> {
                            SpecialKey(
                                text = "ENTER",
                                onClick = {
                                    if (enabled) {
                                        context.triggerLightHaptic()
                                        onEnterPressed()
                                    }
                                },
                                enabled = enabled && currentGuess.length >= 3,
                                modifier = Modifier.weight(1.5f)
                            )
                        }
                        "DELETE" -> {
                            SpecialKey(
                                icon = true,
                                onClick = {
                                    if (enabled) {
                                        context.triggerLightHaptic()
                                        onDeletePressed()
                                    }
                                },
                                enabled = enabled && currentGuess.isNotEmpty(),
                                modifier = Modifier.weight(1.5f)
                            )
                        }
                        else -> {
                            LetterKey(
                                letter = key,
                                state = keyStates[key] ?: LetterState.EMPTY,
                                onClick = {
                                    if (enabled) {
                                        context.triggerLightHaptic()
                                        onKeyPressed(key)
                                    }
                                },
                                enabled = enabled,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Individual letter key
 */
@Composable
private fun LetterKey(
    letter: String,
    state: LetterState,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    var pressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        finishedListener = { pressed = false },
        label = "key_scale"
    )
    
    val (backgroundColor, textColor) = getKeyColors(state, enabled)
    
    Box(
        modifier = modifier
            .scale(scale)
            .height(56.dp)
            .background(
                color = backgroundColor,
                shape = GameComponentShapes.KeyboardKey
            )
            .clickable(enabled = enabled) {
                pressed = true
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = letter,
            style = GameTextStyles.KeyboardKey,
            color = textColor,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Special key (ENTER, DELETE)
 */
@Composable
private fun SpecialKey(
    text: String = "",
    icon: Boolean = false,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    var pressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        finishedListener = { pressed = false },
        label = "special_key_scale"
    )
    
    val backgroundColor = if (enabled) {
        MaterialTheme.gameColors.keyBackground
    } else {
        MaterialTheme.gameColors.keyBackground.copy(alpha = 0.5f)
    }
    
    val textColor = if (enabled) {
        MaterialTheme.gameColors.keyText
    } else {
        MaterialTheme.gameColors.keyText.copy(alpha = 0.5f)
    }
    
    Box(
        modifier = modifier
            .scale(scale)
            .height(56.dp)
            .background(
                color = backgroundColor,
                shape = GameComponentShapes.KeyboardKey
            )
            .clickable(enabled = enabled) {
                pressed = true
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        if (icon) {
            Icon(
                imageVector = Icons.Default.Backspace,
                contentDescription = "Delete",
                tint = textColor,
                modifier = Modifier.size(20.dp)
            )
        } else {
            Text(
                text = text,
                style = GameTextStyles.KeyboardSpecial,
                color = textColor,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun getKeyColors(state: LetterState, enabled: Boolean): Pair<Color, Color> {
    val gameColors = MaterialTheme.gameColors
    
    if (!enabled) {
        return Pair(
            gameColors.keyBackground.copy(alpha = 0.5f),
            gameColors.keyText.copy(alpha = 0.5f)
        )
    }
    
    return when (state) {
        LetterState.CORRECT -> Pair(
            gameColors.correctTile,
            Color.White
        )
        LetterState.PRESENT -> Pair(
            gameColors.presentTile,
            Color.White
        )
        LetterState.ABSENT -> Pair(
            gameColors.absentTile,
            Color.White
        )
        LetterState.EMPTY -> Pair(
            gameColors.keyBackground,
            gameColors.keyText
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GameKeyboardPreview() {
    KotlinGameTheme {
        Surface {
            GameKeyboard(
                onKeyPressed = { },
                onEnterPressed = { },
                onDeletePressed = { },
                keyStates = mapOf(
                    "Q" to LetterState.CORRECT,
                    "W" to LetterState.PRESENT,
                    "E" to LetterState.ABSENT,
                    "A" to LetterState.CORRECT,
                    "S" to LetterState.ABSENT
                ),
                currentGuess = "WORD",
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GameKeyboardDarkPreview() {
    KotlinGameTheme(darkTheme = true) {
        Surface {
            GameKeyboard(
                onKeyPressed = { },
                onEnterPressed = { },
                onDeletePressed = { },
                keyStates = mapOf(
                    "Q" to LetterState.CORRECT,
                    "W" to LetterState.PRESENT,
                    "E" to LetterState.ABSENT
                ),
                currentGuess = "TEST",
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
