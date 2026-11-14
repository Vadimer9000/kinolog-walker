package com.kinolog.walker.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Assessment
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.kinolog.walker.BuildConfig
import com.kinolog.walker.ui.MainViewModel
import com.kinolog.walker.ui.components.AnimatedSegmentedControl
import com.kinolog.walker.ui.components.AppCalendarDialog
import com.kinolog.walker.ui.components.AppTextField
import com.kinolog.walker.ui.components.GlassBox
import com.kinolog.walker.ui.components.NavigationTab
import com.kinolog.walker.ui.theme.LocalIsDarkTheme
import com.kinolog.walker.ui.theme.CharcoalPrimary
import com.kinolog.walker.ui.theme.DeepForest
import com.kinolog.walker.ui.theme.DividerMuted
import com.kinolog.walker.ui.theme.OliveGreySecondary
import com.kinolog.walker.ui.theme.PriceBadgeBg
import com.kinolog.walker.ui.theme.PriceBadgeText
import com.kinolog.walker.ui.theme.SageGreenPrimary
import com.kinolog.walker.ui.theme.SandBeige
import com.kinolog.walker.ui.theme.SoftMint
import com.kinolog.walker.util.HapticHelper
import com.kinolog.walker.util.PreferencesManager
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.kinolog.walker.util.ThemeMode
import java.time.LocalDate

@Composable
fun SettingsBackupScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val view = LocalView.current
    val isDark = LocalIsDarkTheme.current
    val earnedGreen = if (isDark) Color(0xFF81C784) else Color(0xFF2E7D32)
    val dividerColor = if (isDark) Color(0xFF28362D) else DividerMuted.copy(alpha = 0.6f)

    val currentApiKey by viewModel.yandexApiKey.collectAsState()
    var apiKeyInput by remember(currentApiKey) { mutableStateOf(currentApiKey) }

    val clientsMap by viewModel.clientsMap.collectAsState()
    val allWalks by viewModel.allWalks.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val isMapEnabled by viewModel.isMapEnabled.collectAsState()
    val isHapticFeedbackEnabled by viewModel.isHapticFeedbackEnabled.collectAsState()
    val isNotificationsEnabled by viewModel.isNotificationsEnabled.collectAsState()
    val notificationLeadTimeMinutes by viewModel.notificationLeadTimeMinutes.collectAsState()
    val currentPricePresets by viewModel.pricePresets.collectAsState()
    var pricePresetsInput by remember(currentPricePresets) {
        mutableStateOf(currentPricePresets.joinToString(", "))
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.setNotificationsEnabled(isGranted)
    }

    var pendingImportUri by remember { mutableStateOf<Uri?>(null) }

    // Import file launcher
    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            pendingImportUri = uri
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 12.dp)
            ) {
                IconButton(onClick = { viewModel.setTab(NavigationTab.SCHEDULE) }) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowBack,
                        contentDescription = "Назад к карте",
                        tint = CharcoalPrimary
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "Настройки и Бэкап",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = CharcoalPrimary
                    )
                    Text(
                        text = "Управление отчетами, уведомлениями, интерфейсом и базой",
                        fontSize = 12.sp,
                        color = OliveGreySecondary
                    )
                }
            }

            // 1. Отчетность за период (Первая карточка)
            GlassBox(
                modifier = Modifier.fillMaxWidth(),
                elevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Assessment, contentDescription = null, tint = SageGreenPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Отчетность за период",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = CharcoalPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Сводка выгулов, заработанных средств и распределение по клиентам.",
                        fontSize = 12.sp,
                        color = OliveGreySecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    var selectedPeriodIndex by remember { mutableIntStateOf(1) } // Default: Неделя
                    var currentPage by remember { mutableIntStateOf(0) }
                    val periods = listOf("Сегодня", "Неделя", "Месяц", "Период")

                    AnimatedSegmentedControl(
                        items = periods,
                        selectedIndex = selectedPeriodIndex,
                        onItemSelected = { selectedPeriodIndex = it },
                        itemLabel = { it },
                        modifier = Modifier.fillMaxWidth(),
                        height = 38.dp
                    )

                    val today = remember { LocalDate.now() }
                    val todayEpoch = remember(today) { today.toEpochDay() }
                    val weekStartEpoch = remember(today) { today.minusDays(today.dayOfWeek.value.toLong() - 1).toEpochDay() }
                    val weekEndEpoch = remember(today) { today.plusDays(7L - today.dayOfWeek.value.toLong()).toEpochDay() }
                    val monthStartEpoch = remember(today) { today.withDayOfMonth(1).toEpochDay() }
                    val monthEndEpoch = remember(today) { today.withDayOfMonth(today.lengthOfMonth()).toEpochDay() }

                    var customStartDate by remember { mutableStateOf(today.withDayOfMonth(1)) }
                    var customEndDate by remember { mutableStateOf(today) }
                    var showCustomStartPicker by remember { mutableStateOf(false) }
                    var showCustomEndPicker by remember { mutableStateOf(false) }

                    val customStartEpoch = remember(customStartDate) { customStartDate.toEpochDay() }
                    val customEndEpoch = remember(customEndDate) { customEndDate.toEpochDay() }
                    val customMinEpoch = minOf(customStartEpoch, customEndEpoch)
                    val customMaxEpoch = maxOf(customStartEpoch, customEndEpoch)

                    AnimatedVisibility(visible = selectedPeriodIndex == 3) {
                        val dateFormatter = remember { DateTimeFormatter.ofPattern("d MMM yyyy", Locale("ru")) }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    HapticHelper.lightClick(view, context)
                                    showCustomStartPicker = true
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Rounded.CalendarToday, contentDescription = null, tint = SageGreenPrimary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "С: ${customStartDate.format(dateFormatter)}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = CharcoalPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    HapticHelper.lightClick(view, context)
                                    showCustomEndPicker = true
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Rounded.CalendarToday, contentDescription = null, tint = SageGreenPrimary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "По: ${customEndDate.format(dateFormatter)}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = CharcoalPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    if (showCustomStartPicker) {
                        AppCalendarDialog(
                            initialDate = customStartDate,
                            onDateSelected = { customStartDate = it },
                            onDismiss = { showCustomStartPicker = false }
                        )
                    }

                    if (showCustomEndPicker) {
                        AppCalendarDialog(
                            initialDate = customEndDate,
                            onDateSelected = { customEndDate = it },
                            onDismiss = { showCustomEndPicker = false }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    val periodWalks = remember(allWalks, selectedPeriodIndex, customMinEpoch, customMaxEpoch) {
                        when (selectedPeriodIndex) {
                            0 -> allWalks.filter { it.dateEpochDay == todayEpoch }
                            1 -> allWalks.filter { it.dateEpochDay in weekStartEpoch..weekEndEpoch }
                            2 -> allWalks.filter { it.dateEpochDay in monthStartEpoch..monthEndEpoch }
                            3 -> allWalks.filter { it.dateEpochDay in customMinEpoch..customMaxEpoch }
                            else -> allWalks.filter { it.dateEpochDay == todayEpoch }
                        }
                    }

                    val completedWalks = remember(periodWalks) { periodWalks.filter { it.isCompleted } }
                    val totalEarned = remember(completedWalks) { completedWalks.sumOf { it.price } }
                    val plannedEarned = remember(periodWalks) { periodWalks.sumOf { it.price } }

                    // KPI Cards
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Earnings KPI
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(SoftMint.copy(alpha = 0.5f))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = "Заработано",
                                    fontSize = 11.sp,
                                    color = OliveGreySecondary,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "$totalEarned ₽",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = earnedGreen
                                )
                                if (plannedEarned > totalEarned) {
                                    Text(
                                        text = "из $plannedEarned ₽ план",
                                        fontSize = 10.sp,
                                        color = OliveGreySecondary
                                    )
                                }
                            }
                        }

                        // Walks Count KPI
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(SandBeige.copy(alpha = 0.6f))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = "Выгулов",
                                    fontSize = 11.sp,
                                    color = OliveGreySecondary,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${completedWalks.size} / ${periodWalks.size}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CharcoalPrimary
                                )
                                Text(
                                    text = "выполнено / всего",
                                    fontSize = 10.sp,
                                    color = OliveGreySecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Client Breakdown
                    Text(
                        text = "Клиенты за период:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CharcoalPrimary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    if (periodWalks.isEmpty()) {
                        Text(
                            text = "Нет записей о выгулах за этот период.",
                            fontSize = 12.sp,
                            color = OliveGreySecondary,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        val clientGroups = remember(periodWalks) {
                            periodWalks.groupBy { it.clientId }
                                .toList()
                                .sortedByDescending { (_, walks) -> walks.filter { it.isCompleted }.sumOf { it.price } }
                        }

                        val pageSize = 5
                        val totalPages = ((clientGroups.size + 4) / pageSize).coerceAtLeast(1)

                        LaunchedEffect(selectedPeriodIndex, customStartDate, customEndDate, clientGroups.size) {
                            currentPage = 0
                        }

                        val safePage = currentPage.coerceIn(0, totalPages - 1)
                        val pagedClients = remember(clientGroups, safePage) {
                            clientGroups.drop(safePage * pageSize).take(pageSize)
                        }

                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            pagedClients.forEach { (clientId, walksForClient) ->
                                val client = clientsMap[clientId]
                                val clientCompleted = walksForClient.count { it.isCompleted }
                                val clientEarned = walksForClient.filter { it.isCompleted }.sumOf { it.price }
                                val clientPlanned = walksForClient.sumOf { it.price }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(SandBeige.copy(alpha = 0.35f))
                                        .padding(horizontal = 10.dp, vertical = 8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = client?.fullName ?: "Клиент #$clientId",
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 13.sp,
                                                color = CharcoalPrimary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "Выгулов: $clientCompleted из ${walksForClient.size}",
                                                fontSize = 11.sp,
                                                color = OliveGreySecondary
                                            )
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = "$clientEarned ₽",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = earnedGreen
                                            )
                                            if (clientPlanned > clientEarned) {
                                                Text(
                                                    text = "план $clientPlanned ₽",
                                                    fontSize = 10.sp,
                                                    color = OliveGreySecondary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (clientGroups.size > 5) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(SandBeige.copy(alpha = if (isDark) 0.45f else 0.35f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val canGoBack = currentPage > 0
                                IconButton(
                                    onClick = {
                                        if (canGoBack) {
                                            HapticHelper.lightClick(view, context)
                                            currentPage--
                                        }
                                    },
                                    enabled = canGoBack,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.ChevronLeft,
                                        contentDescription = "Предыдущая страница",
                                        tint = if (canGoBack) SageGreenPrimary else OliveGreySecondary.copy(alpha = 0.35f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Text(
                                    text = "Стр. ${currentPage + 1} из $totalPages",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = CharcoalPrimary
                                )

                                val canGoForward = currentPage < totalPages - 1
                                IconButton(
                                    onClick = {
                                        if (canGoForward) {
                                            HapticHelper.lightClick(view, context)
                                            currentPage++
                                        }
                                    },
                                    enabled = canGoForward,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.ChevronRight,
                                        contentDescription = "Следующая страница",
                                        tint = if (canGoForward) SageGreenPrimary else OliveGreySecondary.copy(alpha = 0.35f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Интерфейс (Вторая позиция: Тема оформления и Режим отображения)
            GlassBox(
                modifier = Modifier.fillMaxWidth(),
                elevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Palette, contentDescription = null, tint = SageGreenPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Интерфейс",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = CharcoalPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Настройка темы оформления и режима отображения карты.",
                        fontSize = 12.sp,
                        color = OliveGreySecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Тема оформления",
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        color = CharcoalPrimary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    AnimatedSegmentedControl(
                        items = listOf(ThemeMode.SYSTEM, ThemeMode.LIGHT, ThemeMode.DARK),
                        selectedIndex = when (themeMode) {
                            ThemeMode.SYSTEM -> 0
                            ThemeMode.LIGHT -> 1
                            ThemeMode.DARK -> 2
                        },
                        onItemSelected = { idx ->
                            HapticHelper.lightClick(view, context)
                            val newMode = when (idx) {
                                0 -> ThemeMode.SYSTEM
                                1 -> ThemeMode.LIGHT
                                else -> ThemeMode.DARK
                            }
                            viewModel.setThemeMode(newMode)
                        },
                        itemLabel = {
                            when (it) {
                                ThemeMode.SYSTEM -> "Как в системе"
                                ThemeMode.LIGHT -> "Светлая"
                                ThemeMode.DARK -> "Тёмная"
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        height = 38.dp
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = dividerColor, thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Режим отображения",
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        color = CharcoalPrimary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "В режиме «Только план» приложение работает быстрее без встроенной карты. Маршруты строятся прямо в приложении Яндекс.Карт.",
                        fontSize = 12.sp,
                        color = OliveGreySecondary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    AnimatedSegmentedControl(
                        items = listOf("Только план", "Карта и план"),
                        selectedIndex = if (!isMapEnabled) 0 else 1,
                        onItemSelected = { idx ->
                            HapticHelper.lightClick(view, context)
                            viewModel.setMapEnabled(idx == 1)
                        },
                        itemLabel = { it },
                        modifier = Modifier.fillMaxWidth(),
                        height = 38.dp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Уведомления и отклик (Третья карточка)
            GlassBox(
                modifier = Modifier.fillMaxWidth(),
                elevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Notifications,
                            contentDescription = null,
                            tint = SageGreenPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Уведомления и отклик",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = CharcoalPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Напоминания о предстоящих мероприятиях и тактильная вибрация.",
                        fontSize = 12.sp,
                        color = OliveGreySecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Switch 1: Notifications
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Уведомления о записях",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = CharcoalPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Локальные Push-напоминания до начала мероприятий",
                                fontSize = 11.sp,
                                color = OliveGreySecondary
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Switch(
                            checked = isNotificationsEnabled,
                            onCheckedChange = { enabled ->
                                if (enabled) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        val granted = ContextCompat.checkSelfPermission(
                                            context,
                                            Manifest.permission.POST_NOTIFICATIONS
                                        ) == PackageManager.PERMISSION_GRANTED
                                        if (!granted) {
                                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                            return@Switch
                                        }
                                    }
                                    HapticHelper.lightClick(view, context)
                                }
                                viewModel.setNotificationsEnabled(enabled)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = SageGreenPrimary,
                                uncheckedThumbColor = if (isDark) Color(0xFFBAC4BE) else Color.White,
                                uncheckedTrackColor = if (isDark) Color(0xFF28352C) else Color(0xFFD6DFD9),
                                uncheckedBorderColor = Color.Transparent
                            )
                        )
                    }

                    // Lead time selector (visible when notifications are enabled)
                    AnimatedVisibility(visible = isNotificationsEnabled) {
                        Column(modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) {
                            Text(
                                text = "Напоминать за:",
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp,
                                color = CharcoalPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            val leadTimeOptions = remember { listOf(15, 30, 60, 120) }
                            val selectedLeadIndex = leadTimeOptions.indexOf(notificationLeadTimeMinutes).coerceAtLeast(0)

                            AnimatedSegmentedControl(
                                items = leadTimeOptions,
                                selectedIndex = selectedLeadIndex,
                                onItemSelected = { idx ->
                                    HapticHelper.tick(view, context)
                                    viewModel.setNotificationLeadTimeMinutes(leadTimeOptions[idx])
                                },
                                itemLabel = { minutes ->
                                    when (minutes) {
                                        15 -> "15 мин"
                                        30 -> "30 мин"
                                        60 -> "1 час"
                                        120 -> "2 часа"
                                        else -> "$minutes мин"
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                height = 36.dp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = dividerColor, thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Switch 2: Haptic Feedback
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.Vibration,
                                    contentDescription = null,
                                    tint = SageGreenPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Тактильный виброотклик",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = CharcoalPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Мягкая вибрация при прокрутке времени и нажатиях",
                                fontSize = 11.sp,
                                color = OliveGreySecondary
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Switch(
                            checked = isHapticFeedbackEnabled,
                            onCheckedChange = { enabled ->
                                if (enabled) {
                                    HapticHelper.lightClick(view, context)
                                }
                                viewModel.setHapticFeedbackEnabled(enabled)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = SageGreenPrimary,
                                uncheckedThumbColor = if (isDark) Color(0xFFBAC4BE) else Color.White,
                                uncheckedTrackColor = if (isDark) Color(0xFF28352C) else Color(0xFFD6DFD9),
                                uncheckedBorderColor = Color.Transparent
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Быстрые цены (шаблоны стоимости) (Четвёртая карточка)
            GlassBox(
                modifier = Modifier.fillMaxWidth(),
                elevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Payments, contentDescription = null, tint = SageGreenPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Быстрые цены (шаблоны стоимости)",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = CharcoalPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Чипы для быстрого выбора стоимости в форме записи. Укажите суммы через запятую:",
                        fontSize = 12.sp,
                        color = OliveGreySecondary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    AppTextField(
                        value = pricePresetsInput,
                        onValueChange = { pricePresetsInput = it },
                        label = "Суммы в рублях (через запятую)",
                        placeholder = "500, 800, 1000, 1500",
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    val previewList = pricePresetsInput
                        .split(",")
                        .mapNotNull { it.trim().toIntOrNull() }
                        .filter { it > 0 }

                    if (previewList.isNotEmpty()) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text("Превью:", fontSize = 11.sp, color = OliveGreySecondary)
                            Spacer(modifier = Modifier.height(6.dp))
                            @OptIn(ExperimentalLayoutApi::class)
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                previewList.forEach { p ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(PriceBadgeBg)
                                            .padding(horizontal = 10.dp, vertical = 5.dp)
                                    ) {
                                        Text(
                                            text = "$p ₽",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PriceBadgeText,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = {
                                val def = PreferencesManager.DEFAULT_PRICE_PRESETS
                                pricePresetsInput = def.joinToString(", ")
                                viewModel.setPricePresets(def)
                            }
                        ) {
                            Text("Сбросить", color = OliveGreySecondary, fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                if (previewList.isNotEmpty()) {
                                    HapticHelper.success(view, context)
                                    viewModel.setPricePresets(previewList)
                                    Toast.makeText(context, "Шаблоны цен сохранены", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SageGreenPrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Сохранить цены", color = Color.White, fontSize = 13.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 5. Резервная копия базы (Telegram) (Пятая карточка)
            GlassBox(
                modifier = Modifier.fillMaxWidth(),
                elevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Send, contentDescription = null, tint = SageGreenPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Резервная копия базы (Telegram)",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = CharcoalPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Экспортируйте базу клиентов, питомцев и все запланированные выгулы в один клик прямо в «Избранное» (Saved Messages) Telegram.",
                        fontSize = 12.sp,
                        color = OliveGreySecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Export to Telegram Button
                    Button(
                        onClick = { viewModel.exportDatabaseToTelegram(context) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SageGreenPrimary,
                            contentColor = Color.White
                        ),
                        shape = CircleShape,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(Icons.Rounded.FileUpload, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Отправить бэкап в Избранное Telegram", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Import from JSON Button
                    Button(
                        onClick = {
                            openDocumentLauncher.launch(
                                arrayOf("application/json", "*/*")
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SoftMint,
                            contentColor = DeepForest
                        ),
                        shape = CircleShape,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(Icons.Rounded.FileDownload, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Восстановить базу из JSON файла", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 6. API-ключ Яндекс.Карт (JS API) (Шестая карточка)
            GlassBox(
                modifier = Modifier.fillMaxWidth(),
                elevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Key, contentDescription = null, tint = SageGreenPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "API-ключ Яндекс.Карт (JS API)",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = CharcoalPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Используется для встроенной интерактивной карты в приложении. Если поле пустое, карта работает в базовом режиме.",
                        fontSize = 12.sp,
                        color = OliveGreySecondary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    AppTextField(
                        value = apiKeyInput,
                        onValueChange = { apiKeyInput = it },
                        label = "API-ключ Яндекс.Карт",
                        placeholder = "Вставьте ключ из Кабинета разработчика",
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            HapticHelper.success(view, context)
                            viewModel.setYandexApiKey(apiKeyInput)
                            Toast.makeText(context, "API-ключ сохранён", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SageGreenPrimary),
                        shape = CircleShape,
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Icon(Icons.Rounded.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Сохранить ключ", color = Color.White, fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 7. О приложении (Седьмая карточка, в самом низу)
            GlassBox(
                modifier = Modifier.fillMaxWidth(),
                elevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Info, contentDescription = null, tint = SageGreenPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "О приложении",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = CharcoalPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "KinologWalker v${BuildConfig.VERSION_NAME}\n" +
                                "Профессиональное приложение для кинологов и выгульщиков.\n" +
                                "Локальная база данных Room • Интеграция Яндекс.Карт\n" +
                                "Локальные Push-напоминания и тактильный отклик.",
                        fontSize = 12.sp,
                        color = OliveGreySecondary,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }

        // Import Backup Confirmation Dialog
        if (pendingImportUri != null) {
            val uri = pendingImportUri!!
            AlertDialog(
                onDismissRequest = { pendingImportUri = null },
                title = { Text("Восстановление из копии", color = CharcoalPrimary) },
                text = { Text("Текущие данные будут заменены данными из резервной копии. Продолжить?") },
                confirmButton = {
                    Button(
                        onClick = {
                            val toImport = uri
                            pendingImportUri = null
                            viewModel.importDatabaseFromJson(context, toImport)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SageGreenPrimary,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Продолжить")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { pendingImportUri = null }) {
                        Text("Отмена", color = CharcoalPrimary)
                    }
                }
            )
        }
    }
}
