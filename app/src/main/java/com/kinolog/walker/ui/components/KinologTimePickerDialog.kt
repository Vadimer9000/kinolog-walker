package com.kinolog.walker.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.kinolog.walker.ui.theme.CanvasAlabaster
import com.kinolog.walker.ui.theme.CharcoalPrimary
import com.kinolog.walker.ui.theme.LocalIsDarkTheme
import com.kinolog.walker.ui.theme.OliveGreySecondary
import com.kinolog.walker.ui.theme.SageGreenPrimary
import com.kinolog.walker.util.HapticHelper
import kotlinx.coroutines.launch

@Composable
fun KinologTimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onDismiss: () -> Unit,
    onTimeSelected: (hour: Int, minute: Int) -> Unit
) {
    var selectedHour by remember { mutableIntStateOf(initialHour.coerceIn(0, 23)) }
    var selectedMinute by remember { mutableIntStateOf(initialMinute.coerceIn(0, 59)) }

    val view = LocalView.current
    val context = LocalContext.current
    val isDark = LocalIsDarkTheme.current
    val cardContainerColor = if (isDark) Color(0xFF141D17) else CanvasAlabaster
    val cardBorder = if (isDark) {
        BorderStroke(1.dp, Color(0xFF3B694C))
    } else {
        BorderStroke(1.dp, SageGreenPrimary.copy(alpha = 0.35f))
    }

    val focusBgColor = if (isDark) {
        Color(0xFF284232).copy(alpha = 0.75f)
    } else {
        SageGreenPrimary.copy(alpha = 0.12f)
    }
    val focusBorderColor = if (isDark) {
        Color(0xFF3B694C).copy(alpha = 0.6f)
    } else {
        SageGreenPrimary.copy(alpha = 0.3f)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = cardContainerColor),
            border = cardBorder
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 18.dp)
            ) {
                // Header: Title with clock icon
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Schedule,
                        contentDescription = null,
                        tint = SageGreenPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Время начала",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = CharcoalPrimary
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Center: Apple-style Wheel Drums (Hours : Minutes)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Central Highlight Focus Plate across both wheels
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(focusBgColor)
                            .border(1.dp, focusBorderColor, RoundedCornerShape(10.dp))
                    )

                    // The Two Drums + Colon Separator
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Hours Drum (00..23)
                        WheelPickerColumn(
                            rangeSize = 24,
                            initialValue = initialHour,
                            modifier = Modifier.weight(1f),
                            isDark = isDark,
                            onValueChange = { selectedHour = it }
                        )

                        // Center Colon Separator
                        Text(
                            text = ":",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color(0xFFE2E8E4) else CharcoalPrimary,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )

                        // Minutes Drum (00..59)
                        WheelPickerColumn(
                            rangeSize = 60,
                            initialValue = initialMinute,
                            modifier = Modifier.weight(1f),
                            isDark = isDark,
                            onValueChange = { selectedMinute = it }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons: Cancel and Select
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Отмена", color = OliveGreySecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            HapticHelper.success(view, context)
                            onTimeSelected(selectedHour, selectedMinute)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SageGreenPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Выбрать", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WheelPickerColumn(
    rangeSize: Int,
    initialValue: Int,
    modifier: Modifier = Modifier,
    isDark: Boolean,
    onValueChange: (Int) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val view = LocalView.current
    val context = LocalContext.current
    val itemHeight = 40.dp
    val density = LocalDensity.current
    val itemHeightPx = with(density) { itemHeight.toPx() }

    // Position around middle of Int.MAX_VALUE for infinite two-way scrolling
    val baseMultiplier = 100_000
    val startIndex = (baseMultiplier * rangeSize) + (initialValue % rangeSize)
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = startIndex)
    val snapFlingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    // Center item index in the viewport (with vertical padding of 80dp on a 200dp column,
    // firstVisibleItemIndex is the item snapped to the top of padded area = exactly center)
    val centerIndex by remember {
        derivedStateOf {
            val firstVisible = listState.firstVisibleItemIndex
            val scrollOffset = listState.firstVisibleItemScrollOffset
            if (scrollOffset > itemHeightPx / 2f) firstVisible + 1 else firstVisible
        }
    }

    var isFirstComposition by remember { mutableStateOf(true) }

    LaunchedEffect(centerIndex) {
        val currentValue = ((centerIndex % rangeSize) + rangeSize) % rangeSize
        onValueChange(currentValue)
        if (isFirstComposition) {
            isFirstComposition = false
        } else {
            HapticHelper.tick(view, context)
        }
    }

    LazyColumn(
        state = listState,
        flingBehavior = snapFlingBehavior,
        contentPadding = PaddingValues(vertical = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.height(200.dp)
    ) {
        items(
            count = Int.MAX_VALUE,
            key = { it }
        ) { index ->
            val value = ((index % rangeSize) + rangeSize) % rangeSize
            val distance = kotlin.math.abs(index - centerIndex)

            val (alpha, fontSize) = when (distance) {
                0 -> 1.0f to 24.sp
                1 -> 0.50f to 18.sp
                else -> 0.20f to 15.sp
            }
            val fontWeight = if (distance == 0) FontWeight.SemiBold else FontWeight.Normal
            val textColor = if (distance == 0) {
                if (isDark) Color(0xFFE2E8E4) else CharcoalPrimary
            } else {
                if (isDark) Color(0xFFA0ABA4) else OliveGreySecondary
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(itemHeight)
                    .clickable {
                        coroutineScope.launch {
                            listState.animateScrollToItem(index)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                val formattedValue = if (value < 10) "0$value" else "$value"
                Text(
                    text = formattedValue,
                    fontSize = fontSize,
                    fontWeight = fontWeight,
                    color = textColor.copy(alpha = alpha)
                )
            }
        }
    }
}
