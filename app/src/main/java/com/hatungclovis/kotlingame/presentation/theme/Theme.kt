package com.hatungclovis.kotlingame.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Custom color scheme for game-specific colors
data class GameColors(
    val tileBorder: Color,
    val tileBackground: Color,
    val keyboardBackground: Color,
    val keyBackground: Color,
    val keyText: Color,
    val correctTile: Color,
    val presentTile: Color,
    val absentTile: Color,
    val emptyTile: Color,
)

// Composition local for game colors
val LocalGameColors = staticCompositionLocalOf {
    GameColors(
        tileBorder = GameColorPalette.Light.TileBorder,
        tileBackground = GameColorPalette.Light.TileBackground,
        keyboardBackground = GameColorPalette.Light.KeyboardBackground,
        keyBackground = GameColorPalette.Light.KeyBackground,
        keyText = GameColorPalette.Light.KeyText,
        correctTile = GameColorPalette.Correct,
        presentTile = GameColorPalette.Present,
        absentTile = GameColorPalette.Absent,
        emptyTile = GameColorPalette.Empty,
    )
}

// Light color scheme
private val LightColorScheme = lightColorScheme(
    primary = GameColorPalette.Light.Primary,
    onPrimary = GameColorPalette.Light.OnPrimary,
    secondary = GameColorPalette.Light.Secondary,
    onSecondary = GameColorPalette.Light.OnSecondary,
    background = GameColorPalette.Light.Background,
    onBackground = GameColorPalette.Light.OnBackground,
    surface = GameColorPalette.Light.Surface,
    onSurface = GameColorPalette.Light.OnSurface,
    surfaceVariant = GameColorPalette.Light.SurfaceVariant,
    onSurfaceVariant = GameColorPalette.Light.OnSurfaceVariant,
    outline = GameColorPalette.Light.Outline,
    outlineVariant = GameColorPalette.Light.OutlineVariant,
    error = GameColorPalette.Light.Error,
    onError = GameColorPalette.Light.OnError,
)

// Dark color scheme
private val DarkColorScheme = darkColorScheme(
    primary = GameColorPalette.Dark.Primary,
    onPrimary = GameColorPalette.Dark.OnPrimary,
    secondary = GameColorPalette.Dark.Secondary,
    onSecondary = GameColorPalette.Dark.OnSecondary,
    background = GameColorPalette.Dark.Background,
    onBackground = GameColorPalette.Dark.OnBackground,
    surface = GameColorPalette.Dark.Surface,
    onSurface = GameColorPalette.Dark.OnSurface,
    surfaceVariant = GameColorPalette.Dark.SurfaceVariant,
    onSurfaceVariant = GameColorPalette.Dark.OnSurfaceVariant,
    outline = GameColorPalette.Dark.Outline,
    outlineVariant = GameColorPalette.Dark.OutlineVariant,
    error = GameColorPalette.Dark.Error,
    onError = GameColorPalette.Dark.OnError,
)

// Light game colors
private val LightGameColors = GameColors(
    tileBorder = GameColorPalette.Light.TileBorder,
    tileBackground = GameColorPalette.Light.TileBackground,
    keyboardBackground = GameColorPalette.Light.KeyboardBackground,
    keyBackground = GameColorPalette.Light.KeyBackground,
    keyText = GameColorPalette.Light.KeyText,
    correctTile = GameColorPalette.Correct,
    presentTile = GameColorPalette.Present,
    absentTile = GameColorPalette.Absent,
    emptyTile = GameColorPalette.Empty,
)

// Dark game colors
private val DarkGameColors = GameColors(
    tileBorder = GameColorPalette.Dark.TileBorder,
    tileBackground = GameColorPalette.Dark.TileBackground,
    keyboardBackground = GameColorPalette.Dark.KeyboardBackground,
    keyBackground = GameColorPalette.Dark.KeyBackground,
    keyText = GameColorPalette.Dark.KeyText,
    correctTile = GameColorPalette.Correct,
    presentTile = GameColorPalette.Present,
    absentTile = GameColorPalette.Absent,
    emptyTile = GameColorPalette.Empty,
)

@Composable
fun KotlinGameTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val gameColors = if (darkTheme) DarkGameColors else LightGameColors

    CompositionLocalProvider(LocalGameColors provides gameColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = GameTypography,
            shapes = GameShapes,
            content = content
        )
    }
}

// Extension to access game colors easily
val MaterialTheme.gameColors: GameColors
    @Composable
    get() = LocalGameColors.current
