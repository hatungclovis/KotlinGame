package com.hatungclovis.kotlingame.presentation.theme

import androidx.compose.ui.graphics.Color

/**
 * Color palette for the Kotlin Wordle Game
 * Based on the original Wordle color scheme with dark mode support
 */
object GameColorPalette {
    
    // Letter state colors (consistent with GameColors from domain)
    val Correct = Color(0xFF6AAA64)           // Green
    val Present = Color(0xFFC9B458)           // Yellow  
    val Absent = Color(0xFF787C7E)            // Gray
    val Empty = Color(0xFFFFFFFF)             // White
    
    // Light theme colors
    object Light {
        val Primary = Color(0xFF6AAA64)
        val OnPrimary = Color.White
        val Secondary = Color(0xFFC9B458)
        val OnSecondary = Color.Black
        
        val Background = Color(0xFFFFFFFF)
        val OnBackground = Color(0xFF1A1A1B)
        val Surface = Color(0xFFF6F7F8)
        val OnSurface = Color(0xFF1A1A1B)
        
        val SurfaceVariant = Color(0xFFE1E5E9)
        val OnSurfaceVariant = Color(0xFF44474E)
        
        val Outline = Color(0xFFD3D6DA)
        val OutlineVariant = Color(0xFFC4C7C5)
        
        val Error = Color(0xFFD73A49)
        val OnError = Color.White
        
        // Game-specific colors
        val TileBorder = Color(0xFFD3D6DA)
        val TileBackground = Color.White
        val KeyboardBackground = Color(0xFFD3D6DA)
        val KeyBackground = Color(0xFFD3D6DA)
        val KeyText = Color(0xFF1A1A1B)
    }
    
    // Dark theme colors
    object Dark {
        val Primary = Color(0xFF6AAA64)
        val OnPrimary = Color.White
        val Secondary = Color(0xFFC9B458)
        val OnSecondary = Color.Black
        
        val Background = Color(0xFF1A1A1B)
        val OnBackground = Color(0xFFE1E3E6)
        val Surface = Color(0xFF2A2A2C)
        val OnSurface = Color(0xFFE1E3E6)
        
        val SurfaceVariant = Color(0xFF44474E)
        val OnSurfaceVariant = Color(0xFFC4C7C5)
        
        val Outline = Color(0xFF8E9297)
        val OutlineVariant = Color(0xFF44474E)
        
        val Error = Color(0xFFCF6679)
        val OnError = Color.Black
        
        // Game-specific colors
        val TileBorder = Color(0xFF3A3A3C)
        val TileBackground = Color(0xFF2A2A2C)
        val KeyboardBackground = Color(0xFF2A2A2C)
        val KeyBackground = Color(0xFF818384)
        val KeyText = Color.White
    }
    
    // State-specific colors that work in both themes
    val Success = Correct
    val Warning = Present
    val Neutral = Absent
}
