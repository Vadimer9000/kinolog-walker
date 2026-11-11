package com.kinolog.walker.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.kinolog.walker.util.ThemeMode

private val LightColorScheme = lightColorScheme(
    primary = SageGreenPrimary,
    onPrimary = LightDeepForest,
    primaryContainer = LightSoftMint,
    onPrimaryContainer = LightDeepForest,
    secondary = LightSandBeige,
    onSecondary = LightCharcoalPrimary,
    background = LightCanvasAlabaster,
    onBackground = LightCharcoalPrimary,
    surface = LightGlassSurface,
    onSurface = LightCharcoalPrimary,
    surfaceVariant = LightSandBeigeLight,
    onSurfaceVariant = LightOliveGreySecondary,
    outline = LightDividerMuted
)

private val DarkColorScheme = darkColorScheme(
    primary = SageGreenPrimary,
    onPrimary = DarkDeepForest,
    primaryContainer = DarkSoftMint,
    onPrimaryContainer = DarkDeepForest,
    secondary = DarkSandBeige,
    onSecondary = DarkCharcoalPrimary,
    background = DarkCanvasAlabaster,
    onBackground = DarkCharcoalPrimary,
    surface = DarkGlassSurface,
    onSurface = DarkCharcoalPrimary,
    surfaceVariant = DarkSandBeigeLight,
    onSurfaceVariant = DarkOliveGreySecondary,
    outline = DarkDividerMuted
)

@Composable
fun KinologWalkerTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val systemInDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> systemInDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val colorScheme = if (isDark) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = (if (isDark) DarkCanvasAlabaster else LightCanvasAlabaster).toArgb()
                window.navigationBarColor = (if (isDark) DarkCanvasBeige else LightCanvasBeige).toArgb()
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = !isDark
                    isAppearanceLightNavigationBars = !isDark
                }
            }
        }
    }

    CompositionLocalProvider(LocalIsDarkTheme provides isDark) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
