package com.kinolog.walker.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Chat
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Directions
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Route
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.kinolog.walker.model.ClientEntity
import com.kinolog.walker.model.PetItem
import com.kinolog.walker.model.WalkCategory
import com.kinolog.walker.model.WalkEventEntity
import com.kinolog.walker.ui.MainViewModel
import com.kinolog.walker.ui.components.GlassBox
import com.kinolog.walker.ui.components.PetTag
import com.kinolog.walker.ui.components.TransportSlider
import com.kinolog.walker.ui.components.YandexMapView
import com.kinolog.walker.ui.theme.CanvasAlabaster
import com.kinolog.walker.ui.theme.CharcoalPrimary
import com.kinolog.walker.ui.theme.DeepForest
import com.kinolog.walker.ui.theme.MintLight
import com.kinolog.walker.ui.theme.OliveGreySecondary
import com.kinolog.walker.ui.theme.SageGreenPrimary
import com.kinolog.walker.ui.theme.SandBeige
import com.kinolog.walker.ui.theme.SoftMint
import com.kinolog.walker.util.IntentHelper
import kotlinx.serialization.json.Json
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ScheduleMapScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val selectedDate by viewModel.selectedDate.collectAsState()
    val routingMode by viewModel.routingMode.collectAsState()
    val apiKey by viewModel.yandexApiKey.collectAsState()
    val walks by viewModel.walksForSelectedDate.collectAsState()
    val clientsMap by viewModel.clientsMap.collectAsState()
    val mapMarkers by viewModel.mapMarkerPoints.collectAsState()
    val clients by viewModel.clients.collectAsState()

    var showAddWalkDialog by remember { mutableStateOf(false) }
    var walkToDelete by remember { mutableStateOf<WalkEventEntity?>(null) }

    val json = remember { Json { ignoreUnknownKeys = true } }

    val dateFormatter = remember {
        DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale("ru"))
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Top Floating Transport Slider & Date Controls
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                TransportSlider(
                    currentMode = routingMode,
                    onModeChanged = { viewModel.setRoutingMode(it) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Date Selector Pill
                GlassBox(
                    modifier = Modifier.fillMaxWidth(),
                    shape = CircleShape,
                    elevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { viewModel.setSelectedDate(selectedDate.minusDays(1)) }) {
                            Icon(Icons.Rounded.ChevronLeft, contentDescription = "Предыдущий день", tint = SageGreenPrimary)
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable {
                                    DatePickerDialog(
                                        context,
                                        { _, y, m, d ->
                                            viewModel.setSelectedDate(LocalDate.of(y, m + 1, d))
                                        },
                                        selectedDate.year,
                                        selectedDate.monthValue - 1,
                                        selectedDate.dayOfMonth
                                    ).show()
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Rounded.CalendarMonth, contentDescription = null, tint = SageGreenPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = selectedDate.format(dateFormatter).replaceFirstChar { it.uppercase() },
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = CharcoalPrimary
                            )
                        }

                        IconButton(onClick = { viewModel.setSelectedDate(selectedDate.plusDays(1)) }) {
                            Icon(Icons.Rounded.ChevronRight, contentDescription = "Следующий день", tint = SageGreenPrimary)
                        }
                    }
                }
            }

            // Embedded Yandex Map Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(24.dp))
            ) {
                YandexMapView(
                    apiKey = apiKey,
                    points = mapMarkers,
                    routingMode = routingMode
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Daily Schedule List
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "План на день (${walks.size})",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = CharcoalPrimary
                )

                if (walks.isNotEmpty()) {
                    Text(
                        text = "Выполнено: ${walks.count { it.isCompleted }}/${walks.size}",
                        fontSize = 12.sp,
                        color = OliveGreySecondary
                    )
                }
            }

            if (walks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
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
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(walks.sortedBy { it.startTimeMinutes }, key = { it.id }) { walk ->
                        val client = clientsMap[walk.clientId]
                        val pets = remember(client?.petsJson) {
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
                        val timeFormatted = String.format("%02d:%02d – %02d:%02d (%d мин)", startHours, startMins, endHours, endMins, walk.durationMinutes)

                        GlassBox(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = 3.dp
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp)
                            ) {
                                // Top row: Category Badge, Time, Checkbox
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        // Category Pill
                                        Box(
                                            modifier = Modifier
                                                .clip(CircleShape)
                                                .background(walk.category.composeColor.copy(alpha = 0.18f))
                                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = walk.category.title,
                                                color = walk.category.composeColor,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Text(
                                            text = timeFormatted,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = CharcoalPrimary
                                        )
                                    }

                                    // Checkbox / Completion toggle
                                    IconButton(
                                        onClick = { viewModel.toggleWalkCompletion(walk) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (walk.isCompleted) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                                            contentDescription = "Статус",
                                            tint = if (walk.isCompleted) SageGreenPrimary else OliveGreySecondary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Client name & phone
                                if (client != null) {
                                    Text(
                                        text = client.fullName,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp,
                                        color = CharcoalPrimary
                                    )

                                    if (pets.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        FlowRow(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            pets.forEach { pet ->
                                                PetTag(pet = pet)
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Address
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Rounded.LocationOn,
                                            contentDescription = "Адрес",
                                            tint = SageGreenPrimary,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        val fullAddr = buildString {
                                            append(client.addressStreet)
                                            if (client.entrance.isNotBlank()) append(", под. ${client.entrance}")
                                            if (client.floor.isNotBlank()) append(", эт. ${client.floor}")
                                            if (client.apartment.isNotBlank()) append(", кв. ${client.apartment}")
                                        }
                                        Text(text = fullAddr, fontSize = 12.sp, color = CharcoalPrimary)
                                    }

                                    if (walk.walkComment.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Заметка к выгулу: ${walk.walkComment}",
                                            fontSize = 12.sp,
                                            color = OliveGreySecondary
                                        )
                                    }

                                    // Action buttons row
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        // Route to this walk
                                        Button(
                                            onClick = { viewModel.routeSingleWalk(context, walk) },
                                            colors = ButtonDefaults.buttonColors(containerColor = SoftMint, contentColor = DeepForest),
                                            shape = CircleShape,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Rounded.Directions, contentDescription = null, modifier = Modifier.size(15.dp))
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text("Маршрут", fontSize = 11.sp)
                                        }

                                        // Call
                                        Button(
                                            onClick = { IntentHelper.dialPhone(context, client.phone) },
                                            colors = ButtonDefaults.buttonColors(containerColor = SandBeige, contentColor = CharcoalPrimary),
                                            shape = CircleShape,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Rounded.Call, contentDescription = null, modifier = Modifier.size(15.dp))
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text("Звонок", fontSize = 11.sp)
                                        }

                                        // Google Calendar
                                        Button(
                                            onClick = { IntentHelper.openGoogleCalendar(context, client, walk) },
                                            colors = ButtonDefaults.buttonColors(containerColor = MintLight, contentColor = DeepForest),
                                            shape = CircleShape,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Rounded.CalendarMonth, contentDescription = null, modifier = Modifier.size(15.dp))
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text("Календарь", fontSize = 11.sp)
                                        }

                                        // Delete
                                        IconButton(
                                            onClick = { walkToDelete = walk },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(Icons.Rounded.Delete, contentDescription = "Удалить", tint = Color(0xFFD9534F), modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(120.dp))
                    }
                }
            }
        }

        // Floating Main Route CTA Button & FAB
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 85.dp, start = 16.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (walks.isNotEmpty()) {
                Button(
                    onClick = { viewModel.routeDayWalks(context) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SageGreenPrimary,
                        contentColor = Color.White
                    ),
                    shape = CircleShape,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(Icons.Rounded.Route, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Построить маршрут на день (${walks.size} точек)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // FAB to schedule a new walk
        FloatingActionButton(
            onClick = { showAddWalkDialog = true },
            containerColor = SageGreenPrimary,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 145.dp)
        ) {
            Icon(Icons.Rounded.Add, contentDescription = "Запланировать выгул")
        }
    }

    // Delete Walk Dialog (handles recurring series)
    if (walkToDelete != null) {
        val walk = walkToDelete!!
        val isRecurring = !walk.seriesId.isNullOrBlank()

        AlertDialog(
            onDismissRequest = { walkToDelete = null },
            title = { Text(if (isRecurring) "Удалить регулярный выгул?" else "Удалить выгул?") },
            text = {
                Text(
                    if (isRecurring)
                        "Этот выгул является частью повторяющейся серии. Удалить только этот выгул или всю будущую серию?"
                    else
                        "Вы действительно хотите удалить этот запланированный выгул?"
                )
            },
            confirmButton = {
                if (isRecurring) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.deleteWalk(walk, deleteEntireSeries = false)
                                walkToDelete = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SandBeige, contentColor = CharcoalPrimary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Только этот выгул")
                        }
                        Button(
                            onClick = {
                                viewModel.deleteWalk(walk, deleteEntireSeries = true)
                                walkToDelete = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD9534F), contentColor = Color.White),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Всю серию расписания")
                        }
                    }
                } else {
                    Button(
                        onClick = {
                            viewModel.deleteWalk(walk, deleteEntireSeries = false)
                            walkToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD9534F), contentColor = Color.White)
                    ) {
                        Text("Удалить")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { walkToDelete = null }) {
                    Text("Отмена", color = CharcoalPrimary)
                }
            }
        )
    }

    // Add Walk Dialog
    if (showAddWalkDialog) {
        AddWalkDialog(
            clients = clients,
            initialDate = selectedDate,
            onDismiss = { showAddWalkDialog = false },
            onSave = { clientId, category, date, startTime, duration, comment, isRecurring, days, endDate ->
                viewModel.saveWalk(
                    clientId = clientId,
                    category = category,
                    date = date,
                    startTimeMinutes = startTime,
                    durationMinutes = duration,
                    walkComment = comment,
                    isRecurring = isRecurring,
                    selectedDays = days,
                    recurrenceEndDate = endDate
                ) {
                    showAddWalkDialog = false
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWalkDialog(
    clients: List<ClientEntity>,
    initialDate: LocalDate,
    onDismiss: () -> Unit,
    onSave: (
        clientId: Long,
        category: WalkCategory,
        date: LocalDate,
        startTimeMinutes: Int,
        durationMinutes: Int,
        comment: String,
        isRecurring: Boolean,
        selectedDays: Set<DayOfWeek>,
        recurrenceEndDate: LocalDate
    ) -> Unit
) {
    val context = LocalContext.current
    val json = remember { Json { ignoreUnknownKeys = true } }

    var selectedClientId by remember { mutableStateOf(clients.firstOrNull()?.id ?: 0L) }
    var selectedCategory by remember { mutableStateOf(WalkCategory.REGULAR_WALK) }
    var walkDate by remember { mutableStateOf(initialDate) }
    var startTimeMinutes by remember { mutableIntStateOf(19 * 60) } // Default 19:00
    var durationMinutes by remember { mutableIntStateOf(45) }
    var walkComment by remember { mutableStateOf("") }

    // Recurrence Engine
    var isRecurring by remember { mutableStateOf(false) }
    var selectedDays by remember {
        mutableStateOf(setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY))
    }
    var recurrenceEndDate by remember { mutableStateOf(initialDate.plusMonths(1)) }

    var clientMenuExpanded by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 24.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CanvasAlabaster)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Запланировать выгул",
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

                    OutlinedTextField(
                        value = "${currentClient.fullName} (${if (petsText.isNotBlank()) petsText else "Без питомцев"})",
                        onValueChange = {},
                        readOnly = true,
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

                Spacer(modifier = Modifier.height(14.dp))

                // Walk Category Selector
                Text("Категория выгула:", fontSize = 13.sp, fontWeight = FontWeight.Medium)
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
                                    .clickable { selectedCategory = cat }
                                    .padding(vertical = 8.dp, horizontal = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = cat.title,
                                    color = if (isSelected) Color.White else CharcoalPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Time & Duration
                val hours = startTimeMinutes / 60
                val mins = startTimeMinutes % 60
                val timeString = String.format("%02d:%02d", hours, mins)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Start Time Button
                    OutlinedTextField(
                        value = timeString,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Время начала") },
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                TimePickerDialog(
                                    context,
                                    { _, h, m -> startTimeMinutes = h * 60 + m },
                                    hours,
                                    mins,
                                    true
                                ).show()
                            }
                    )

                    // Duration Selector (30, 45, 60, 90, 120)
                    OutlinedTextField(
                        value = "$durationMinutes мин",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Длительность") },
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                val durations = listOf(30, 45, 60, 90, 120)
                                val nextIdx = (durations.indexOf(durationMinutes) + 1) % durations.size
                                durationMinutes = durations[nextIdx]
                            }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Walk Comment
                OutlinedTextField(
                    value = walkComment,
                    onValueChange = { walkComment = it },
                    label = { Text("Заметка к выгулу (что отработать, самочувствие)") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Recurrence Engine
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Регулярный график:", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(SandBeige)
                            .padding(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (!isRecurring) SageGreenPrimary else Color.Transparent)
                                .clickable { isRecurring = false }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Разовый", color = if (!isRecurring) Color.White else CharcoalPrimary, fontSize = 11.sp)
                        }
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (isRecurring) SageGreenPrimary else Color.Transparent)
                                .clickable { isRecurring = true }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Серия", color = if (isRecurring) Color.White else CharcoalPrimary, fontSize = 11.sp)
                        }
                    }
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

                        // End Date Picker
                        val endFormatted = recurrenceEndDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("ru")))
                        OutlinedTextField(
                            value = endFormatted,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Повторять до даты") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    DatePickerDialog(
                                        context,
                                        { _, y, m, d ->
                                            recurrenceEndDate = LocalDate.of(y, m + 1, d)
                                        },
                                        recurrenceEndDate.year,
                                        recurrenceEndDate.monthValue - 1,
                                        recurrenceEndDate.dayOfMonth
                                    ).show()
                                }
                        )
                    }
                }

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
                            onSave(
                                selectedClientId,
                                selectedCategory,
                                walkDate,
                                startTimeMinutes,
                                durationMinutes,
                                walkComment,
                                isRecurring,
                                selectedDays,
                                recurrenceEndDate
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SageGreenPrimary),
                        shape = CircleShape
                    ) {
                        Text("Сохранить", color = Color.White)
                    }
                }
            }
        }
    }
}
