package com.xpike.fit.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Lime = Color(0xFFC8F16A)
private val Ink = Color(0xFF1C211A)
private val MutedInk = Color(0xFF747B70)
private val LightCanvas = Color(0xFFF4F6F1)
private val DarkCanvas = Color(0xFF10120F)
private val DarkSurface = Color(0xFF1A1D19)

private val LightColors = lightColorScheme(
    primary = Lime,
    onPrimary = Ink,
    primaryContainer = Color(0xFFE7F8C9),
    onPrimaryContainer = Ink,
    secondary = Color(0xFF598229),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE7F8C9),
    onSecondaryContainer = Ink,
    background = LightCanvas,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    surfaceVariant = Color(0xFFEDF0E9),
    onSurfaceVariant = MutedInk,
    outline = Color(0xFFDDE2D7),
    error = Color(0xFFB3261E),
)

private val DarkColors = darkColorScheme(
    primary = Lime,
    onPrimary = Color(0xFF17200B),
    primaryContainer = Color(0xFF35451F),
    onPrimaryContainer = Color(0xFFE8F8C9),
    secondary = Color(0xFFB6D989),
    onSecondary = Color(0xFF273616),
    secondaryContainer = Color(0xFF35451F),
    onSecondaryContainer = Color(0xFFE8F8C9),
    background = DarkCanvas,
    onBackground = Color(0xFFF1F3EE),
    surface = DarkSurface,
    onSurface = Color(0xFFF1F3EE),
    surfaceVariant = Color(0xFF252923),
    onSurfaceVariant = Color(0xFFB4B9AE),
    outline = Color(0xFF3A4038),
    error = Color(0xFFFFB4AB),
)

@Composable
fun FitTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
