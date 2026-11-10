package com.kinolog.walker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kinolog.walker.ui.theme.GlassBorderBottomRight
import com.kinolog.walker.ui.theme.GlassBorderTopLeft
import com.kinolog.walker.ui.theme.GlassSurface

@Composable
fun GlassBox(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(28.dp),
    backgroundColor: Color = GlassSurface,
    elevation: Dp = 6.dp,
    borderWidth: Dp = 1.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val specularBorderBrush = Brush.linearGradient(
        colors = listOf(
            GlassBorderTopLeft,
            GlassBorderBottomRight
        )
    )

    Box(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = shape,
                ambientColor = Color(0x1410281E),
                spotColor = Color(0x1410281E)
            )
            .clip(shape)
            .background(backgroundColor)
            .border(
                width = borderWidth,
                brush = specularBorderBrush,
                shape = shape
            ),
        content = content
    )
}
