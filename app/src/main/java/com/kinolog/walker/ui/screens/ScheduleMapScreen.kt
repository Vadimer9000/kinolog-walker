package com.kinolog.walker.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Directions
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Route
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.ViewAgenda
import androidx.compose.material.icons.rounded.ViewHeadline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import com.kinolog.walker.util.PreferencesManager
import kotlinx.serialization.json.Json
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
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

    var showWalkFormDialog by remember { mutableStateOf(false) }
    var walkToEdit by remember { mutableStateOf<WalkEventEntity?>(null) }
    var walkToDelete by remember { mutableStateOf<WalkEventEntity?>(null) }

    val prefs = remember { PreferencesManager(context) }
    var isCompactView by remember { mutableStateOf(prefs.isCompactView) }

    val json = remember { Json { ignoreUnknownKeys = true } }

    val dateFormatter = remember {
        DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale("ru"))
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val peekHeight = (maxHeight * 0.40f).coerceIn(240.dp, 360.dp)
        val scaffoldState = rememberBottomSheetScaffoldState(
            bottomSheetState = rememberStandardBottomSheetState(
                initialValue = SheetValue.PartiallyExpanded,
                skipHiddenState = true
            )
        )

        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            sheetPeekHeight = peekHeight,
            sheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            sheetContainerColor = CanvasAlabaster.copy(alpha = 0.97f),
            sheetShadowElevation = 16.dp,
            sheetDragHandle = {
                BottomSheetDefaults.DragHandle(
                    color = SageGreenPrimary.copy(alpha = 0.4f),
                    width = 38.dp,
                    height = 4.dp
                )
            },
            containerColor = Color.Transparent,
            sheetContent = {
                // Header of BottomSheet
                val totalPlannedEarnings = walks.sumOf { it.price }
                val totalEarned = walks.filter { it.isCompleted }.sumOf { it.price }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
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
                                    color = Color(0xFF2E7D32)
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
                            Button(
                                onClick = { viewModel.routeDayWalks(context) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SageGreenPrimary,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Icon(Icons.Rounded.Route, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Маршрут ($pendingCount)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    softWrap = false
                                )
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
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
                        verticalArrangement = Arrangement.spacedBy(if (isCompactView) 6.dp else 10.dp)
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
                            val timeFormatted = String.format(
                                "%02d:%02d – %02d:%02d (%d мин)",
                                startHours, startMins, endHours, endMins, walk.durationMinutes
                            )

                            val isDone = walk.isCompleted

                            if (isCompactView) {
                                // Narrow Compact Walk Card (~56dp height)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .alpha(if (isDone) 0.65f else 1.0f)
                                ) {
                                    GlassBox(
                                        modifier = Modifier.fillMaxWidth(),
                                        elevation = if (isDone) 1.dp else 2.dp
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(if (isDone) Color(0xFFEAEFEB).copy(alpha = 0.6f) else Color.Transparent)
                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            // Line 1: Time, Price, Client, Pet, Checkbox
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(
                                                    modifier = Modifier.weight(1f),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Text(
                                                        text = String.format("%02d:%02d-%02d:%02d", startHours, startMins, endHours, endMins),
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isDone) OliveGreySecondary else CharcoalPrimary,
                                                        textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None
                                                    )

                                                    if (walk.price > 0) {
                                                        Box(
                                                            modifier = Modifier
                                                                .clip(CircleShape)
                                                                .background(Color(0xFFE8F5E9))
                                                                .padding(horizontal = 5.dp, vertical = 1.dp)
                                                        ) {
                                                            Text(
                                                                text = "${walk.price} ₽",
                                                                color = Color(0xFF2E7D32),
                                                                fontSize = 10.sp,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        }
                                                    }

                                                    if (client != null) {
                                                        Text(
                                                            text = client.fullName,
                                                            fontWeight = FontWeight.SemiBold,
                                                            fontSize = 13.sp,
                                                            color = if (isDone) OliveGreySecondary else CharcoalPrimary,
                                                            textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )

                                                        if (pets.isNotEmpty()) {
                                                            val petSummary = pets.joinToString(" ") { "${it.type.icon} ${it.name}" }
                                                            Text(
                                                                text = "($petSummary)",
                                                                fontSize = 11.sp,
                                                                color = OliveGreySecondary,
                                                                maxLines = 1,
                                                                overflow = TextOverflow.Ellipsis
                                                            )
                                                        }
                                                    }
                                                }

                                                IconButton(
                                                    onClick = { viewModel.toggleWalkCompletion(walk) },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = if (isDone) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                                                        contentDescription = "Статус",
                                                        tint = if (isDone) Color(0xFF2E7D32) else OliveGreySecondary,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(3.dp))

                                            // Line 2: Address (left) + micro action buttons (right)
                                            if (client != null) {
                                                val fullAddr = buildString {
                                                    append(client.addressStreet)
                                                    if (client.entrance.isNotBlank()) append(", под. ${client.entrance}")
                                                    if (client.floor.isNotBlank()) append(", эт. ${client.floor}")
                                                    if (client.apartment.isNotBlank()) append(", кв. ${client.apartment}")
                                                }

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(
                                                        modifier = Modifier.weight(1f),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Rounded.LocationOn,
                                                            contentDescription = "Адрес",
                                                            tint = if (isDone) OliveGreySecondary else SageGreenPrimary,
                                                            modifier = Modifier.size(12.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(3.dp))
                                                        Text(
                                                            text = fullAddr,
                                                            fontSize = 11.sp,
                                                            color = if (isDone) OliveGreySecondary else CharcoalPrimary,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    }

                                                    Spacer(modifier = Modifier.width(6.dp))

                                                    Row(
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        // Route
                                                        FilledTonalIconButton(
                                                            onClick = { viewModel.routeSingleWalk(context, walk) },
                                                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                                                containerColor = SoftMint,
                                                                contentColor = DeepForest
                                                            ),
                                                            shape = RoundedCornerShape(8.dp),
                                                            modifier = Modifier.size(28.dp)
                                                        ) {
                                                            Icon(Icons.Rounded.Directions, contentDescription = "Маршрут", modifier = Modifier.size(15.dp))
                                                        }

                                                        // Call
                                                        FilledTonalIconButton(
                                                            onClick = { IntentHelper.dialPhone(context, client.phone) },
                                                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                                                containerColor = SandBeige,
                                                                contentColor = CharcoalPrimary
                                                            ),
                                                            shape = RoundedCornerShape(8.dp),
                                                            modifier = Modifier.size(28.dp)
                                                        ) {
                                                            Icon(Icons.Rounded.Call, contentDescription = "Звонок", modifier = Modifier.size(14.dp))
                                                        }

                                                        // Messenger
                                                        FilledTonalIconButton(
                                                            onClick = {
                                                                IntentHelper.openMessenger(
                                                                    context,
                                                                    client.messengerType,
                                                                    client.messengerIdentifier.ifBlank { client.phone }
                                                                )
                                                            },
                                                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                                                containerColor = MintLight,
                                                                contentColor = DeepForest
                                                            ),
                                                            shape = RoundedCornerShape(8.dp),
                                                            modifier = Modifier.size(28.dp)
                                                        ) {
                                                            Icon(Icons.Rounded.Chat, contentDescription = "Мессенджер", modifier = Modifier.size(14.dp))
                                                        }

                                                        // Edit
                                                        FilledTonalIconButton(
                                                            onClick = {
                                                                walkToEdit = walk
                                                                showWalkFormDialog = true
                                                            },
                                                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                                                containerColor = SandBeige,
                                                                contentColor = CharcoalPrimary
                                                            ),
                                                            shape = RoundedCornerShape(8.dp),
                                                            modifier = Modifier.size(28.dp)
                                                        ) {
                                                            Icon(Icons.Rounded.Edit, contentDescription = "Редактировать", modifier = Modifier.size(14.dp))
                                                        }

                                                        // Delete
                                                        FilledTonalIconButton(
                                                            onClick = { walkToDelete = walk },
                                                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                                                containerColor = Color(0xFFFFEBEE),
                                                                contentColor = Color(0xFFD9534F)
                                                            ),
                                                            shape = RoundedCornerShape(8.dp),
                                                            modifier = Modifier.size(28.dp)
                                                        ) {
                                                            Icon(Icons.Rounded.Delete, contentDescription = "Удалить", modifier = Modifier.size(14.dp))
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                // Standard Walk Card with Completed Visual Styling
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .alpha(if (isDone) 0.65f else 1.0f)
                            ) {
                                GlassBox(
                                    modifier = Modifier.fillMaxWidth(),
                                    elevation = if (isDone) 1.dp else 3.dp
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(if (isDone) Color(0xFFEAEFEB).copy(alpha = 0.6f) else Color.Transparent)
                                            .padding(horizontal = 14.dp, vertical = 10.dp)
                                    ) {
                                        // Line 1: Time badge, category chip, price, and checkbox
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = timeFormatted,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isDone) OliveGreySecondary else CharcoalPrimary,
                                                    textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None
                                                )

                                                Box(
                                                    modifier = Modifier
                                                        .clip(CircleShape)
                                                        .background(
                                                            if (isDone) Color.LightGray.copy(alpha = 0.3f)
                                                            else walk.category.composeColor.copy(alpha = 0.16f)
                                                        )
                                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = if (isDone) "✓ Выполнен" else walk.category.title,
                                                        color = if (isDone) Color(0xFF2E7D32) else walk.category.composeColor,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        maxLines = 1
                                                    )
                                                }

                                                // Price Badge
                                                if (walk.price > 0) {
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(CircleShape)
                                                            .background(Color(0xFFE8F5E9))
                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    ) {
                                                        Text(
                                                            text = "${walk.price} ₽",
                                                            color = Color(0xFF2E7D32),
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }
                                            }

                                            IconButton(
                                                onClick = { viewModel.toggleWalkCompletion(walk) },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (isDone) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                                                    contentDescription = "Статус",
                                                    tint = if (isDone) Color(0xFF2E7D32) else OliveGreySecondary,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        // Line 2: Client name + pet chips in one line/flow
                                        if (client != null) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = client.fullName,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 14.sp,
                                                    color = if (isDone) OliveGreySecondary else CharcoalPrimary,
                                                    textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )

                                                if (pets.isNotEmpty()) {
                                                    FlowRow(
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                        verticalArrangement = Arrangement.Center
                                                    ) {
                                                        pets.forEach { pet ->
                                                            PetTag(pet = pet)
                                                        }
                                                    }
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(4.dp))

                                            // Line 3: Location pin & address
                                            val fullAddr = buildString {
                                                append(client.addressStreet)
                                                if (client.entrance.isNotBlank()) append(", под. ${client.entrance}")
                                                if (client.floor.isNotBlank()) append(", эт. ${client.floor}")
                                                if (client.apartment.isNotBlank()) append(", кв. ${client.apartment}")
                                            }
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.LocationOn,
                                                    contentDescription = "Адрес",
                                                    tint = if (isDone) OliveGreySecondary else SageGreenPrimary,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = fullAddr,
                                                    fontSize = 12.sp,
                                                    color = if (isDone) OliveGreySecondary else CharcoalPrimary,
                                                    maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }

                                            if (walk.walkComment.isNotBlank()) {
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = "Заметка: ${walk.walkComment}",
                                                    fontSize = 11.sp,
                                                    color = OliveGreySecondary,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(8.dp))

                                            // Line 4: Action buttons row
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                // Route to single walk
                                                Button(
                                                    onClick = { viewModel.routeSingleWalk(context, walk) },
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = SoftMint,
                                                        contentColor = DeepForest
                                                    ),
                                                    shape = RoundedCornerShape(12.dp),
                                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                                    modifier = Modifier.height(36.dp)
                                                ) {
                                                    Icon(Icons.Rounded.Directions, contentDescription = null, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = "Маршрут",
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        maxLines = 1,
                                                        softWrap = false
                                                    )
                                                }

                                                // Call
                                                FilledTonalIconButton(
                                                    onClick = { IntentHelper.dialPhone(context, client.phone) },
                                                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                                                        containerColor = SandBeige,
                                                        contentColor = CharcoalPrimary
                                                    ),
                                                    shape = RoundedCornerShape(12.dp),
                                                    modifier = Modifier.size(36.dp)
                                                ) {
                                                    Icon(Icons.Rounded.Call, contentDescription = "Звонок", modifier = Modifier.size(17.dp))
                                                }

                                                // Messenger
                                                FilledTonalIconButton(
                                                    onClick = {
                                                        IntentHelper.openMessenger(
                                                            context,
                                                            client.messengerType,
                                                            client.messengerIdentifier.ifBlank { client.phone }
                                                        )
                                                    },
                                                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                                                        containerColor = MintLight,
                                                        contentColor = DeepForest
                                                    ),
                                                    shape = RoundedCornerShape(12.dp),
                                                    modifier = Modifier.size(36.dp)
                                                ) {
                                                    Icon(Icons.Rounded.Chat, contentDescription = "Мессенджер", modifier = Modifier.size(17.dp))
                                                }

                                                // Calendar
                                                FilledTonalIconButton(
                                                    onClick = { IntentHelper.openGoogleCalendar(context, client, walk) },
                                                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                                                        containerColor = SandBeige,
                                                        contentColor = CharcoalPrimary
                                                    ),
                                                    shape = RoundedCornerShape(12.dp),
                                                    modifier = Modifier.size(36.dp)
                                                ) {
                                                    Icon(Icons.Rounded.CalendarMonth, contentDescription = "Календарь", modifier = Modifier.size(17.dp))
                                                }

                                                // Edit Button
                                                FilledTonalIconButton(
                                                    onClick = {
                                                        walkToEdit = walk
                                                        showWalkFormDialog = true
                                                    },
                                                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                                                        containerColor = SandBeige,
                                                        contentColor = CharcoalPrimary
                                                    ),
                                                    shape = RoundedCornerShape(12.dp),
                                                    modifier = Modifier.size(36.dp)
                                                ) {
                                                    Icon(Icons.Rounded.Edit, contentDescription = "Редактировать", modifier = Modifier.size(17.dp))
                                                }

                                                Spacer(modifier = Modifier.weight(1f))

                                                // Delete
                                                FilledTonalIconButton(
                                                    onClick = { walkToDelete = walk },
                                                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                                                        containerColor = Color(0xFFFFEBEE),
                                                        contentColor = Color(0xFFD9534F)
                                                    ),
                                                    shape = RoundedCornerShape(12.dp),
                                                    modifier = Modifier.size(36.dp)
                                                ) {
                                                    Icon(Icons.Rounded.Delete, contentDescription = "Удалить", modifier = Modifier.size(17.dp))
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
        ) { innerPadding ->
            // Background: 100% Fullscreen Map + Top Floating Controls
            Box(modifier = Modifier.fillMaxSize()) {
                YandexMapView(
                    apiKey = apiKey,
                    points = mapMarkers,
                    routingMode = routingMode,
                    modifier = Modifier.fillMaxSize()
                )

                // Floating Controls overlay (statusBarsPadding avoids notification/clock bar)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                ) {
                    // Date Selector Pill
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

                    Spacer(modifier = Modifier.height(8.dp))

                    // Transport Slider right below Date Selector
                    TransportSlider(
                        currentMode = routingMode,
                        onModeChanged = { viewModel.setRoutingMode(it) }
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

    // Add or Edit Walk Dialog
    if (showWalkFormDialog) {
        WalkFormDialog(
            clients = clients,
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalkFormDialog(
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
        recurrenceEndDate: LocalDate,
        updateEntireSeries: Boolean
    ) -> Unit
) {
    val context = LocalContext.current
    val json = remember { Json { ignoreUnknownKeys = true } }

    var selectedClientId by remember {
        mutableStateOf(walkToEdit?.clientId ?: clients.firstOrNull()?.id ?: 0L)
    }
    var selectedCategory by remember {
        mutableStateOf(walkToEdit?.category ?: WalkCategory.REGULAR_WALK)
    }
    var walkDate by remember {
        mutableStateOf(
            if (walkToEdit != null) LocalDate.ofEpochDay(walkToEdit.dateEpochDay) else initialDate
        )
    }
    var startTimeMinutes by remember {
        mutableIntStateOf(walkToEdit?.startTimeMinutes ?: (19 * 60))
    }
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
        mutableStateOf(walkDate.plusMonths(1))
    }

    var clientMenuExpanded by remember { mutableStateOf(false) }
    var showSeriesEditConfirmDialog by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 20.dp)
                .imePadding(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CanvasAlabaster)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                Text(
                    text = if (walkToEdit != null) "Редактировать выгул" else "Запланировать выгул",
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

                Spacer(modifier = Modifier.height(12.dp))

                // Walk Date Selector with overlay for 100% reliable tap
                Text("Дата выгула:", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(4.dp))
                val dateFormatted = walkDate.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale("ru")))
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = dateFormatted.replaceFirstChar { it.uppercase() },
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            Icon(Icons.Rounded.CalendarToday, contentDescription = "Выбрать дату", tint = SageGreenPrimary)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable {
                                DatePickerDialog(
                                    context,
                                    { _, y, m, d -> walkDate = LocalDate.of(y, m + 1, d) },
                                    walkDate.year,
                                    walkDate.monthValue - 1,
                                    walkDate.dayOfMonth
                                ).show()
                            }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

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
                        OutlinedTextField(
                            value = timeString,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Время начала") },
                            trailingIcon = {
                                Icon(Icons.Rounded.Schedule, contentDescription = "Выбрать время", tint = SageGreenPrimary)
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
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
                    }

                    // Duration Input Field (Editable + Quick Chips)
                    OutlinedTextField(
                        value = durationInput,
                        onValueChange = { input ->
                            val digits = input.filter { it.isDigit() }.take(3)
                            durationInput = digits
                            digits.toIntOrNull()?.let { durationMinutes = it }
                        },
                        label = { Text("Длит. (мин)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Quick duration chips
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(15, 30, 45, 60, 90).forEach { minsPreset ->
                        val isSel = durationMinutes == minsPreset
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) SageGreenPrimary else SandBeige)
                                .clickable {
                                    durationMinutes = minsPreset
                                    durationInput = minsPreset.toString()
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "$minsPreset мин",
                                fontSize = 11.sp,
                                color = if (isSel) Color.White else CharcoalPrimary,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Price Field with Quick Preset Chips
                Text("Стоимость выгула:", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = priceInput,
                    onValueChange = { input ->
                        val digits = input.filter { it.isDigit() }.take(6)
                        priceInput = digits
                        price = digits.toIntOrNull() ?: 0
                    },
                    label = { Text("Стоимость (руб.)") },
                    leadingIcon = { Icon(Icons.Rounded.Payments, contentDescription = null, tint = SageGreenPrimary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(300, 500, 800, 1000, 1500).forEach { presetPrice ->
                        val isSel = price == presetPrice
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) SageGreenPrimary else SandBeige)
                                .clickable {
                                    price = presetPrice
                                    priceInput = presetPrice.toString()
                                }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "$presetPrice ₽",
                                fontSize = 11.sp,
                                color = if (isSel) Color.White else CharcoalPrimary,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
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

                            // End Date Picker with clickable overlay
                            val endFormatted = recurrenceEndDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("ru")))
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = endFormatted,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Повторять до даты") },
                                    trailingIcon = {
                                        Icon(Icons.Rounded.CalendarMonth, contentDescription = "Выбрать дату", tint = SageGreenPrimary)
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
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
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Walk Comment (at the very end of the form)
                OutlinedTextField(
                    value = walkComment,
                    onValueChange = { walkComment = it },
                    label = { Text("Заметка к выгулу (что отработать, самочувствие)") },
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
                                showSeriesEditConfirmDialog = true
                            } else {
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
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SageGreenPrimary),
                        shape = CircleShape
                    ) {
                        Text(if (walkToEdit != null) "Сохранить" else "Запланировать", color = Color.White)
                    }
                }
            }
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
