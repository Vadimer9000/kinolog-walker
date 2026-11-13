package com.kinolog.walker.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kinolog.walker.ui.theme.CharcoalPrimary
import com.kinolog.walker.ui.theme.OliveGreySecondary
import com.kinolog.walker.ui.theme.SageGreenPrimary
import com.kinolog.walker.ui.theme.SoftMint
import com.kinolog.walker.util.HapticHelper

enum class NavigationTab(val title: String, val icon: ImageVector) {
    SCHEDULE("Карта и План", Icons.Rounded.Map),
    CLIENTS("Клиенты", Icons.Rounded.Pets),
    SETTINGS("Бэкап", Icons.Rounded.Settings)
}

@Composable
fun AppBottomBar(
    currentTab: NavigationTab,
    onTabSelected: (NavigationTab) -> Unit,
    isMapEnabled: Boolean = false,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val context = LocalContext.current
    val bottomTabs = remember { listOf(NavigationTab.SCHEDULE, NavigationTab.CLIENTS) }
    val selectedIndex = bottomTabs.indexOf(currentTab).coerceAtLeast(0)
    val currentTabState by rememberUpdatedState(currentTab)
    val currentOnTabSelected by rememberUpdatedState(onTabSelected)
    var containerWidth by remember { mutableFloatStateOf(1f) }
    var currentX by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        GlassBox(
            modifier = Modifier.fillMaxWidth(),
            shape = CircleShape,
            elevation = 10.dp
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .padding(4.dp)
                    .onSizeChanged { size ->
                        if (size.width > 0) {
                            containerWidth = size.width.toFloat()
                        }
                    }
                    .pointerInput(bottomTabs.size) {
                        detectHorizontalDragGestures(
                            onDragStart = { offset ->
                                currentX = offset.x
                                if (containerWidth > 0f && bottomTabs.isNotEmpty()) {
                                    val targetIndex = ((currentX / containerWidth) * bottomTabs.size).toInt().coerceIn(0, bottomTabs.size - 1)
                                    val targetTab = bottomTabs[targetIndex]
                                    if (targetTab != currentTabState) {
                                        HapticHelper.tick(view, context)
                                        currentOnTabSelected(targetTab)
                                    }
                                }
                            },
                            onHorizontalDrag = { change, dragAmount ->
                                change.consume()
                                currentX = (currentX + dragAmount).coerceIn(0f, containerWidth)
                                if (containerWidth > 0f && bottomTabs.isNotEmpty()) {
                                    val targetIndex = ((currentX / containerWidth) * bottomTabs.size).toInt().coerceIn(0, bottomTabs.size - 1)
                                    val targetTab = bottomTabs[targetIndex]
                                    if (targetTab != currentTabState) {
                                        HapticHelper.tick(view, context)
                                        currentOnTabSelected(targetTab)
                                    }
                                }
                            }
                        )
                    }
            ) {
                val totalWidth = maxWidth
                val segmentWidth = totalWidth / bottomTabs.size

                val animatedOffsetFraction by animateFloatAsState(
                    targetValue = selectedIndex.toFloat(),
                    animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f),
                    label = "bottom_bar_offset"
                )

                // Animated sliding indicator pill
                if (currentTab in bottomTabs) {
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
                }

                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    bottomTabs.forEachIndexed { index, tab ->
                        val isSelected = tab == currentTab
                        val contentColor by animateColorAsState(
                            targetValue = if (isSelected) CharcoalPrimary else OliveGreySecondary,
                            animationSpec = spring(),
                            label = "pill_tab_content"
                        )
                        val iconScale by animateFloatAsState(
                            targetValue = if (isSelected) 1.15f else 1.0f,
                            animationSpec = spring(dampingRatio = 0.55f, stiffness = 400f),
                            label = "pill_icon_scale"
                        )

                        val interactionSource = remember { MutableInteractionSource() }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(CircleShape)
                                .clickable(
                                    interactionSource = interactionSource,
                                    indication = null
                                ) {
                                    if (tab != currentTab) {
                                        HapticHelper.tick(view, context)
                                    }
                                    onTabSelected(tab)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                val tabTitle = if (tab == NavigationTab.SCHEDULE) {
                                    if (isMapEnabled) "Карта и План" else "План"
                                } else tab.title

                                val tabIcon = if (tab == NavigationTab.SCHEDULE) {
                                    if (isMapEnabled) Icons.Rounded.Map else Icons.Rounded.CalendarToday
                                } else tab.icon

                                Icon(
                                    imageVector = tabIcon,
                                    contentDescription = tabTitle,
                                    tint = if (isSelected) SageGreenPrimary else contentColor,
                                    modifier = Modifier
                                        .size(20.dp)
                                        .scale(iconScale)
                                )
                                Text(
                                    text = tabTitle,
                                    color = contentColor,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
