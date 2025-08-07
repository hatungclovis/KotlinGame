package com.hatungclovis.kotlingame.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Shape definitions for the game UI
 */
val GameShapes = Shapes(
    // Small components - buttons, chips
    small = RoundedCornerShape(8.dp),
    
    // Medium components - cards, dialogs
    medium = RoundedCornerShape(12.dp),
    
    // Large components - bottom sheets, large cards
    large = RoundedCornerShape(16.dp)
)

/**
 * Game-specific shapes
 */
object GameComponentShapes {
    // Letter tiles - slightly rounded square
    val Tile = RoundedCornerShape(4.dp)
    
    // Keyboard keys - rounded rectangle
    val KeyboardKey = RoundedCornerShape(6.dp)
    
    // Modal dialogs
    val Modal = RoundedCornerShape(20.dp)
    
    // Statistics cards
    val StatCard = RoundedCornerShape(12.dp)
    
    // Game board container
    val GameBoard = RoundedCornerShape(16.dp)
    
    // Settings toggles
    val Toggle = RoundedCornerShape(20.dp)
    
    // Buttons
    val Button = RoundedCornerShape(25.dp)
    
    // Selection chips (difficulty, word length)
    val SelectionChip = RoundedCornerShape(20.dp)
}
