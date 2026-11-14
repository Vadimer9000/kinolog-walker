package com.kinolog.walker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AllInclusive
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.kinolog.walker.ui.theme.CanvasAlabaster
import com.kinolog.walker.ui.theme.CharcoalPrimary
import com.kinolog.walker.ui.theme.MintLight
import com.kinolog.walker.ui.theme.OliveGreySecondary
import com.kinolog.walker.ui.theme.SageGreenDark
import com.kinolog.walker.ui.theme.SageGreenPrimary
import com.kinolog.walker.ui.theme.SoftMint
import com.kinolog.walker.util.HapticHelper
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun AppCalendarDialog(
    initialDate: LocalDate? = null,
    allowClear: Boolean = false,
    clearButtonText: String = "Сделать бессрочным",
    onDateSelected: (LocalDate) -> Unit,
    onClearDate: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val view = LocalView.current
    val context = LocalContext.current
    val today = remember { LocalDate.now() }

    var displayedMonth by remember(initialDate) {
        mutableStateOf(if (initialDate != null) YearMonth.from(initialDate) else YearMonth.from(today))
    }

    val monthFormatter = remember {
        DateTimeFormatter.ofPattern("LLLL yyyy", Locale("ru"))
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CanvasAlabaster)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Month Header with navigation & Today button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            HapticHelper.lightClick(view, context)
                            displayedMonth = displayedMonth.minusMonths(1)
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Rounded.ChevronLeft,
                            contentDescription = "Предыдущий месяц",
                            tint = SageGreenPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Text(
                        text = displayedMonth.format(monthFormatter).replaceFirstChar { it.uppercase() },
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = CharcoalPrimary
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // "Сегодня" quick jump pill
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MintLight)
                                .clickable {
                                    HapticHelper.lightClick(view, context)
                                    val now = LocalDate.now()
                                    displayedMonth = YearMonth.from(now)
                                }
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Сегодня",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SageGreenDark
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        IconButton(
                            onClick = {
                                HapticHelper.lightClick(view, context)
                                displayedMonth = displayedMonth.plusMonths(1)
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Rounded.ChevronRight,
                                contentDescription = "Следующий месяц",
                                tint = SageGreenPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Weekday headers
                val weekDays = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    weekDays.forEach { dayName ->
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dayName,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = OliveGreySecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Month Grid
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val daysInMonth = displayedMonth.lengthOfMonth()
                    val firstDayOfWeek = displayedMonth.atDay(1).dayOfWeek.value // 1 = Monday .. 7 = Sunday
                    val totalSlots = (firstDayOfWeek - 1) + daysInMonth
                    val totalWeeks = (totalSlots + 6) / 7

                    Column(modifier = Modifier.fillMaxWidth()) {
                        for (week in 0 until totalWeeks) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                for (col in 0..6) {
                                    val dayIndex = week * 7 + col
                                    val dayNum = dayIndex - (firstDayOfWeek - 1) + 1

                                    if (dayNum in 1..daysInMonth) {
                                        val date = displayedMonth.atDay(dayNum)
                                        val isSelected = (initialDate != null && date == initialDate)
                                        val isToday = (date == today)

                                        val cellBg = when {
                                            isSelected -> SageGreenPrimary
                                            isToday -> SoftMint.copy(alpha = 0.5f)
                                            else -> Color.Transparent
                                        }

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(48.dp)
                                                .padding(horizontal = 1.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .then(
                                                    if (isToday && !isSelected) {
                                                        Modifier.border(1.dp, SageGreenPrimary.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                                                    } else Modifier
                                                )
                                                .background(cellBg)
                                                .clickable {
                                                    HapticHelper.tick(view, context)
                                                    onDateSelected(date)
                                                    onDismiss()
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "$dayNum",
                                                fontSize = 13.sp,
                                                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) Color.White else CharcoalPrimary
                                            )
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(42.dp)
                                                .padding(horizontal = 1.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Optional "Сделать бессрочным" or cancel actions
                if (allowClear && onClearDate != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = {
                            HapticHelper.lightClick(view, context)
                            onClearDate()
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            Icons.Rounded.AllInclusive,
                            contentDescription = null,
                            tint = SageGreenPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = clearButtonText,
                            color = SageGreenPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Отмена", color = OliveGreySecondary)
                    }
                }
            }
        }
    }
}
