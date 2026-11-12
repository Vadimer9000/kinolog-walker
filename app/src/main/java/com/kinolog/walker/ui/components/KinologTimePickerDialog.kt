package com.kinolog.walker.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.kinolog.walker.ui.theme.CanvasAlabaster
import com.kinolog.walker.ui.theme.CharcoalPrimary
import com.kinolog.walker.ui.theme.LocalIsDarkTheme
import com.kinolog.walker.ui.theme.OliveGreySecondary
import com.kinolog.walker.ui.theme.SageGreenPrimary
import com.kinolog.walker.ui.theme.SandBeige

@Composable
fun KinologTimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onDismiss: () -> Unit,
    onTimeSelected: (hour: Int, minute: Int) -> Unit
) {
    var selectedHour by remember { mutableIntStateOf(initialHour.coerceIn(0, 23)) }
    var selectedMinute by remember { mutableIntStateOf(initialMinute.coerceIn(0, 59)) }

    val isDark = LocalIsDarkTheme.current
    val cardBorder = if (isDark) BorderStroke(1.dp, Color(0xFF4C7A5B)) else BorderStroke(1.dp, SageGreenPrimary.copy(alpha = 0.35f))

    val popularSlots = listOf(
        8 to 0,
        9 to 0,
        10 to 0,
        11 to 0,
        12 to 0,
        14 to 0,
        16 to 0,
        18 to 0,
        19 to 0,
        20 to 0
    )

    val minuteOptions = listOf(0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CanvasAlabaster),
            border = cardBorder
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Schedule,
                        contentDescription = null,
                        tint = SageGreenPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Время начала",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = CharcoalPrimary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Big Time Display with +/- steppers
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Hours Stepper
                    IconButton(
                        onClick = { selectedHour = if (selectedHour > 0) selectedHour - 1 else 23 },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Rounded.Remove, contentDescription = "Час назад", tint = OliveGreySecondary)
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(SageGreenPrimary.copy(alpha = 0.14f))
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = String.format("%02d", selectedHour),
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Bold,
                            color = SageGreenPrimary
                        )
                    }

                    IconButton(
                        onClick = { selectedHour = (selectedHour + 1) % 24 },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = "Час вперед", tint = OliveGreySecondary)
                    }

                    Text(
                        text = ":",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = CharcoalPrimary,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )

                    // Minutes Stepper
                    IconButton(
                        onClick = { selectedMinute = if (selectedMinute > 0) selectedMinute - 1 else 59 },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Rounded.Remove, contentDescription = "Минута назад", tint = OliveGreySecondary)
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(SageGreenPrimary.copy(alpha = 0.14f))
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = String.format("%02d", selectedMinute),
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Bold,
                            color = SageGreenPrimary
                        )
                    }

                    IconButton(
                        onClick = { selectedMinute = (selectedMinute + 1) % 60 },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = "Минута вперед", tint = OliveGreySecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Popular Quick Slots
                Text(
                    text = "Популярное время:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = OliveGreySecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    popularSlots.forEach { (h, m) ->
                        val isSel = selectedHour == h && selectedMinute == m
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSel) SageGreenPrimary else SandBeige)
                                .clickable {
                                    selectedHour = h
                                    selectedMinute = m
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = String.format("%02d:%02d", h, m),
                                fontSize = 12.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSel) Color.White else CharcoalPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Hours Picker (00..23)
                Text(
                    text = "Часы (00 - 23):",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = OliveGreySecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    (0..23).forEach { h ->
                        val isSel = selectedHour == h
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (isSel) SageGreenPrimary else SandBeige)
                                .clickable { selectedHour = h }
                                .size(34.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = String.format("%02d", h),
                                fontSize = 12.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSel) Color.White else CharcoalPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Minutes Picker (00, 05, 10..55)
                Text(
                    text = "Минуты (шаг 5 мин):",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = OliveGreySecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    minuteOptions.forEach { m ->
                        val isSel = selectedMinute == m
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (isSel) SageGreenPrimary else SandBeige)
                                .clickable { selectedMinute = m }
                                .size(34.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = String.format("%02d", m),
                                fontSize = 12.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSel) Color.White else CharcoalPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
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
                        onClick = { onTimeSelected(selectedHour, selectedMinute) },
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
