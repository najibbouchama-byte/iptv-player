package com.iptvplayer.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColors = darkColorScheme(
    background = BackgroundDark,
    surface = SurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    primary = AccentTeal,
    secondary = AccentTealDark,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onPrimary = BackgroundDark,
    error = ErrorRed
)

@Composable
fun IptvPlayerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // L'application est toujours en thème sombre, comme demandé ("interface moderne, sombre")
    MaterialTheme(
        colorScheme = DarkColors,
        typography = AppTypography,
        content = content
    )
}
