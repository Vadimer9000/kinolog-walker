package com.kinolog.walker.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = SageGreenPrimary,
    onPrimary = DeepForest,
    primaryContainer = SoftMint,
    onPrimaryContainer = DeepForest,
    secondary = SandBeige,
    onSecondary = CharcoalPrimary,
    background = CanvasAlabaster,
    onBackground = CharcoalPrimary,
    surface = GlassSurface,
    onSurface = CharcoalPrimary,
    surfaceVariant = SandBeigeLight,
    onSurfaceVariant = OliveGreySecondary,
    outline = DividerMuted
)

@Composable
fun KinologWalkerTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = CanvasAlabaster.toArgb()
                window.navigationBarColor = CanvasBeige.toArgb()
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = true
                    isAppearanceLightNavigationBars = true
                }
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
