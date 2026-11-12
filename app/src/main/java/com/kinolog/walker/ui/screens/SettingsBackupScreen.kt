package com.kinolog.walker.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kinolog.walker.ui.MainViewModel
import com.kinolog.walker.ui.components.AnimatedSegmentedControl
import com.kinolog.walker.ui.components.GlassBox
import com.kinolog.walker.ui.components.NavigationTab
import com.kinolog.walker.ui.theme.LocalIsDarkTheme
import com.kinolog.walker.ui.theme.CharcoalPrimary
import com.kinolog.walker.ui.theme.DeepForest
import com.kinolog.walker.ui.theme.OliveGreySecondary
import com.kinolog.walker.ui.theme.PriceBadgeBg
import com.kinolog.walker.ui.theme.PriceBadgeText
import com.kinolog.walker.ui.theme.SageGreenPrimary
import com.kinolog.walker.ui.theme.SandBeige
import com.kinolog.walker.ui.theme.SoftMint
import com.kinolog.walker.util.PreferencesManager
import com.kinolog.walker.util.ThemeMode
import java.time.LocalDate

@Composable
fun SettingsBackupScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDark = LocalIsDarkTheme.current
    val earnedGreen = if (isDark) Color(0xFF81C784) else Color(0xFF2E7D32)
    val currentApiKey by viewModel.yandexApiKey.collectAsState()
    var apiKeyInput by remember(currentApiKey) { mutableStateOf(currentApiKey) }

    val clients by viewModel.clients.collectAsState()
    val allWalks by viewModel.allWalks.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val isMapEnabled by viewModel.isMapEnabled.collectAsState()
    val currentPricePresets by viewModel.pricePresets.collectAsState()
    var pricePresetsInput by remember(currentPricePresets) {
        mutableStateOf(currentPricePresets.joinToString(", "))
    }

    // Import file launcher
    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.importDatabaseFromJson(context, uri)
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
                        text = "Управление темой, аналитикой, ключами карт и базой данных",
                        fontSize = 12.sp,
                        color = OliveGreySecondary
                    )
                }
            }

            // Section: Theme Mode (Soft Dark Theme)
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
                            text = "Тема оформления",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = CharcoalPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Мягкая палитра: автоматическая системная, светлая или ночная тёмная тема.",
                        fontSize = 12.sp,
                        color = OliveGreySecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    AnimatedSegmentedControl(
                        items = listOf(ThemeMode.SYSTEM, ThemeMode.LIGHT, ThemeMode.DARK),
                        selectedIndex = when (themeMode) {
                            ThemeMode.SYSTEM -> 0
                            ThemeMode.LIGHT -> 1
                            ThemeMode.DARK -> 2
                        },
                        onItemSelected = { idx ->
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
                        height = 40.dp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Section: Display Mode (Pure Plan vs Embedded Map)
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
                        Icon(Icons.Rounded.Map, contentDescription = null, tint = SageGreenPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Режим отображения",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = CharcoalPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "В режиме «Только план» приложение работает быстрее без встроенной карты. Маршруты строятся прямо в приложении Яндекс.Карт.",
                        fontSize = 12.sp,
                        color = OliveGreySecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    AnimatedSegmentedControl(
                        items = listOf("Только план", "Карта и план"),
                        selectedIndex = if (!isMapEnabled) 0 else 1,
                        onItemSelected = { idx ->
                            viewModel.setMapEnabled(idx == 1)
                        },
                        itemLabel = { it },
                        modifier = Modifier.fillMaxWidth(),
                        height = 40.dp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Section: Quick Price Presets
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

                    OutlinedTextField(
                        value = pricePresetsInput,
                        onValueChange = { pricePresetsInput = it },
                        label = { Text("Суммы в рублях (через запятую)") },
                        placeholder = { Text("500, 800, 1000, 1500") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val previewList = pricePresetsInput
                        .split(",")
                        .mapNotNull { it.trim().toIntOrNull() }
                        .filter { it > 0 }

                    if (previewList.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Превью:", fontSize = 11.sp, color = OliveGreySecondary)
                            previewList.forEach { p ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(PriceBadgeBg)
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "$p ₽",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PriceBadgeText
                                    )
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
                                    viewModel.setPricePresets(previewList)
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

            // Section: Analytics & Period Reporting
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
                    val periods = listOf("Сегодня", "Неделя", "Месяц", "Все время")

                    AnimatedSegmentedControl(
                        items = periods,
                        selectedIndex = selectedPeriodIndex,
                        onItemSelected = { selectedPeriodIndex = it },
                        itemLabel = { it },
                        modifier = Modifier.fillMaxWidth(),
                        height = 38.dp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    val today = remember { LocalDate.now() }
                    val todayEpoch = remember(today) { today.toEpochDay() }
                    val weekStartEpoch = remember(today) { today.minusDays(today.dayOfWeek.value.toLong() - 1).toEpochDay() }
                    val weekEndEpoch = remember(today) { today.plusDays(7L - today.dayOfWeek.value.toLong()).toEpochDay() }
                    val monthStartEpoch = remember(today) { today.withDayOfMonth(1).toEpochDay() }
                    val monthEndEpoch = remember(today) { today.withDayOfMonth(today.lengthOfMonth()).toEpochDay() }

                    val periodWalks = remember(allWalks, selectedPeriodIndex) {
                        when (selectedPeriodIndex) {
                            0 -> allWalks.filter { it.dateEpochDay == todayEpoch }
                            1 -> allWalks.filter { it.dateEpochDay in weekStartEpoch..weekEndEpoch }
                            2 -> allWalks.filter { it.dateEpochDay in monthStartEpoch..monthEndEpoch }
                            else -> allWalks
                        }
                    }

                    val completedWalks = remember(periodWalks) { periodWalks.filter { it.isCompleted } }
                    val totalEarned = remember(completedWalks) { completedWalks.sumOf { it.price } }
                    val plannedEarned = remember(periodWalks) { periodWalks.sumOf { it.price } }
                    val clientsMap = remember(clients) { clients.associateBy { it.id } }

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

                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            clientGroups.forEach { (clientId, walksForClient) ->
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
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Section 1: Yandex Maps JS API Key
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

                    OutlinedTextField(
                        value = apiKeyInput,
                        onValueChange = { apiKeyInput = it },
                        placeholder = { Text("Вставьте ключ из Кабинета разработчика", fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
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

            // Section 2: Telegram Backup Export / Import
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

            // Section 3: App info
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
                        Icon(Icons.Rounded.Shield, contentDescription = null, tint = SageGreenPrimary)
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
                        text = "Кинолог-Выгульщик v1.0.0\n" +
                                "Локальная база данных Room + Geocoder\n" +
                                "Маршрутизация Яндекс.Карты (Пешком / Транспорт)\n" +
                                "Безопасное хранилище и бэкапы без сторонних облаков",
                        fontSize = 12.sp,
                        color = OliveGreySecondary,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}
