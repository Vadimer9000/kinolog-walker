package com.kinolog.walker.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kinolog.walker.ui.theme.CharcoalPrimary
import com.kinolog.walker.ui.theme.OliveGreySecondary
import com.kinolog.walker.ui.theme.SageGreenPrimary
import com.kinolog.walker.ui.theme.SoftMint
import com.kinolog.walker.util.HapticHelper

@Composable
fun TransportSlider(
    currentMode: String, // "pd" or "mt"
    onModeChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val context = LocalContext.current
    val isPedestrian = currentMode == "pd"

    GlassBox(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = CircleShape,
        elevation = 6.dp
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            val totalWidth = maxWidth
            val halfWidth = totalWidth / 2

            val indicatorOffsetFraction by animateFloatAsState(
                targetValue = if (isPedestrian) 0f else 1f,
                animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f),
                label = "indicator_offset"
            )

            // Animated sliding indicator
            Box(
                modifier = Modifier
                    .width(halfWidth)
                    .fillMaxHeight()
                    .offset {
                        IntOffset(
                            x = (indicatorOffsetFraction * halfWidth.toPx()).toInt(),
                            y = 0
                        )
                    }
                    .clip(CircleShape)
                    .background(SoftMint)
            )

            // Tab labels
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Option 1: Pedestrian
                val text1Color by animateColorAsState(
                    targetValue = if (isPedestrian) CharcoalPrimary else OliveGreySecondary,
                    label = "text1_color"
                )
                val scale1 by animateFloatAsState(
                    targetValue = if (isPedestrian) 1.05f else 0.95f,
                    animationSpec = spring(dampingRatio = 0.65f, stiffness = 500f),
                    label = "scale_pd"
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            if (!isPedestrian) {
                                HapticHelper.lightClick(view, context)
                                onModeChanged("pd")
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🚶 Пешком",
                        color = text1Color,
                        fontWeight = if (isPedestrian) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 13.sp,
                        modifier = Modifier.scale(scale1)
                    )
                }

                // Option 2: Mass transit
                val text2Color by animateColorAsState(
                    targetValue = if (!isPedestrian) CharcoalPrimary else OliveGreySecondary,
                    label = "text2_color"
                )
                val scale2 by animateFloatAsState(
                    targetValue = if (!isPedestrian) 1.05f else 0.95f,
                    animationSpec = spring(dampingRatio = 0.65f, stiffness = 500f),
                    label = "scale_mt"
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            if (isPedestrian) {
                                HapticHelper.lightClick(view, context)
                                onModeChanged("mt")
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🚌 Транспорт",
                        color = text2Color,
                        fontWeight = if (!isPedestrian) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 13.sp,
                        modifier = Modifier.scale(scale2)
                    )
                }
            }
        }
    }
}
