package com.onemail.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

val Background = Color(0xFF101814)
val CardSurface = Color(0xFF19241E)
val SurfaceHigh = Color(0xFF1F2B24)
val Mint = Color(0xFF87D6B0)
val OnMint = Color(0xFF08291B)
val OnSurface = Color(0xFFE2ECE6)
val OnSurfaceVariant = Color(0xFF8D9E94)
val ErrorRed = Color(0xFFFFB4AB)

private val OneMailColorScheme = darkColorScheme(
    primary = Mint,
    onPrimary = OnMint,
    primaryContainer = Color(0xFF1F3B2C),
    onPrimaryContainer = Color(0xFFB6F0D3),
    secondary = Mint,
    onSecondary = OnMint,
    secondaryContainer = SurfaceHigh,
    onSecondaryContainer = OnSurface,
    tertiary = Color(0xFF9CCFB8),
    onTertiary = OnMint,
    background = Background,
    onBackground = OnSurface,
    surface = Background,
    onSurface = OnSurface,
    surfaceVariant = CardSurface,
    onSurfaceVariant = OnSurfaceVariant,
    surfaceTint = Mint,
    inverseSurface = OnSurface,
    inverseOnSurface = Background,
    inversePrimary = Color(0xFF1B6B4A),
    error = ErrorRed,
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF3B1512),
    onErrorContainer = Color(0xFFFFDAD6),
    outline = Color(0xFF3C4C43),
    outlineVariant = Color(0xFF2A362F),
    scrim = Color(0xFF000000),
    surfaceBright = Color(0xFF1B2620),
    surfaceDim = Color(0xFF0B100D),
    surfaceContainerLowest = Color(0xFF0B100D),
    surfaceContainerLow = Color(0xFF131C17),
    surfaceContainer = CardSurface,
    surfaceContainerHigh = SurfaceHigh,
    surfaceContainerHighest = Color(0xFF26332C)
)

private val OneMailShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun OneMailTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = Color.TRANSPARENT
            window.navigationBarColor = Background.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }
    MaterialTheme(
        colorScheme = OneMailColorScheme,
        shapes = OneMailShapes,
        typography = OneMailTypography,
        content = content
    )
}
