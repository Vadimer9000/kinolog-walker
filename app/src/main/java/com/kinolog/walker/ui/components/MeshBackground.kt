package com.kinolog.walker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.kinolog.walker.ui.theme.CanvasAlabaster
import com.kinolog.walker.ui.theme.CanvasBeige

@Composable
fun MeshBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(CanvasAlabaster, CanvasBeige)
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Top right subtle sage blob
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x35BCE3CE), Color(0x00BCE3CE)),
                    center = Offset(width * 0.85f, height * 0.15f),
                    radius = width * 0.55f
                ),
                center = Offset(width * 0.85f, height * 0.15f),
                radius = width * 0.55f
            )

            // Middle left subtle mint blob
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x2AD4E6DC), Color(0x00D4E6DC)),
                    center = Offset(width * 0.1f, height * 0.5f),
                    radius = width * 0.65f
                ),
                center = Offset(width * 0.1f, height * 0.5f),
                radius = width * 0.65f
            )

            // Bottom right subtle warm blob
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x28E4DDD3), Color(0x00E4DDD3)),
                    center = Offset(width * 0.8f, height * 0.85f),
                    radius = width * 0.5f
                ),
                center = Offset(width * 0.8f, height * 0.85f),
                radius = width * 0.5f
            )
        }

        content()
    }
}
