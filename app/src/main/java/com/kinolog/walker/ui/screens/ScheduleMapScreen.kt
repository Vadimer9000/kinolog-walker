package com.kinolog.walker.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Chat
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Directions
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Route
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.ViewAgenda
import androidx.compose.material.icons.rounded.ViewHeadline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.kinolog.walker.model.ClientEntity
import com.kinolog.walker.model.PetItem
import com.kinolog.walker.model.WalkCategory
import com.kinolog.walker.model.WalkEventEntity
import com.kinolog.walker.ui.MainViewModel
import com.kinolog.walker.ui.components.AnimatedSegmentedControl
import com.kinolog.walker.ui.components.AppCalendarDialog
import com.kinolog.walker.ui.components.AppTextField
import com.kinolog.walker.ui.components.GlassBox
import com.kinolog.walker.ui.components.KinologTimePickerDialog
import com.kinolog.walker.ui.components.NavigationTab
import com.kinolog.walker.ui.components.PetTag
import com.kinolog.walker.ui.components.TransportSlider
import com.kinolog.walker.ui.components.YandexMapView
import com.kinolog.walker.ui.theme.CanvasAlabaster
import com.kinolog.walker.ui.theme.CharcoalPrimary
import com.kinolog.walker.ui.theme.DangerRedBg
import com.kinolog.walker.ui.theme.DangerRedText
import com.kinolog.walker.ui.theme.DeepForest
import com.kinolog.walker.ui.theme.GlassSurface
import com.kinolog.walker.ui.theme.LocalIsDarkTheme
import com.kinolog.walker.ui.theme.MintLight
import com.kinolog.walker.ui.theme.OliveGreySecondary
import com.kinolog.walker.ui.theme.PriceBadgeBg
import com.kinolog.walker.ui.theme.PriceBadgeText
import com.kinolog.walker.ui.theme.SageGreenDark
import com.kinolog.walker.ui.theme.SageGreenPrimary
import com.kinolog.walker.ui.theme.SandBeige
import com.kinolog.walker.ui.theme.SoftMint
import com.kinolog.walker.util.HapticHelper
import com.kinolog.walker.util.IntentHelper
import com.kinolog.walker.util.PreferencesManager
import kotlinx.serialization.json.Json
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

private fun cleanAddress(address: String): String {
    return address
        .trim()
        .removePrefix("г. Санкт-Петербург, ")
        .removePrefix("г. Санкт-Петербург,")
        .removePrefix("Санкт-Петербург, ")
        .removePrefix("Санкт-Петербург,")
        .removePrefix("город Санкт-Петербург, ")
        .removePrefix("город Санкт-Петербург,")
        .removePrefix("г. СПб, ")
        .removePrefix("г. СПб,")
        .removePrefix("СПб, ")
        .removePrefix("СПб,")
        .trim()
}

enum class SheetPosition {
    COLLAPSED,
    MIDDLE,
    EXPANDED
}

@Composable
private fun ScheduleTopControls(
    selectedDate: LocalDate,
    isCalendarExpanded: Boolean,
    onToggleCalendar: () -> Unit,
    onPrevDay: () -> Unit,
    onNextDay: () -> Unit,
    modifier: Modifier = Modifier,
    onSettingsClick: () -> Unit = {}
) {
    val view = LocalView.current
    val context = LocalContext.current
    val dayMonthFormatter = remember { DateTimeFormatter.ofPattern("d MMMM", Locale("ru")) }
    val formattedDate = remember(selectedDate) {
        val weekday = when (selectedDate.dayOfWeek) {
            DayOfWeek.MONDAY -> "Пн"
            DayOfWeek.TUESDAY -> "Вт"
            DayOfWeek.WEDNESDAY -> "Ср"
            DayOfWeek.THURSDAY -> "Чт"
            DayOfWeek.FRIDAY -> "Пт"
            DayOfWeek.SATURDAY -> "Сб"
            DayOfWeek.SUNDAY -> "Вс"
        }
        "$weekday, ${selectedDate.format(dayMonthFormatter)}"
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Date Selector Pill takes full width for clean one-hand view
        GlassBox(
            modifier = Modifier.fillMaxWidth(),
            shape = CircleShape,
            elevation = 6.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        HapticHelper.lightClick(view, context)
                        onPrevDay()
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Rounded.ChevronLeft, contentDescription = "Предыдущий день", tint = SageGreenPrimary, modifier = Modifier.size(20.dp))
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .weight(1f)
                        .clip(CircleShape)
                        .clickable {
                            HapticHelper.lightClick(view, context)
                            onToggleCalendar()
                        }
                        .padding(horizontal = 6.dp, vertical = 6.dp)
                ) {
                    Icon(
                        Icons.Rounded.CalendarMonth,
                        contentDescription = null,
                        tint = SageGreenPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formattedDate,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = CharcoalPrimary,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = if (isCalendarExpanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                        contentDescription = if (isCalendarExpanded) "Свернуть календарь" else "Развернуть календарь",
                        tint = SageGreenPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(
                    onClick = {
                        HapticHelper.lightClick(view, context)
                        onNextDay()
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Rounded.ChevronRight, contentDescription = "Следующий день", tint = SageGreenPrimary, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
private fun MonthCalendarDropdown(
    selectedDate: LocalDate,
    walksCountByEpochDay: Map<Long, Int>,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    var displayedMonth by remember(selectedDate) {
        mutableStateOf(YearMonth.from(selectedDate))
    }

    val monthFormatter = remember {
        DateTimeFormatter.ofPattern("LLLL yyyy", Locale("ru"))
    }

    val today = remember { LocalDate.now() }

    GlassBox(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp)
        ) {
            // Month Header with navigation & Today button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { displayedMonth = displayedMonth.minusMonths(1) },
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
                                val now = LocalDate.now()
                                displayedMonth = YearMonth.from(now)
                                onDateSelected(now)
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
                        onClick = { displayedMonth = displayedMonth.plusMonths(1) },
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

            // Days Grid
            val daysInMonth = displayedMonth.lengthOfMonth()
            val firstDayOfWeek = displayedMonth.atDay(1).dayOfWeek.value // 1 = Monday .. 7 = Sunday
            val totalSlots = (firstDayOfWeek - 1) + daysInMonth
            val totalWeeks = (totalSlots + 6) / 7

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
                            val isSelected = (date == selectedDate)
                            val isToday = (date == today)
                            val walksCount = walksCountByEpochDay[date.toEpochDay()] ?: 0

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
                                    .clickable { onDateSelected(date) },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "$dayNum",
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else CharcoalPrimary
                                    )

                                    if (walksCount > 0) {
                                        Box(
                                            modifier = Modifier
                                                .padding(top = 1.dp)
                                                .background(
                                                    color = if (isSelected) Color.White.copy(alpha = 0.92f) else PriceBadgeBg,
                                                    shape = RoundedCornerShape(4.dp)
                                                )
                                                .padding(horizontal = 4.dp, vertical = 0.5.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "$walksCount",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) SageGreenDark else PriceBadgeText,
                                                lineHeight = 10.sp
                                            )
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.height(11.dp))
                                    }
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .padding(horizontal = 1.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ScheduleMapScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val view = LocalView.current
    val isDark = LocalIsDarkTheme.current
    val selectedDate by viewModel.selectedDate.collectAsState()
    val routingMode by viewModel.routingMode.collectAsState()
    val apiKey by viewModel.yandexApiKey.collectAsState()
    val walks by viewModel.walksForSelectedDate.collectAsState()
    val clientsMap by viewModel.clientsMap.collectAsState()
    val mapMarkers by viewModel.mapMarkerPoints.collectAsState()
    val allClients by viewModel.allClients.collectAsState()
    val isMapEnabled by viewModel.isMapEnabled.collectAsState()
    val allWalks by viewModel.allWalks.collectAsState()
    val walksCountByEpochDay = remember(allWalks) {
        allWalks.groupBy { it.dateEpochDay }.mapValues { it.value.size }
    }
    var isCalendarExpanded by remember { mutableStateOf(false) }

    var showWalkFormDialog by remember { mutableStateOf(false) }
    var walkToEdit by remember { mutableStateOf<WalkEventEntity?>(null) }
    var walkToDelete by remember { mutableStateOf<WalkEventEntity?>(null) }
    var clientForCallChooser by remember { mutableStateOf<ClientEntity?>(null) }
    var walkToReschedule by remember { mutableStateOf<WalkEventEntity?>(null) }
    var walkToOpenMap by remember { mutableStateOf<WalkEventEntity?>(null) }

    val prefs = remember { PreferencesManager(context) }
    var isCompactView by remember { mutableStateOf(prefs.isCompactView) }

    val json = remember { Json { ignoreUnknownKeys = true } }

    @Composable
    fun WalksListContent(contentModifier: Modifier = Modifier) {
        var daySwipeAccumulator by remember { mutableFloatStateOf(0f) }

        Column(
            modifier = contentModifier
                .fillMaxWidth()
                .pointerInput(selectedDate) {
                    detectHorizontalDragGestures(
                        onDragStart = { daySwipeAccumulator = 0f },
                        onDragEnd = {
                            if (daySwipeAccumulator < -50f) {
                                HapticHelper.lightClick(view, context)
                                viewModel.setSelectedDate(selectedDate.plusDays(1))
                            } else if (daySwipeAccumulator > 50f) {
                                HapticHelper.lightClick(view, context)
                                viewModel.setSelectedDate(selectedDate.minusDays(1))
                            }
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            daySwipeAccumulator += dragAmount
                        }
                    )
                }
                .background(Color.Transparent)
        ) {
                        // Header of BottomSheet
                    val totalPlannedEarnings = remember(walks) { walks.sumOf { it.price } }
                    val totalEarned = remember(walks) { walks.filter { it.isCompleted }.sumOf { it.price } }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f, fill = false)) {
                            Text(
                                text = "План на день (${walks.size})",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = CharcoalPrimary
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                if (walks.isNotEmpty()) {
                                    Text(
                                        text = "Выполнено: ${walks.count { it.isCompleted }}/${walks.size}",
                                        fontSize = 11.sp,
                                        color = OliveGreySecondary
                                    )
                                }
                                if (totalPlannedEarnings > 0) {
                                    Text(
                                        text = "• $totalEarned / $totalPlannedEarnings ₽",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isDark) Color(0xFF81C784) else Color(0xFF2E7D32)
                                    )
                                }
                            }
                        }
    
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val pendingCount = walks.count { !it.isCompleted }
                            if (pendingCount > 0) {
                                // Compact Route Button without text "Маршрут", fixed 36x36 dp
                                FilledTonalIconButton(
                                    onClick = {
                                        HapticHelper.lightClick(view, context)
                                        viewModel.routeDayWalks(context)
                                    },
                                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                                        containerColor = SageGreenPrimary,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Rounded.Route, contentDescription = "Маршрут ($pendingCount)", modifier = Modifier.size(18.dp))
                                }
                            }
    
                            // Toggle Compact View Button
                            FilledTonalIconButton(
                                onClick = {
                                    isCompactView = !isCompactView
                                    prefs.isCompactView = isCompactView
                                },
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = if (isCompactView) SageGreenPrimary else SandBeige,
                                    contentColor = if (isCompactView) Color.White else CharcoalPrimary
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = if (isCompactView) Icons.Rounded.ViewHeadline else Icons.Rounded.ViewAgenda,
                                    contentDescription = "Компактный вид",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
    
                            // Add Walk Button
                            FilledTonalIconButton(
                                onClick = {
                                    walkToEdit = null
                                    showWalkFormDialog = true
                                },
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = SoftMint,
                                    contentColor = DeepForest
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Rounded.Add, contentDescription = "Запланировать выгул", modifier = Modifier.size(20.dp))
                            }
                        }
                    }
    
                    if (walks.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(horizontal = 24.dp, vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "На этот день выгулов нет.\nНажмите '+', чтобы добавить в расписание.",
                                color = OliveGreySecondary,
                                fontSize = 14.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
                            verticalArrangement = Arrangement.spacedBy(if (isCompactView) 6.dp else 10.dp)
                        ) {
                            items(walks, key = { it.id }) { walk ->
                                val client = clientsMap[walk.clientId]
                                val pets = remember(client?.id, client?.petsJson) {
                                    if (client != null && client.petsJson.isNotBlank()) {
                                        try {
                                            json.decodeFromString<List<PetItem>>(client.petsJson)
                                        } catch (e: Exception) {
                                            emptyList()
                                        }
                                    } else emptyList()
                                }
    
                                val startHours = walk.startTimeMinutes / 60
                                val startMins = walk.startTimeMinutes % 60
                                val endTimeMinutes = walk.startTimeMinutes + walk.durationMinutes
                                val endHours = (endTimeMinutes / 60) % 24
                                val endMins = endTimeMinutes % 60
                                val timeFormatted = String.format(
                                    "%02d:%02d – %02d:%02d (%d мин)",
                                    startHours, startMins, endHours, endMins, walk.durationMinutes
                                )
    
                                val isDone = walk.isCompleted

                                val walkCardBg = when {
                                    isDone && isDark -> Color(0xFF1A241E)
                                    isDone && !isDark -> Color(0xFFEAEFEB).copy(alpha = 0.6f)
                                    isDark -> Color(0xFF1E2620)
                                    else -> GlassSurface
                                }
                                val walkCardAlpha = if (isDone && !isDark) 0.75f else 1.0f

                                val walkPrimaryText = when {
                                    isDone && isDark -> Color(0xFFE2E8E4)
                                    isDone && !isDark -> OliveGreySecondary
                                    else -> CharcoalPrimary
                                }
                                val walkSecondaryText = when {
                                    isDone && isDark -> Color(0xFFBAC4BE)
                                    else -> OliveGreySecondary
                                }
                                val walkDoneCheckTint = if (isDark) Color(0xFF81C784) else Color(0xFF2E7D32)
                                val walkChipBg = if (isDone) (if (isDark) Color(0xFF23362A) else Color(0xFFD6DFD9).copy(alpha = 0.5f)) else walk.category.composeColor.copy(alpha = 0.16f)
                                val walkChipText = if (isDone) (if (isDark) Color(0xFF81C784) else Color(0xFF2E7D32)) else walk.category.composeColor
                                val walkPriceBg = if (isDone) (if (isDark) Color(0xFF23362A) else Color(0xFFD6DFD9).copy(alpha = 0.5f)) else PriceBadgeBg
                                val walkPriceText = if (isDone) (if (isDark) Color(0xFF81C784) else OliveGreySecondary) else PriceBadgeText
                                val walkLocationTint = if (isDone) (if (isDark) Color(0xFF81C784) else OliveGreySecondary) else SageGreenPrimary
    
                                var isCardExpanded by remember(walk.id) { mutableStateOf(false) }
                                val showCompact = isCompactView && !isCardExpanded

                                if (showCompact) {
                                    // Compact Walk Card with clean, dense styling and tap-to-expand
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .alpha(walkCardAlpha)
                                    ) {
                                        GlassBox(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    HapticHelper.lightClick(view, context)
                                                    isCardExpanded = true
                                                },
                                            backgroundColor = walkCardBg,
                                            elevation = if (isDone) 1.dp else 2.dp
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                                            ) {
                                                // Строка 1: время и клиент/питомцы (Modifier.weight(1f), maxLines = 1, Ellipsis) + бейдж цены («2000 ₽») + компактный кружок статуса (визуально 26.dp, кликабельная зона 48.dp через minimumInteractiveComponentSize())
                                                val petSummary = pets.joinToString(", ") { "${it.type.icon} ${it.name}" }
                                                val line1ClientAndPets = buildString {
                                                    append(client?.fullName ?: "")
                                                    if (petSummary.isNotBlank()) {
                                                        if (isNotEmpty()) append(" · ")
                                                        append(petSummary)
                                                    }
                                                }

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(
                                                        modifier = Modifier.weight(1f),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            text = String.format("%02d:%02d–%02d:%02d", startHours, startMins, endHours, endMins),
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = walkPrimaryText,
                                                            textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None,
                                                            maxLines = 1
                                                        )

                                                        Spacer(modifier = Modifier.width(6.dp))

                                                        Text(
                                                            text = line1ClientAndPets,
                                                            fontWeight = FontWeight.SemiBold,
                                                            fontSize = 13.sp,
                                                            color = walkPrimaryText,
                                                            textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis,
                                                            modifier = Modifier.weight(1f, fill = false)
                                                        )
                                                    }

                                                    if (walk.price > 0) {
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Box(
                                                            modifier = Modifier
                                                                .clip(CircleShape)
                                                                .background(walkPriceBg)
                                                                .padding(horizontal = 7.dp, vertical = 2.dp),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text(
                                                                text = "${walk.price} ₽",
                                                                color = walkPriceText,
                                                                fontSize = 11.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                maxLines = 1,
                                                                softWrap = false
                                                            )
                                                        }
                                                    }

                                                    Spacer(modifier = Modifier.width(6.dp))

                                                    IconButton(
                                                        onClick = {
                                                            HapticHelper.success(view, context)
                                                            viewModel.toggleWalkCompletion(walk)
                                                        },
                                                        modifier = Modifier
                                                            .size(26.dp)
                                                            .minimumInteractiveComponentSize()
                                                    ) {
                                                        Icon(
                                                            imageVector = if (isDone) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                                                            contentDescription = "Статус",
                                                            tint = if (isDone) walkDoneCheckTint else OliveGreySecondary,
                                                            modifier = Modifier.size(26.dp)
                                                        )
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(4.dp))

                                                // Строка 2: иконка пина + адрес без «Санкт-Петербург, » на всю свободную ширину (Modifier.weight(1f), maxLines = 1, Ellipsis — чтобы улица и номер дома влезали целиком!) + справа 2 компактные кнопки «Маршрут» и «Звонок» (визуальный размер 36.dp со скруглением 10.dp, тач-зона 48.dp)
                                                val cleanedStreet = cleanAddress(client?.addressStreet ?: "")
                                                val fullAddrShort = buildString {
                                                    append(cleanedStreet)
                                                    if (!client?.entrance.isNullOrBlank()) append(", под. ${client.entrance}")
                                                    if (!client?.floor.isNullOrBlank()) append(", эт. ${client.floor}")
                                                    if (!client?.apartment.isNullOrBlank()) append(", кв. ${client.apartment}")
                                                }

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(
                                                        modifier = Modifier.weight(1f),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Rounded.LocationOn,
                                                            contentDescription = null,
                                                            tint = walkLocationTint,
                                                            modifier = Modifier.size(13.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(3.dp))
                                                        Text(
                                                            text = fullAddrShort.ifBlank { "Адрес не указан" },
                                                            fontSize = 11.5.sp,
                                                            color = walkSecondaryText,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    }

                                                    Spacer(modifier = Modifier.width(6.dp))

                                                    Row(
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        // Маршрут
                                                        FilledTonalIconButton(
                                                            onClick = {
                                                                HapticHelper.lightClick(view, context)
                                                                viewModel.routeSingleWalk(context, walk)
                                                            },
                                                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                                                containerColor = SoftMint,
                                                                contentColor = DeepForest
                                                            ),
                                                            shape = RoundedCornerShape(10.dp),
                                                            modifier = Modifier
                                                                .size(36.dp)
                                                                .minimumInteractiveComponentSize()
                                                        ) {
                                                            Icon(Icons.Rounded.Directions, contentDescription = "Маршрут", modifier = Modifier.size(17.dp))
                                                        }

                                                        // Звонок
                                                        FilledTonalIconButton(
                                                            onClick = {
                                                                if (client != null) {
                                                                    if (client.secondaryContactPhone.isNotBlank()) {
                                                                        clientForCallChooser = client
                                                                    } else {
                                                                        IntentHelper.dialPhone(context, client.phone)
                                                                    }
                                                                }
                                                            },
                                                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                                                containerColor = SandBeige,
                                                                contentColor = CharcoalPrimary
                                                            ),
                                                            shape = RoundedCornerShape(10.dp),
                                                            modifier = Modifier
                                                                .size(36.dp)
                                                                .minimumInteractiveComponentSize()
                                                        ) {
                                                            Icon(Icons.Rounded.Call, contentDescription = "Звонок", modifier = Modifier.size(16.dp))
                                                        }
                                                    }
                                                }

                                                // Строка 3: заметка/домофон
                                                val clientNote = client?.clientComment?.trim() ?: ""
                                                val walkNote = walk.walkComment.trim()
                                                val hasClientNote = clientNote.isNotBlank()
                                                val hasWalkNote = walkNote.isNotBlank()

                                                if (hasClientNote || hasWalkNote) {
                                                    val notesSummary = buildString {
                                                        if (hasClientNote) {
                                                            append("🔑 $clientNote")
                                                        }
                                                        if (hasClientNote && hasWalkNote) {
                                                            append("  •  ")
                                                        }
                                                        if (hasWalkNote) {
                                                            append("📝 $walkNote")
                                                        }
                                                    }
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = notesSummary,
                                                        fontSize = 12.sp,
                                                        color = if (isDark) Color(0xFFFFD59E) else Color(0xFFB45309),
                                                        maxLines = 2,
                                                        overflow = TextOverflow.Ellipsis,
                                                        lineHeight = 15.sp,
                                                        modifier = Modifier.fillMaxWidth()
                                                    )
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    // Standard Walk Card with clean, intuitive, contrasting design
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .alpha(walkCardAlpha)
                                    ) {
                                        GlassBox(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .then(
                                                    if (isCompactView) {
                                                        Modifier.clickable {
                                                            HapticHelper.lightClick(view, context)
                                                            isCardExpanded = false
                                                        }
                                                    } else Modifier
                                                ),
                                            backgroundColor = walkCardBg,
                                            elevation = if (isDone) 1.dp else 3.dp
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 14.dp, vertical = 12.dp)
                                            ) {
                                                // 1. Верхняя строка (шапка): слева время в компактном формате без обрезки («20:00–20:15 · 15 мин», maxLines = 1), справа аккуратный бейдж стоимости «500 ₽» и кнопка-чекбокс выполнения
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .then(
                                                            if (isCompactView) {
                                                                Modifier.clip(RoundedCornerShape(6.dp)).clickable {
                                                                    HapticHelper.lightClick(view, context)
                                                                    isCardExpanded = false
                                                                }
                                                            } else Modifier
                                                        ),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = String.format("%02d:%02d–%02d:%02d · %d мин", startHours, startMins, endHours, endMins, walk.durationMinutes),
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = walkPrimaryText,
                                                        textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None,
                                                        maxLines = 1,
                                                        softWrap = false,
                                                        modifier = Modifier.weight(1f, fill = false)
                                                    )

                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                    ) {
                                                        if (walk.price > 0) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .clip(CircleShape)
                                                                    .background(walkPriceBg)
                                                                    .padding(horizontal = 8.dp, vertical = 2.dp),
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                Text(
                                                                    text = "${walk.price} ₽",
                                                                    color = walkPriceText,
                                                                    fontSize = 11.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    maxLines = 1,
                                                                    softWrap = false
                                                                )
                                                            }
                                                        }

                                                        IconButton(
                                                            onClick = {
                                                                HapticHelper.success(view, context)
                                                                viewModel.toggleWalkCompletion(walk)
                                                            },
                                                            modifier = Modifier.size(32.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = if (isDone) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                                                                contentDescription = "Статус",
                                                                tint = if (isDone) walkDoneCheckTint else OliveGreySecondary,
                                                                modifier = Modifier.size(24.dp)
                                                            )
                                                        }
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(4.dp))

                                                // 2. Вторая строка: имя клиента крупным шрифтом (16.sp SemiBold) по левому краю; справа в этой же строке компактная кнопка меню «⋮» (DropdownMenu) для «Перенести дату», «Редактировать», «Удалить»
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .then(
                                                            if (isCompactView) {
                                                                Modifier.clip(RoundedCornerShape(6.dp)).clickable {
                                                                    HapticHelper.lightClick(view, context)
                                                                    isCardExpanded = false
                                                                }
                                                            } else Modifier
                                                        ),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = client?.fullName ?: "",
                                                        fontWeight = FontWeight.SemiBold,
                                                        fontSize = 16.sp,
                                                        color = if (isDone) (if (isDark) Color(0xFFBAC4BE) else OliveGreySecondary) else (if (isDark) Color.White else CharcoalPrimary),
                                                        textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                        modifier = Modifier.weight(1f)
                                                    )

                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        if (isCompactView) {
                                                            IconButton(
                                                                onClick = {
                                                                    HapticHelper.lightClick(view, context)
                                                                    isCardExpanded = false
                                                                },
                                                                modifier = Modifier.size(38.dp)
                                                            ) {
                                                                Icon(
                                                                    imageVector = Icons.Rounded.KeyboardArrowUp,
                                                                    contentDescription = "Свернуть",
                                                                    tint = OliveGreySecondary,
                                                                    modifier = Modifier.size(22.dp)
                                                                )
                                                            }
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                        }

                                                        Box {
                                                            var showMenu by remember { mutableStateOf(false) }

                                                            IconButton(
                                                                onClick = { showMenu = true },
                                                                modifier = Modifier.size(28.dp)
                                                            ) {
                                                                Icon(
                                                                    imageVector = Icons.Rounded.MoreVert,
                                                                    contentDescription = "Действия",
                                                                    tint = OliveGreySecondary,
                                                                    modifier = Modifier.size(20.dp)
                                                                )
                                                            }

                                                            DropdownMenu(
                                                                expanded = showMenu,
                                                                onDismissRequest = { showMenu = false },
                                                                modifier = Modifier.background(CanvasAlabaster)
                                                            ) {
                                                                DropdownMenuItem(
                                                                    text = { Text("Перенести дату", fontSize = 14.sp, color = CharcoalPrimary) },
                                                                    leadingIcon = {
                                                                        Icon(
                                                                            Icons.Rounded.CalendarToday,
                                                                            contentDescription = null,
                                                                            tint = SageGreenPrimary,
                                                                            modifier = Modifier.size(18.dp)
                                                                        )
                                                                    },
                                                                    onClick = {
                                                                        showMenu = false
                                                                        walkToReschedule = walk
                                                                    }
                                                                )
                                                                DropdownMenuItem(
                                                                    text = { Text("Редактировать", fontSize = 14.sp, color = CharcoalPrimary) },
                                                                    leadingIcon = {
                                                                        Icon(
                                                                            Icons.Rounded.Edit,
                                                                            contentDescription = null,
                                                                            tint = SageGreenPrimary,
                                                                            modifier = Modifier.size(18.dp)
                                                                        )
                                                                    },
                                                                    onClick = {
                                                                        showMenu = false
                                                                        walkToEdit = walk
                                                                        showWalkFormDialog = true
                                                                    }
                                                                )
                                                                DropdownMenuItem(
                                                                    text = { Text("В календарь", fontSize = 14.sp, color = CharcoalPrimary) },
                                                                    leadingIcon = {
                                                                        Icon(
                                                                            Icons.Rounded.CalendarMonth,
                                                                            contentDescription = null,
                                                                            tint = SageGreenPrimary,
                                                                            modifier = Modifier.size(18.dp)
                                                                        )
                                                                    },
                                                                    onClick = {
                                                                        showMenu = false
                                                                        if (client != null) {
                                                                            IntentHelper.openGoogleCalendar(context, client, walk)
                                                                        }
                                                                    }
                                                                )
                                                                DropdownMenuItem(
                                                                    text = { Text("Удалить", fontSize = 14.sp, color = DangerRedText) },
                                                                    leadingIcon = {
                                                                        Icon(
                                                                            Icons.Rounded.Delete,
                                                                            contentDescription = null,
                                                                            tint = DangerRedText,
                                                                            modifier = Modifier.size(18.dp)
                                                                        )
                                                                    },
                                                                    onClick = {
                                                                        showMenu = false
                                                                        walkToDelete = walk
                                                                    }
                                                                )
                                                            }
                                                        }
                                                    }
                                                }

                                                // 3. Блок питомцев: аккуратные компактные пилюли по ширине текста (FlowRow), если у питомца нет описания особенностей; при наличии описания выводим светлым контрастным текстом внутри плашки
                                                if (pets.isNotEmpty()) {
                                                    Spacer(modifier = Modifier.height(6.dp))
                                                    FlowRow(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                                    ) {
                                                        pets.forEach { pet ->
                                                            val detailsLower = pet.breedOrDetails.lowercase()
                                                            val isAlert = detailsLower.contains("агрессив") || detailsLower.contains("успокоительн")
                                                            val hasDetails = pet.breedOrDetails.isNotBlank()

                                                            val petBg = when {
                                                                isAlert && isDark -> Color(0xFF382A1C)
                                                                isAlert && !isDark -> Color(0xFFFFF3E0)
                                                                isDark -> Color(0xFF233129)
                                                                else -> MintLight
                                                            }
                                                            val petBorder = when {
                                                                isAlert && isDark -> Color(0xFFD97706).copy(alpha = 0.5f)
                                                                isAlert && !isDark -> Color(0xFFF59E0B).copy(alpha = 0.6f)
                                                                isDark -> Color(0xFF334B3D)
                                                                else -> SoftMint
                                                            }
                                                            val petNameColor = when {
                                                                isAlert && isDark -> Color(0xFFFFD59E)
                                                                isAlert && !isDark -> Color(0xFF92400E)
                                                                isDark -> Color.White
                                                                else -> CharcoalPrimary
                                                            }
                                                            val petDetailsColor = when {
                                                                isAlert && isDark -> Color(0xFFFFE0B2).copy(alpha = 0.9f)
                                                                isAlert && !isDark -> Color(0xFFB45309)
                                                                isDark -> Color.White.copy(alpha = 0.85f)
                                                                else -> Color(0xFF38433E)
                                                            }

                                                            Row(
                                                                modifier = (if (hasDetails) Modifier.fillMaxWidth() else Modifier)
                                                                    .clip(RoundedCornerShape(8.dp))
                                                                    .background(petBg)
                                                                    .border(0.5.dp, petBorder, RoundedCornerShape(8.dp))
                                                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                                                verticalAlignment = Alignment.CenterVertically
                                                            ) {
                                                                Text(
                                                                    text = "${pet.type.icon} ${pet.name}",
                                                                    color = petNameColor,
                                                                    fontSize = 12.sp,
                                                                    fontWeight = FontWeight.SemiBold
                                                                )
                                                                if (hasDetails) {
                                                                    Text(
                                                                        text = " • ${pet.breedOrDetails}",
                                                                        color = petDetailsColor,
                                                                        fontSize = 12.sp,
                                                                        modifier = Modifier.padding(start = 2.dp),
                                                                        maxLines = 2,
                                                                        overflow = TextOverflow.Ellipsis
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    }
                                                }

                                                // 4. Блок адреса: при отображении в карточке убирай избыточный префикс «Санкт-Петербург, » (оставляй сразу улицу, дом, подъезд, этаж, кв.); при тапе на адрес открывай диалог подтверждения «Открыть карту?».
                                                if (client != null) {
                                                    val cleanedStreet = cleanAddress(client.addressStreet)
                                                    val fullAddr = buildString {
                                                        append(cleanedStreet)
                                                        if (client.entrance.isNotBlank()) append(", под. ${client.entrance}")
                                                        if (client.floor.isNotBlank()) append(", эт. ${client.floor}")
                                                        if (client.apartment.isNotBlank()) append(", кв. ${client.apartment}")
                                                    }
                                                    if (fullAddr.isNotBlank()) {
                                                        Spacer(modifier = Modifier.height(6.dp))
                                                        Row(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .clip(RoundedCornerShape(6.dp))
                                                                .clickable {
                                                                    HapticHelper.lightClick(view, context)
                                                                    walkToOpenMap = walk
                                                                }
                                                                .padding(vertical = 3.dp),
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Rounded.LocationOn,
                                                                contentDescription = "Адрес",
                                                                tint = walkLocationTint,
                                                                modifier = Modifier.size(16.dp)
                                                            )
                                                            Spacer(modifier = Modifier.width(4.dp))
                                                            Text(
                                                                text = fullAddr,
                                                                fontSize = 12.sp,
                                                                color = walkPrimaryText,
                                                                maxLines = 2,
                                                                overflow = TextOverflow.Ellipsis
                                                            )
                                                        }
                                                    }
                                                }

                                                // 5. Оба типа заметок (не взаимоисключающие!): подтягивай как постоянную заметку/домофон из карточки клиента (client.note / intercom / comment), так и заметку конкретной прогулки (walk.note / comment). Выводи их отдельными контрастными строками (13.sp, без обрезания на 1 строке), если заполнены:
                                                // * 🔑 Постоянная заметка клиента (домофон, ключи, общие правила)
                                                // * 📝 Заметка к этому выгулу (например, «пораньше»)
                                                val clientNote = client?.clientComment?.trim() ?: ""
                                                val walkNote = walk.walkComment.trim()

                                                if (clientNote.isNotBlank()) {
                                                    Spacer(modifier = Modifier.height(6.dp))
                                                    Text(
                                                        text = "🔑 $clientNote",
                                                        fontSize = 13.sp,
                                                        color = if (isDark) Color(0xFFFFD59E) else Color(0xFF92400E),
                                                        lineHeight = 18.sp,
                                                        modifier = Modifier.fillMaxWidth()
                                                    )
                                                }

                                                if (walkNote.isNotBlank()) {
                                                    Spacer(modifier = Modifier.height(6.dp))
                                                    Text(
                                                        text = "📝 $walkNote",
                                                        fontSize = 13.sp,
                                                        color = if (isDark) Color(0xFFA5D6A7) else Color(0xFF2E6948),
                                                        lineHeight = 18.sp,
                                                        modifier = Modifier.fillMaxWidth()
                                                    )
                                                }

                                                Spacer(modifier = Modifier.height(10.dp))

                                                // 6. Нижний ряд действий: 3 удобные равноширинные кнопки с иконками и подписями: «Маршрут», «Звонок», «Чат»
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    // Маршрут
                                                    Button(
                                                        onClick = {
                                                            HapticHelper.lightClick(view, context)
                                                            viewModel.routeSingleWalk(context, walk)
                                                        },
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .heightIn(min = 48.dp),
                                                        colors = ButtonDefaults.buttonColors(
                                                            containerColor = SoftMint,
                                                            contentColor = DeepForest
                                                        ),
                                                        shape = RoundedCornerShape(10.dp),
                                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                                                    ) {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.Center
                                                        ) {
                                                            Icon(
                                                                Icons.Rounded.Directions,
                                                                contentDescription = null,
                                                                modifier = Modifier.size(16.dp)
                                                            )
                                                            Spacer(modifier = Modifier.width(4.dp))
                                                            Text(
                                                                "Маршрут",
                                                                fontSize = 12.sp,
                                                                fontWeight = FontWeight.SemiBold,
                                                                maxLines = 1
                                                            )
                                                        }
                                                    }

                                                    // Звонок
                                                    Button(
                                                        onClick = {
                                                            if (client != null) {
                                                                if (client.secondaryContactPhone.isNotBlank()) {
                                                                    clientForCallChooser = client
                                                                } else {
                                                                    IntentHelper.dialPhone(context, client.phone)
                                                                }
                                                            }
                                                        },
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .heightIn(min = 48.dp),
                                                        colors = ButtonDefaults.buttonColors(
                                                            containerColor = SandBeige,
                                                            contentColor = CharcoalPrimary
                                                        ),
                                                        shape = RoundedCornerShape(10.dp),
                                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                                                    ) {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.Center
                                                        ) {
                                                            Icon(
                                                                Icons.Rounded.Call,
                                                                contentDescription = null,
                                                                modifier = Modifier.size(15.dp)
                                                            )
                                                            Spacer(modifier = Modifier.width(4.dp))
                                                            Text(
                                                                "Звонок",
                                                                fontSize = 12.sp,
                                                                fontWeight = FontWeight.SemiBold,
                                                                maxLines = 1
                                                            )
                                                        }
                                                    }

                                                    // Чат
                                                    Button(
                                                        onClick = {
                                                            if (client != null) {
                                                                IntentHelper.openMessenger(
                                                                    context,
                                                                    client.messengerType,
                                                                    client.messengerIdentifier.ifBlank { client.phone }
                                                                )
                                                            }
                                                        },
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .heightIn(min = 48.dp),
                                                        colors = ButtonDefaults.buttonColors(
                                                            containerColor = MintLight,
                                                            contentColor = DeepForest
                                                        ),
                                                        shape = RoundedCornerShape(10.dp),
                                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                                                    ) {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.Center
                                                        ) {
                                                            Icon(
                                                                Icons.Rounded.Chat,
                                                                contentDescription = null,
                                                                modifier = Modifier.size(15.dp)
                                                            )
                                                            Spacer(modifier = Modifier.width(4.dp))
                                                            Text(
                                                                "Чат",
                                                                fontSize = 12.sp,
                                                                fontWeight = FontWeight.SemiBold,
                                                                maxLines = 1
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                }
                            }
                        }
                    }
        }

    if (!isMapEnabled) {
        // PURE PLAN MODE (Fast, no WebView, full screen native plan)
        Column(
            modifier = modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(top = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                // Top Date Selector
                ScheduleTopControls(
                    selectedDate = selectedDate,
                    isCalendarExpanded = isCalendarExpanded,
                    onToggleCalendar = { isCalendarExpanded = !isCalendarExpanded },
                    onPrevDay = { viewModel.setSelectedDate(selectedDate.minusDays(1)) },
                    onNextDay = { viewModel.setSelectedDate(selectedDate.plusDays(1)) }
                )

                AnimatedVisibility(
                    visible = isCalendarExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    MonthCalendarDropdown(
                        selectedDate = selectedDate,
                        walksCountByEpochDay = walksCountByEpochDay,
                        onDateSelected = { date ->
                            viewModel.setSelectedDate(date)
                            isCalendarExpanded = false
                        },
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                TransportSlider(
                    currentMode = routingMode,
                    onModeChanged = { viewModel.setRoutingMode(it) }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            WalksListContent(
                contentModifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }
    } else {
        // MAP + PLAN MODE (Fullscreen interactive map + 3-position draggable sheet)
        BoxWithConstraints(modifier = modifier.fillMaxSize()) {
            val screenMaxHeight = maxHeight
            val collapsedHeight = 88.dp
            val middleHeight = screenMaxHeight * 0.48f
            val expandedHeight = screenMaxHeight - 80.dp

            var sheetPosition by remember { mutableStateOf(SheetPosition.MIDDLE) }

            val animatedSheetHeight by animateDpAsState(
                targetValue = when (sheetPosition) {
                    SheetPosition.COLLAPSED -> collapsedHeight
                    SheetPosition.MIDDLE -> middleHeight
                    SheetPosition.EXPANDED -> expandedHeight
                },
                animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f),
                label = "sheet_anim_height"
            )

            // Background: 100% Fullscreen Map
            YandexMapView(
                apiKey = apiKey,
                points = mapMarkers,
                routingMode = routingMode,
                modifier = Modifier.fillMaxSize()
            )

            // Floating Controls overlay
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp)
            ) {
                ScheduleTopControls(
                    selectedDate = selectedDate,
                    isCalendarExpanded = isCalendarExpanded,
                    onToggleCalendar = { isCalendarExpanded = !isCalendarExpanded },
                    onPrevDay = { viewModel.setSelectedDate(selectedDate.minusDays(1)) },
                    onNextDay = { viewModel.setSelectedDate(selectedDate.plusDays(1)) }
                )

                AnimatedVisibility(
                    visible = isCalendarExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    MonthCalendarDropdown(
                        selectedDate = selectedDate,
                        walksCountByEpochDay = walksCountByEpochDay,
                        onDateSelected = { date ->
                            viewModel.setSelectedDate(date)
                            isCalendarExpanded = false
                        },
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                TransportSlider(
                    currentMode = routingMode,
                    onModeChanged = { viewModel.setRoutingMode(it) }
                )
            }

            // Draggable / Snappable Bottom Sheet (Fixable at Collapsed, Middle, Expanded)
            var dragAccumulator by remember { mutableFloatStateOf(0f) }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(animatedSheetHeight)
                    .shadow(16.dp, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(CanvasAlabaster)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Drag handle with drag gesture & click cycle
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                sheetPosition = when (sheetPosition) {
                                    SheetPosition.COLLAPSED -> SheetPosition.MIDDLE
                                    SheetPosition.MIDDLE -> SheetPosition.EXPANDED
                                    SheetPosition.EXPANDED -> SheetPosition.COLLAPSED
                                }
                            }
                            .pointerInput(Unit) {
                                detectVerticalDragGestures(
                                    onDragStart = { dragAccumulator = 0f },
                                    onDragEnd = {
                                        if (dragAccumulator < -40f) {
                                            sheetPosition = when (sheetPosition) {
                                                SheetPosition.COLLAPSED -> SheetPosition.MIDDLE
                                                SheetPosition.MIDDLE -> SheetPosition.EXPANDED
                                                SheetPosition.EXPANDED -> SheetPosition.EXPANDED
                                            }
                                        } else if (dragAccumulator > 40f) {
                                            sheetPosition = when (sheetPosition) {
                                                SheetPosition.EXPANDED -> SheetPosition.MIDDLE
                                                SheetPosition.MIDDLE -> SheetPosition.COLLAPSED
                                                SheetPosition.COLLAPSED -> SheetPosition.COLLAPSED
                                            }
                                        }
                                        dragAccumulator = 0f
                                    },
                                    onVerticalDrag = { _, dragAmount ->
                                        dragAccumulator += dragAmount
                                    }
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(vertical = 10.dp)
                                .width(42.dp)
                                .height(4.dp)
                                .clip(CircleShape)
                                .background(SageGreenPrimary.copy(alpha = 0.5f))
                        )
                    }

                    WalksListContent(
                        contentModifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                }
            }
        }
    }

    // Delete Walk Dialog (Clean Layout with separate Cancel button)
    if (walkToDelete != null) {
        val walk = walkToDelete!!
        val isRecurring = !walk.seriesId.isNullOrBlank()

        Dialog(onDismissRequest = { walkToDelete = null }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = CanvasAlabaster),
                modifier = Modifier.fillMaxWidth(0.92f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = if (isRecurring) "Удаление серии выгулов" else "Удалить выгул?",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = CharcoalPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (isRecurring)
                            "Этот выгул повторяется по расписанию. Выберите вариант удаления:"
                        else
                            "Вы уверены, что хотите удалить этот запланированный выгул?",
                        fontSize = 13.sp,
                        color = OliveGreySecondary
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    if (isRecurring) {
                        Button(
                            onClick = {
                                viewModel.deleteWalk(walk, deleteEntireSeries = false)
                                walkToDelete = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SandBeige, contentColor = CharcoalPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                        ) {
                            Text("Только этот выгул", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                viewModel.deleteWalk(walk, deleteEntireSeries = true)
                                walkToDelete = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD9534F), contentColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                        ) {
                            Text("Всю серию", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        TextButton(
                            onClick = { walkToDelete = null },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Отмена", color = OliveGreySecondary, fontSize = 14.sp)
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = { walkToDelete = null }) {
                                Text("Отмена", color = OliveGreySecondary)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    viewModel.deleteWalk(walk, deleteEntireSeries = false)
                                    walkToDelete = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD9534F), contentColor = Color.White),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Удалить")
                            }
                        }
                    }
                }
            }
        }
    }

    // Reschedule Walk Dialog
    if (walkToReschedule != null) {
        val targetWalk = walkToReschedule!!
        AppCalendarDialog(
            initialDate = LocalDate.ofEpochDay(targetWalk.dateEpochDay),
            onDateSelected = { newDate ->
                viewModel.updateWalk(targetWalk.copy(dateEpochDay = newDate.toEpochDay()))
                walkToReschedule = null
            },
            onDismiss = { walkToReschedule = null }
        )
    }

    // Confirm Open Map Dialog
    if (walkToOpenMap != null) {
        val targetWalk = walkToOpenMap!!
        val targetClient = clientsMap[targetWalk.clientId]
        val cleanedStreet = cleanAddress(targetClient?.addressStreet ?: "")
        val addressDisplay = buildString {
            append(cleanedStreet)
            if (!targetClient?.entrance.isNullOrBlank()) append(", под. ${targetClient.entrance}")
            if (!targetClient?.floor.isNullOrBlank()) append(", эт. ${targetClient.floor}")
            if (!targetClient?.apartment.isNullOrBlank()) append(", кв. ${targetClient.apartment}")
        }
        AlertDialog(
            onDismissRequest = { walkToOpenMap = null },
            title = {
                Text(
                    text = "Открыть карту?",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = CharcoalPrimary
                )
            },
            text = {
                Text(
                    text = if (addressDisplay.isNotBlank())
                        "Построить маршрут до адреса:\n$addressDisplay"
                    else
                        "Построить маршрут к клиенту?",
                    fontSize = 14.sp,
                    color = OliveGreySecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        HapticHelper.lightClick(view, context)
                        viewModel.routeSingleWalk(context, targetWalk)
                        walkToOpenMap = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SageGreenPrimary, contentColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Открыть")
                }
            },
            dismissButton = {
                TextButton(onClick = { walkToOpenMap = null }) {
                    Text("Отмена", color = OliveGreySecondary)
                }
            },
            containerColor = CanvasAlabaster,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Add or Edit Walk Dialog
    if (showWalkFormDialog) {
        WalkFormDialog(
            viewModel = viewModel,
            clients = allClients,
            initialDate = selectedDate,
            walkToEdit = walkToEdit,
            onDismiss = {
                showWalkFormDialog = false
                walkToEdit = null
            },
            onSave = { walkId, clientId, category, date, startTime, duration, price, comment, isRecurring, days, endDate, updateEntireSeries ->
                if (walkId != 0L && walkToEdit != null) {
                    val updated = walkToEdit!!.copy(
                        clientId = clientId,
                        category = category,
                        dateEpochDay = date.toEpochDay(),
                        startTimeMinutes = startTime,
                        durationMinutes = duration,
                        price = price,
                        walkComment = comment
                    )
                    viewModel.updateWalkSeries(updated, updateEntireSeries = updateEntireSeries) {
                        showWalkFormDialog = false
                        walkToEdit = null
                    }
                } else {
                    viewModel.saveWalk(
                        clientId = clientId,
                        category = category,
                        date = date,
                        startTimeMinutes = startTime,
                        durationMinutes = duration,
                        price = price,
                        walkComment = comment,
                        isRecurring = isRecurring,
                        selectedDays = days,
                        recurrenceEndDate = endDate
                    ) {
                        showWalkFormDialog = false
                        walkToEdit = null
                    }
                }
            }
        )
    }

    // Choose Contact to Call Dialog
    if (clientForCallChooser != null) {
        val targetClient = clientForCallChooser!!
        AlertDialog(
            onDismissRequest = { clientForCallChooser = null },
            title = {
                Text(
                    text = "Кому позвонить?",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = CharcoalPrimary
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Primary Contact
                    Button(
                        onClick = {
                            val phone = targetClient.phone
                            clientForCallChooser = null
                            IntentHelper.dialPhone(context, phone)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SoftMint,
                            contentColor = DeepForest
                        )
                    ) {
                        Icon(Icons.Rounded.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = targetClient.fullName.ifBlank { "Основной контакт" },
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = targetClient.phone,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }

                    // Secondary Contact
                    Button(
                        onClick = {
                            val phone = targetClient.secondaryContactPhone
                            clientForCallChooser = null
                            IntentHelper.dialPhone(context, phone)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SandBeige,
                            contentColor = CharcoalPrimary
                        )
                    ) {
                        Icon(Icons.Rounded.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = targetClient.secondaryContactName.ifBlank { "Второй хозяин" },
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = targetClient.secondaryContactPhone,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { clientForCallChooser = null }) {
                    Text("Отмена", color = OliveGreySecondary)
                }
            },
            containerColor = CanvasAlabaster,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

private data class CategoryFormLabels(
    val dialogTitle: String,
    val dateLabel: String,
    val priceLabel: String,
    val commentLabel: String
)

private fun getCategoryFormLabels(category: WalkCategory, isEdit: Boolean): CategoryFormLabels {
    return when (category) {
        WalkCategory.REGULAR_WALK -> CategoryFormLabels(
            dialogTitle = if (isEdit) "Редактировать выгул" else "Запланировать выгул",
            dateLabel = "Дата выгула",
            priceLabel = "Стоимость выгула (₽)",
            commentLabel = "Заметка к выгулу (что отработать, самочувствие)"
        )
        WalkCategory.CANINE_TRAINING, WalkCategory.BEHAVIOR_CORRECTION -> CategoryFormLabels(
            dialogTitle = if (isEdit) "Редактировать занятие" else "Запланировать занятие",
            dateLabel = "Дата занятия",
            priceLabel = "Стоимость занятия (₽)",
            commentLabel = "Заметка к занятию (цель, прогресс, ДЗ)"
        )
        WalkCategory.PET_SITTING -> CategoryFormLabels(
            dialogTitle = if (isEdit) "Редактировать визит / передержку" else "Запланировать визит",
            dateLabel = "Дата визита / передержки",
            priceLabel = "Стоимость услуги (₽)",
            commentLabel = "Заметка к визиту (кормление, уход)"
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun WalkFormDialog(
    viewModel: MainViewModel,
    clients: List<ClientEntity>,
    initialDate: LocalDate,
    walkToEdit: WalkEventEntity? = null,
    onDismiss: () -> Unit,
    onSave: (
        walkId: Long,
        clientId: Long,
        category: WalkCategory,
        date: LocalDate,
        startTimeMinutes: Int,
        durationMinutes: Int,
        price: Int,
        comment: String,
        isRecurring: Boolean,
        selectedDays: Set<DayOfWeek>,
        recurrenceEndDate: LocalDate?,
        updateEntireSeries: Boolean
    ) -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current
    val isDark = LocalIsDarkTheme.current
    val json = remember { Json { ignoreUnknownKeys = true } }
    val pricePresets by viewModel.pricePresets.collectAsState()

    var selectedClientId by remember {
        mutableStateOf(walkToEdit?.clientId ?: clients.firstOrNull()?.id ?: 0L)
    }
    var selectedCategory by remember {
        mutableStateOf(walkToEdit?.category ?: WalkCategory.REGULAR_WALK)
    }
    val formLabels = remember(selectedCategory, walkToEdit) {
        getCategoryFormLabels(selectedCategory, walkToEdit != null)
    }
    var walkDate by remember {
        mutableStateOf(
            if (walkToEdit != null) LocalDate.ofEpochDay(walkToEdit.dateEpochDay) else initialDate
        )
    }
    var showWalkDatePicker by remember { mutableStateOf(false) }
    var startTimeMinutes by remember {
        mutableIntStateOf(walkToEdit?.startTimeMinutes ?: (19 * 60))
    }
    var showTimePicker by remember { mutableStateOf(false) }
    var durationMinutes by remember {
        mutableIntStateOf(walkToEdit?.durationMinutes ?: 45)
    }
    var durationInput by remember {
        mutableStateOf((walkToEdit?.durationMinutes ?: 45).toString())
    }
    var price by remember {
        mutableIntStateOf(walkToEdit?.price ?: 0)
    }
    var priceInput by remember {
        mutableStateOf(if ((walkToEdit?.price ?: 0) > 0) walkToEdit!!.price.toString() else "")
    }
    var walkComment by remember {
        mutableStateOf(walkToEdit?.walkComment ?: "")
    }

    // Recurrence Engine
    var isRecurring by remember {
        mutableStateOf(walkToEdit != null && !walkToEdit.seriesId.isNullOrBlank())
    }
    var selectedDays by remember {
        mutableStateOf(setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY))
    }
    var recurrenceEndDate by remember {
        mutableStateOf<LocalDate?>(null)
    }
    var showRecurrenceEndDatePicker by remember { mutableStateOf(false) }

    var clientMenuExpanded by remember { mutableStateOf(false) }
    var showSeriesEditConfirmDialog by remember { mutableStateOf(false) }

    val allWalks by viewModel.allWalks.collectAsState()
    var conflictingClientName by remember { mutableStateOf<String?>(null) }
    var pendingSaveCallback by remember { mutableStateOf<(() -> Unit)?>(null) }

    fun checkOverlapAndProceed(action: () -> Unit) {
        val validDuration = durationMinutes.coerceAtLeast(10)
        val startMin = startTimeMinutes
        val endMin = startTimeMinutes + validDuration
        val walkEpoch = walkDate.toEpochDay()
        val conflictingWalk = allWalks.firstOrNull { walk ->
            walk.id != (walkToEdit?.id ?: 0L) &&
            walk.dateEpochDay == walkEpoch &&
            startMin < (walk.startTimeMinutes + walk.durationMinutes) &&
            walk.startTimeMinutes < endMin
        }
        if (conflictingWalk != null) {
            val client = clients.firstOrNull { it.id == conflictingWalk.clientId }
            val name = client?.fullName ?: "Клиент"
            conflictingClientName = name
            pendingSaveCallback = action
        } else {
            action()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .statusBarsPadding()
                .padding(vertical = 20.dp)
                .imePadding(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CanvasAlabaster)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                Text(
                    text = formLabels.dialogTitle,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = CharcoalPrimary
                )

                Spacer(modifier = Modifier.height(14.dp))

                if (clients.isEmpty()) {
                    Text(
                        text = "Сначала добавьте клиента во вкладке «Клиенты»!",
                        color = Color(0xFFD9534F),
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                        Text("Закрыть")
                    }
                    return@Column
                }

                // Client Selector
                Text("Клиент и питомцы:", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(4.dp))

                ExposedDropdownMenuBox(
                    expanded = clientMenuExpanded,
                    onExpandedChange = { clientMenuExpanded = !clientMenuExpanded }
                ) {
                    val currentClient = clients.firstOrNull { it.id == selectedClientId } ?: clients.first()
                    val pets = remember(currentClient.petsJson) {
                        try {
                            json.decodeFromString<List<PetItem>>(currentClient.petsJson)
                        } catch (e: Exception) {
                            emptyList()
                        }
                    }
                    val petsText = pets.joinToString { "${it.type.icon} ${it.name}" }

                    AppTextField(
                        value = "${currentClient.fullName} (${if (petsText.isNotBlank()) petsText else "Без питомцев"})",
                        onValueChange = {},
                        readOnly = true,
                        label = "Клиент и питомцы",
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = clientMenuExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )

                    ExposedDropdownMenu(
                        expanded = clientMenuExpanded,
                        onDismissRequest = { clientMenuExpanded = false }
                    ) {
                        clients.forEach { c ->
                            val cPets = try {
                                json.decodeFromString<List<PetItem>>(c.petsJson)
                            } catch (e: Exception) {
                                emptyList()
                            }
                            val cPetsText = cPets.joinToString { "${it.type.icon} ${it.name}" }
                            DropdownMenuItem(
                                text = { Text("${c.fullName} — $cPetsText") },
                                onClick = {
                                    selectedClientId = c.id
                                    clientMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Walk Date Selector with overlay for 100% reliable tap
                Text("${formLabels.dateLabel}:", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(4.dp))
                val dateFormatted = walkDate.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale("ru")))
                Box(modifier = Modifier.fillMaxWidth()) {
                    AppTextField(
                        value = dateFormatted.replaceFirstChar { it.uppercase() },
                        onValueChange = {},
                        readOnly = true,
                        label = formLabels.dateLabel,
                        trailingIcon = {
                            Icon(Icons.Rounded.CalendarToday, contentDescription = "Выбрать дату", tint = SageGreenPrimary)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable {
                                HapticHelper.lightClick(view, context)
                                showWalkDatePicker = true
                            }
                    )
                }

                if (showWalkDatePicker) {
                    AppCalendarDialog(
                        initialDate = walkDate,
                        onDateSelected = { walkDate = it },
                        onDismiss = { showWalkDatePicker = false }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Service Type / Category Selector
                Text("Тип услуги:", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(6.dp))

                WalkCategory.entries.chunked(2).forEach { rowCategories ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        rowCategories.forEach { cat ->
                            val isSelected = cat == selectedCategory
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(CircleShape)
                                    .background(if (isSelected) cat.composeColor else SandBeige)
                                    .clickable {
                                        HapticHelper.tick(view, context)
                                        selectedCategory = cat
                                    }
                                    .padding(vertical = 8.dp, horizontal = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = cat.title,
                                    color = if (isSelected) Color.White else CharcoalPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Time & Duration with clickable overlay & manual input
                val hours = startTimeMinutes / 60
                val mins = startTimeMinutes % 60
                val timeString = String.format("%02d:%02d", hours, mins)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Start Time with overlay
                    Box(modifier = Modifier.weight(1f)) {
                        AppTextField(
                            value = timeString,
                            onValueChange = {},
                            readOnly = true,
                            label = "Время начала",
                            trailingIcon = {
                                Icon(Icons.Rounded.Schedule, contentDescription = "Выбрать время", tint = SageGreenPrimary)
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { showTimePicker = true }
                        )
                    }

                    // Duration Input Field (Editable + Quick Chips)
                    AppTextField(
                        value = durationInput,
                        onValueChange = { input ->
                            val digits = input.filter { it.isDigit() }.take(3)
                            durationInput = digits
                            digits.toIntOrNull()?.let { durationMinutes = it }
                        },
                        label = "Длит. (мин)",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Quick duration chips
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(15, 30, 45, 60, 90).forEach { minsPreset ->
                        val isSel = durationMinutes == minsPreset
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) SageGreenPrimary else SandBeige)
                                .clickable {
                                    HapticHelper.tick(view, context)
                                    durationMinutes = minsPreset
                                    durationInput = minsPreset.toString()
                                }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "$minsPreset мин",
                                fontSize = 11.sp,
                                color = if (isSel) Color.White else CharcoalPrimary,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }

                if (showTimePicker) {
                    KinologTimePickerDialog(
                        initialHour = startTimeMinutes / 60,
                        initialMinute = startTimeMinutes % 60,
                        onDismiss = { showTimePicker = false },
                        onTimeSelected = { h, m ->
                            startTimeMinutes = h * 60 + m
                            showTimePicker = false
                        }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Price Field with Quick Preset Chips
                Text("${formLabels.priceLabel}:", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(4.dp))
                AppTextField(
                    value = priceInput,
                    onValueChange = { input ->
                        val digits = input.filter { it.isDigit() }.take(6)
                        priceInput = digits
                        price = digits.toIntOrNull() ?: 0
                    },
                    label = formLabels.priceLabel,
                    leadingIcon = { Icon(Icons.Rounded.Payments, contentDescription = null, tint = SageGreenPrimary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    pricePresets.forEach { presetPrice ->
                        val isSel = price == presetPrice
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) SageGreenPrimary else SandBeige)
                                .clickable {
                                    HapticHelper.tick(view, context)
                                    price = presetPrice
                                    priceInput = presetPrice.toString()
                                }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "$presetPrice ₽",
                                fontSize = 11.sp,
                                color = if (isSel) Color.White else CharcoalPrimary,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }

                // Recurrence Engine (only when creating a new walk)
                if (walkToEdit == null) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Регулярный график:", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        AnimatedSegmentedControl(
                            items = listOf("Разовый", "Серия"),
                            selectedIndex = if (!isRecurring) 0 else 1,
                            onItemSelected = { isRecurring = it == 1 },
                            modifier = Modifier.width(170.dp),
                            height = 36.dp
                        )
                    }

                    AnimatedVisibility(visible = isRecurring) {
                        Column(modifier = Modifier.padding(top = 10.dp)) {
                            Text("Дни недели:", fontSize = 12.sp, color = OliveGreySecondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                val daysOfWeek = listOf(
                                    DayOfWeek.MONDAY to "Пн",
                                    DayOfWeek.TUESDAY to "Вт",
                                    DayOfWeek.WEDNESDAY to "Ср",
                                    DayOfWeek.THURSDAY to "Чт",
                                    DayOfWeek.FRIDAY to "Пт",
                                    DayOfWeek.SATURDAY to "Сб",
                                    DayOfWeek.SUNDAY to "Вс"
                                )
                                daysOfWeek.forEach { (day, label) ->
                                    val isSelected = selectedDays.contains(day)
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) SageGreenPrimary else SandBeige)
                                            .clickable {
                                                HapticHelper.tick(view, context)
                                                selectedDays = if (isSelected) {
                                                    if (selectedDays.size > 1) selectedDays - day else selectedDays
                                                } else {
                                                    selectedDays + day
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            color = if (isSelected) Color.White else CharcoalPrimary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // End Date Picker with clickable overlay & clear button
                            val endFormatted = if (recurrenceEndDate != null) {
                                recurrenceEndDate!!.format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("ru")))
                            } else {
                                "Бессрочно (без конечной даты)"
                            }
                            Box(modifier = Modifier.fillMaxWidth()) {
                                AppTextField(
                                    value = endFormatted,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = "Повторять до даты",
                                    trailingIcon = {
                                        if (recurrenceEndDate != null) {
                                            IconButton(
                                                onClick = {
                                                    HapticHelper.lightClick(view, context)
                                                    recurrenceEndDate = null
                                                },
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Icon(
                                                    Icons.Rounded.Close,
                                                    contentDescription = "Сделать бессрочным",
                                                    tint = OliveGreySecondary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        } else {
                                            Icon(
                                                Icons.Rounded.CalendarMonth,
                                                contentDescription = "Выбрать дату",
                                                tint = SageGreenPrimary
                                            )
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .clickable {
                                            HapticHelper.lightClick(view, context)
                                            showRecurrenceEndDatePicker = true
                                        }
                                )
                            }

                            if (showRecurrenceEndDatePicker) {
                                AppCalendarDialog(
                                    initialDate = recurrenceEndDate ?: walkDate.plusMonths(1),
                                    allowClear = true,
                                    clearButtonText = "Сделать бессрочным",
                                    onDateSelected = { recurrenceEndDate = it },
                                    onClearDate = { recurrenceEndDate = null },
                                    onDismiss = { showRecurrenceEndDatePicker = false }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Walk Comment (at the very end of the form)
                AppTextField(
                    value = walkComment,
                    onValueChange = { walkComment = it },
                    label = formLabels.commentLabel,
                    placeholder = "домофон, поводок и т.д.",
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Отмена", color = OliveGreySecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val validDuration = durationMinutes.coerceAtLeast(10)
                            val isSeries = walkToEdit != null && !walkToEdit.seriesId.isNullOrBlank()
                            val hasKeyChanges = isSeries && (
                                walkToEdit!!.price != price ||
                                walkToEdit.startTimeMinutes != startTimeMinutes ||
                                walkToEdit.durationMinutes != validDuration
                            )

                            if (hasKeyChanges) {
                                checkOverlapAndProceed {
                                    showSeriesEditConfirmDialog = true
                                }
                            } else {
                                checkOverlapAndProceed {
                                    HapticHelper.success(view, context)
                                    onSave(
                                        walkToEdit?.id ?: 0L,
                                        selectedClientId,
                                        selectedCategory,
                                        walkDate,
                                        startTimeMinutes,
                                        validDuration,
                                        price,
                                        walkComment,
                                        isRecurring,
                                        selectedDays,
                                        recurrenceEndDate,
                                        false
                                    )
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SageGreenPrimary),
                        shape = CircleShape
                    ) {
                        Text(if (walkToEdit != null) "Сохранить" else "Запланировать", color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(120.dp))
            }
        }

        if (conflictingClientName != null) {
            AlertDialog(
                onDismissRequest = {
                    conflictingClientName = null
                    pendingSaveCallback = null
                },
                title = { Text("Накладка по времени", fontWeight = FontWeight.Bold, fontSize = 17.sp) },
                text = {
                    Text(
                        "В это время уже есть выгул: ${conflictingClientName}. Всё равно сохранить запись?",
                        fontSize = 14.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            conflictingClientName = null
                            val action = pendingSaveCallback
                            pendingSaveCallback = null
                            action?.invoke()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SageGreenPrimary)
                    ) {
                        Text("Всё равно сохранить", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            conflictingClientName = null
                            pendingSaveCallback = null
                        }
                    ) {
                        Text("Отмена", color = OliveGreySecondary)
                    }
                }
            )
        }

        if (showSeriesEditConfirmDialog && walkToEdit != null) {
            AlertDialog(
                onDismissRequest = { showSeriesEditConfirmDialog = false },
                title = { Text("Изменить серию выгулов?", fontWeight = FontWeight.Bold, fontSize = 17.sp) },
                text = {
                    Text(
                        "Вы изменили время, длительность или стоимость повторяющегося выгула. Применить эти изменения ко всей серии или только к текущему выгулу?",
                        fontSize = 14.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            HapticHelper.success(view, context)
                            showSeriesEditConfirmDialog = false
                            val validDuration = durationMinutes.coerceAtLeast(10)
                            onSave(
                                walkToEdit.id,
                                selectedClientId,
                                selectedCategory,
                                walkDate,
                                startTimeMinutes,
                                validDuration,
                                price,
                                walkComment,
                                isRecurring,
                                selectedDays,
                                recurrenceEndDate,
                                true
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SageGreenPrimary)
                    ) {
                        Text("Вся серия", color = Color.White)
                    }
                },
                dismissButton = {
                    Row {
                        TextButton(onClick = { showSeriesEditConfirmDialog = false }) {
                            Text("Отмена", color = OliveGreySecondary)
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Button(
                            onClick = {
                                HapticHelper.success(view, context)
                                showSeriesEditConfirmDialog = false
                                val validDuration = durationMinutes.coerceAtLeast(10)
                                onSave(
                                    walkToEdit.id,
                                    selectedClientId,
                                    selectedCategory,
                                    walkDate,
                                    startTimeMinutes,
                                    validDuration,
                                    price,
                                    walkComment,
                                    isRecurring,
                                    selectedDays,
                                    recurrenceEndDate,
                                    false
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SandBeige, contentColor = CharcoalPrimary)
                        ) {
                            Text("Только этот")
                        }
                    }
                }
            )
        }
    }
}
