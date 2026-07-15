package com.example.chesschain.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Highlight,
    onPrimary = Color.Black,
    secondary = LightBoard,
    onSecondary = DarkBoard,
    background = DarkBoard,
    surface = Color(0xFF1F251F)
)

private val LightColorScheme = lightColorScheme(
    primary = Highlight,
    onPrimary = Color.Black,
    secondary = DarkBoard,
    onSecondary = LightBoard,
    background = Color.White,
    surface = LightBoard
)

@Composable
fun ChessChainTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
