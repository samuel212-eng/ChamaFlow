package com.chamaflow.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = ChamaBlue, onPrimary = ChamaSurface,
    primaryContainer = ChamaBlueLight, onPrimaryContainer = ChamaBlueDark,
    secondary = ChamaGreen, onSecondary = ChamaSurface,
    secondaryContainer = ChamaGreenLight, onSecondaryContainer = ChamaGreenDark,
    tertiary = ChamaGold, tertiaryContainer = ChamaGoldLight,
    background = ChamaBackground, surface = ChamaSurface,
    onBackground = ChamaTextPrimary, onSurface = ChamaTextPrimary,
    outline = ChamaOutline, error = ChamaRed, errorContainer = ChamaRedLight
)

private val DarkColorScheme = darkColorScheme(
    primary = ChamaBlueLight, onPrimary = ChamaBlueDark,
    primaryContainer = ChamaBlueDark, secondary = ChamaGreenLight,
    onSecondary = ChamaGreenDark, background = ChamaBackgroundDark,
    surface = ChamaSurfaceDark, outline = ChamaOutlineDark
)

@Composable
fun ChamaFlowTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }
    MaterialTheme(colorScheme = colorScheme, typography = Typography(), content = content)
}
