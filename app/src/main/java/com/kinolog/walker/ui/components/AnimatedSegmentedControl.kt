package com.kinolog.walker.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kinolog.walker.ui.theme.CharcoalPrimary
import com.kinolog.walker.ui.theme.OliveGreySecondary
import com.kinolog.walker.ui.theme.SandBeige
import com.kinolog.walker.ui.theme.SoftMint
import com.kinolog.walker.util.HapticHelper

@Composable
fun <T> AnimatedSegmentedControl(
    items: List<T>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 40.dp,
    itemLabel: (T) -> String = { it.toString() }
) {
    val view = LocalView.current
    val context = LocalContext.current
    var dragAccumulator by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(CircleShape)
            .background(SandBeige)
            .padding(3.dp)
            .pointerInput(selectedIndex, items.size) {
                detectHorizontalDragGestures(
                    onDragStart = { dragAccumulator = 0f },
                    onDragEnd = {
                        if (dragAccumulator > 30f && selectedIndex < items.lastIndex) {
                            HapticHelper.lightClick(view, context)
                            onItemSelected(selectedIndex + 1)
                        } else if (dragAccumulator < -30f && selectedIndex > 0) {
                            HapticHelper.lightClick(view, context)
                            onItemSelected(selectedIndex - 1)
                        }
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        dragAccumulator += dragAmount
                    }
                )
            }
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val totalWidth = maxWidth
            val segmentWidth = if (items.isNotEmpty()) totalWidth / items.size else totalWidth

            val animatedOffsetFraction by animateFloatAsState(
                targetValue = selectedIndex.toFloat(),
                animationSpec = spring(dampingRatio = 0.82f, stiffness = 420f),
                label = "seg_offset"
            )

            // Animated sliding indicator pill
            Box(
                modifier = Modifier
                    .width(segmentWidth)
                    .fillMaxHeight()
                    .offset {
                        IntOffset(
                            x = (animatedOffsetFraction * segmentWidth.toPx()).toInt(),
                            y = 0
                        )
                    }
                    .clip(CircleShape)
                    .background(SoftMint)
            )

            // Labels Row
            Row(modifier = Modifier.fillMaxWidth()) {
                items.forEachIndexed { index, item ->
                    val isSelected = index == selectedIndex
                    val textColor by animateColorAsState(
                        targetValue = if (isSelected) CharcoalPrimary else OliveGreySecondary,
                        animationSpec = spring(),
                        label = "seg_text_color"
                    )
                    val scale by animateFloatAsState(
                        targetValue = if (isSelected) 1.04f else 0.96f,
                        animationSpec = spring(dampingRatio = 0.7f, stiffness = 450f),
                        label = "seg_scale"
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
                                if (index != selectedIndex) {
                                    HapticHelper.lightClick(view, context)
                                    onItemSelected(index)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = itemLabel(item),
                            color = textColor,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            maxLines = 1,
                            modifier = Modifier.scale(scale)
                        )
                    }
                }
            }
        }
    }
}
