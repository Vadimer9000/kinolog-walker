package com.kinolog.walker.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val LocalIsDarkTheme = staticCompositionLocalOf { false }

// Static Light Theme Colors
val LightCanvasAlabaster = Color(0xFFF8F6F1)
val LightCanvasBeige = Color(0xFFEFECE6)
val LightCanvasMeshGreen = Color(0x33BCE3CE)

val LightGlassSurface = Color(0xB8FFFFFF)
val LightGlassSurfaceSubtle = Color(0x99FDFCFA)
val LightGlassBorderTopLeft = Color.White.copy(alpha = 0.92f)
val LightGlassBorderBottomRight = Color(0xFFD4E6DC).copy(alpha = 0.45f)

val LightSoftMint = Color(0xFFBCE3CE)
val LightMintLight = Color(0xFFE7F5ED)
val LightDeepForest = Color(0xFF10281E)

val LightSandBeige = Color(0xFFE4DDD3)
val LightSandBeigeLight = Color(0xFFF3EFEA)
val LightCharcoalPrimary = Color(0xFF1A1D1B)
val LightOliveGreySecondary = Color(0xFF68706B)
val LightDividerMuted = Color(0xFFE3DED8)

// Soft Dark Theme Colors (Warm/Soft Slate & Forest Charcoal)
val DarkCanvasAlabaster = Color(0xFF141715)
val DarkCanvasBeige = Color(0xFF1C221E)
val DarkCanvasMeshGreen = Color(0x22365A46)

val DarkGlassSurface = Color(0xDD1F2521)
val DarkGlassSurfaceSubtle = Color(0xAA1A201C)
val DarkGlassBorderTopLeft = Color(0x44789484)
val DarkGlassBorderBottomRight = Color(0x22324238)

val DarkSoftMint = Color(0xFF284838)
val DarkMintLight = Color(0xFF20382B)
val DarkDeepForest = Color(0xFFD4F0DF)

val DarkSandBeige = Color(0xFF28302B)
val DarkSandBeigeLight = Color(0xFF1E2420)
val DarkCharcoalPrimary = Color(0xFFF0F4F2)
val DarkOliveGreySecondary = Color(0xFFA0ABA4)
val DarkDividerMuted = Color(0xFF2F3B34)

// Primary Accents (consistent in both)
val SageGreenPrimary = Color(0xFF5E9E7E)
val SageGreenDark = Color(0xFF4A8267)

// Status & Badge colors
val StatusGreen = Color(0xFF4E9E76)
val StatusOrange = Color(0xFFE67E22)
val StatusBlue = Color(0xFF3498DB)
val StatusPurple = Color(0xFF9B59B6)

// Dynamic Composable Color Getters
val CanvasAlabaster: Color @Composable get() = if (LocalIsDarkTheme.current) DarkCanvasAlabaster else LightCanvasAlabaster
val CanvasBeige: Color @Composable get() = if (LocalIsDarkTheme.current) DarkCanvasBeige else LightCanvasBeige
val CanvasMeshGreen: Color @Composable get() = if (LocalIsDarkTheme.current) DarkCanvasMeshGreen else LightCanvasMeshGreen

val GlassSurface: Color @Composable get() = if (LocalIsDarkTheme.current) DarkGlassSurface else LightGlassSurface
val GlassSurfaceSubtle: Color @Composable get() = if (LocalIsDarkTheme.current) DarkGlassSurfaceSubtle else LightGlassSurfaceSubtle
val GlassBorderTopLeft: Color @Composable get() = if (LocalIsDarkTheme.current) DarkGlassBorderTopLeft else LightGlassBorderTopLeft
val GlassBorderBottomRight: Color @Composable get() = if (LocalIsDarkTheme.current) DarkGlassBorderBottomRight else LightGlassBorderBottomRight

val SoftMint: Color @Composable get() = if (LocalIsDarkTheme.current) DarkSoftMint else LightSoftMint
val MintLight: Color @Composable get() = if (LocalIsDarkTheme.current) DarkMintLight else LightMintLight
val DeepForest: Color @Composable get() = if (LocalIsDarkTheme.current) DarkDeepForest else LightDeepForest

val SandBeige: Color @Composable get() = if (LocalIsDarkTheme.current) DarkSandBeige else LightSandBeige
val SandBeigeLight: Color @Composable get() = if (LocalIsDarkTheme.current) DarkSandBeigeLight else LightSandBeigeLight
val CharcoalPrimary: Color @Composable get() = if (LocalIsDarkTheme.current) DarkCharcoalPrimary else LightCharcoalPrimary
val OliveGreySecondary: Color @Composable get() = if (LocalIsDarkTheme.current) DarkOliveGreySecondary else LightOliveGreySecondary
val DividerMuted: Color @Composable get() = if (LocalIsDarkTheme.current) DarkDividerMuted else LightDividerMuted
