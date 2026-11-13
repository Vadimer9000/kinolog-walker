package com.kinolog.walker.ui.screens

import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Chat
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.PersonOutline
import androidx.compose.material.icons.rounded.PersonSearch
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.kinolog.walker.ui.theme.LocalIsDarkTheme
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.kinolog.walker.model.ClientEntity
import com.kinolog.walker.model.MessengerType
import com.kinolog.walker.model.PetItem
import com.kinolog.walker.model.PetType
import com.kinolog.walker.ui.MainViewModel
import com.kinolog.walker.ui.components.AnimatedSegmentedControl
import com.kinolog.walker.ui.components.AppTextField
import com.kinolog.walker.ui.components.GlassBox
import com.kinolog.walker.ui.components.PetTag
import com.kinolog.walker.ui.theme.CanvasAlabaster
import com.kinolog.walker.ui.theme.CanvasBeige
import com.kinolog.walker.ui.theme.CharcoalPrimary
import com.kinolog.walker.ui.theme.DangerRedBg
import com.kinolog.walker.ui.theme.DangerRedText
import com.kinolog.walker.ui.theme.DeepForest
import com.kinolog.walker.ui.theme.DividerMuted
import com.kinolog.walker.ui.theme.MintLight
import com.kinolog.walker.ui.theme.OliveGreySecondary
import com.kinolog.walker.ui.theme.SageGreenPrimary
import com.kinolog.walker.ui.theme.SandBeige
import com.kinolog.walker.ui.theme.SoftMint
import com.kinolog.walker.util.HapticHelper
import com.kinolog.walker.util.IntentHelper
import kotlinx.serialization.json.Json

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ClientsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clients by viewModel.clients.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }
    var clientToEdit by remember { mutableStateOf<ClientEntity?>(null) }
    var clientToDelete by remember { mutableStateOf<ClientEntity?>(null) }

    val json = remember { Json { ignoreUnknownKeys = true } }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Screen Title
            Text(
                text = "Клиенты и Питомцы",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = CharcoalPrimary
            )
            Text(
                text = "Всего клиентов: ${clients.size}",
                fontSize = 13.sp,
                color = OliveGreySecondary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Search Bar
            AppTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                label = "Поиск клиентов и питомцев",
                placeholder = "Имя, кличка, адрес или телефон",
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = "Поиск",
                        tint = SageGreenPrimary
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Rounded.Clear, contentDescription = "Очистить", tint = OliveGreySecondary)
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Clients List
            if (clients.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isNotBlank()) "Ничего не найдено" else "Список клиентов пуст.\nНажмите '+' чтобы добавить.",
                        color = OliveGreySecondary,
                        fontSize = 15.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(clients, key = { it.id }) { client ->
                        val pets = remember(client.petsJson) {
                            try {
                                json.decodeFromString<List<PetItem>>(client.petsJson)
                            } catch (e: Exception) {
                                emptyList()
                            }
                        }

                        GlassBox(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = 3.dp
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = client.fullName,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 16.sp,
                                            color = CharcoalPrimary
                                        )
                                        Text(
                                            text = client.phone,
                                            fontSize = 13.sp,
                                            color = OliveGreySecondary
                                        )
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        FilledTonalIconButton(
                                            onClick = {
                                                clientToEdit = client
                                                showEditDialog = true
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.size(34.dp)
                                        ) {
                                            Icon(Icons.Rounded.Edit, contentDescription = "Редактировать", tint = SageGreenPrimary, modifier = Modifier.size(16.dp))
                                        }
                                        FilledTonalIconButton(
                                            onClick = { clientToDelete = client },
                                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                                containerColor = DangerRedBg,
                                                contentColor = DangerRedText
                                            ),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.size(34.dp)
                                        ) {
                                            Icon(Icons.Rounded.Delete, contentDescription = "Удалить", modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }

                                // Pets Tags
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

                                // Address
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Rounded.LocationOn,
                                        contentDescription = "Адрес",
                                        tint = SageGreenPrimary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    val fullAddress = buildString {
                                        append(client.addressStreet)
                                        if (client.entrance.isNotBlank()) append(", под. ${client.entrance}")
                                        if (client.floor.isNotBlank()) append(", эт. ${client.floor}")
                                        if (client.apartment.isNotBlank()) append(", кв. ${client.apartment}")
                                    }
                                    Text(
                                        text = fullAddress,
                                        fontSize = 12.sp,
                                        color = CharcoalPrimary,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                if (client.clientComment.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Заметка: ${client.clientComment}",
                                        fontSize = 12.sp,
                                        color = OliveGreySecondary,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                // Quick Action Buttons (Compact row)
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Call button
                                    Button(
                                        onClick = { IntentHelper.dialPhone(context, client.phone) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = SoftMint,
                                            contentColor = DeepForest
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(36.dp)
                                    ) {
                                        Icon(Icons.Rounded.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Звонок", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                                    }

                                    // Messenger button
                                    Button(
                                        onClick = {
                                            IntentHelper.openMessenger(
                                                context,
                                                client.messengerType,
                                                client.messengerIdentifier.ifBlank { client.phone }
                                            )
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = SandBeige,
                                            contentColor = CharcoalPrimary
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(36.dp)
                                    ) {
                                        Icon(Icons.Rounded.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(client.messengerType.title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                                    }
                                }

                                if (client.secondaryContactPhone.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(SandBeige.copy(alpha = 0.4f))
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.PersonOutline,
                                                contentDescription = null,
                                                tint = OliveGreySecondary,
                                                modifier = Modifier.size(15.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "${client.secondaryContactName.ifBlank { "Второй хозяин" }}: ${client.secondaryContactPhone}",
                                                fontSize = 12.sp,
                                                color = CharcoalPrimary,
                                                fontWeight = FontWeight.Medium,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        FilledTonalIconButton(
                                            onClick = { IntentHelper.dialPhone(context, client.secondaryContactPhone) },
                                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                                containerColor = SoftMint,
                                                contentColor = DeepForest
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Rounded.Call, contentDescription = "Позвонить", modifier = Modifier.size(14.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Floating Action Button to Add Client (lowered right above the Clients tab)
        FloatingActionButton(
            onClick = {
                clientToEdit = null
                showEditDialog = true
            },
            containerColor = SageGreenPrimary,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 16.dp)
        ) {
            Icon(Icons.Rounded.Add, contentDescription = "Добавить клиента")
        }
    }

    // Delete Client Confirmation Dialog
    if (clientToDelete != null) {
        val client = clientToDelete!!
        AlertDialog(
            onDismissRequest = { clientToDelete = null },
            title = { Text("Удалить клиента?") },
            text = { Text("Вы уверены, что хотите удалить клиента «${client.fullName}» и все связанные с ним данные?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteClient(client)
                        clientToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD9534F), contentColor = Color.White)
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(onClick = { clientToDelete = null }) {
                    Text("Отмена", color = CharcoalPrimary)
                }
            }
        )
    }

    // Add / Edit Client Dialog with Contacts Import
    if (showEditDialog) {
        ClientFormDialog(
            client = clientToEdit,
            viewModel = viewModel,
            onDismiss = {
                viewModel.clearAddressSuggestions()
                showEditDialog = false
            },
            onSave = { updatedClient, pets ->
                viewModel.clearAddressSuggestions()
                viewModel.saveClient(
                    id = updatedClient.id,
                    fullName = updatedClient.fullName,
                    phone = updatedClient.phone,
                    messengerType = updatedClient.messengerType,
                    messengerIdentifier = updatedClient.messengerIdentifier,
                    pets = pets,
                    addressStreet = updatedClient.addressStreet,
                    entrance = updatedClient.entrance,
                    floor = updatedClient.floor,
                    apartment = updatedClient.apartment,
                    clientComment = updatedClient.clientComment,
                    secondaryContactName = updatedClient.secondaryContactName,
                    secondaryContactPhone = updatedClient.secondaryContactPhone,
                    latitude = updatedClient.latitude,
                    longitude = updatedClient.longitude,
                    onDone = { showEditDialog = false }
                )
            }
        )
    }
}

fun extractRussianPhoneDigits(raw: String): String {
    val digits = raw.filter { it.isDigit() }
    return when {
        (digits.startsWith("7") || digits.startsWith("8")) && digits.length >= 11 -> digits.substring(1)
        else -> digits
    }.take(10)
}

fun formatRussianPhoneFromDigits(digits: String): String {
    if (digits.isEmpty()) return ""
    val d = digits.take(10)
    val sb = StringBuilder("+7 (")
    for (i in d.indices) {
        if (i == 3) sb.append(") ")
        if (i == 6) sb.append("-")
        if (i == 8) sb.append("-")
        sb.append(d[i])
    }
    return sb.toString()
}

fun formatRussianPhone(raw: String): String {
    val digits = extractRussianPhoneDigits(raw)
    return formatRussianPhoneFromDigits(digits)
}

class RussianPhoneVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text.filter { it.isDigit() }.take(10)
        val sb = StringBuilder()
        if (digits.isNotEmpty()) {
            sb.append("(")
            for (i in digits.indices) {
                if (i == 3) sb.append(") ")
                if (i == 6) sb.append("-")
                if (i == 8) sb.append("-")
                sb.append(digits[i])
            }
        }
        val transformed = AnnotatedString(sb.toString())

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (digits.isEmpty()) return 0
                val clamped = offset.coerceIn(0, digits.length)
                return when {
                    clamped <= 0 -> 1 // after '('
                    clamped <= 3 -> 1 + clamped
                    clamped <= 6 -> 1 + clamped + 2 // + ") "
                    clamped <= 8 -> 1 + clamped + 3 // + ") " + "-"
                    else -> 1 + clamped + 4         // + ") " + "-" + "-"
                }.coerceIn(0, transformed.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (digits.isEmpty()) return 0
                val orig = when {
                    offset <= 1 -> 0
                    offset <= 4 -> offset - 1
                    offset <= 6 -> 3
                    offset <= 9 -> offset - 3
                    offset <= 10 -> 6
                    offset <= 12 -> offset - 4
                    offset <= 13 -> 8
                    else -> offset - 5
                }
                return orig.coerceIn(0, digits.length)
            }
        }

        return TransformedText(transformed, offsetMapping)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ClientFormDialog(
    client: ClientEntity?,
    viewModel: MainViewModel,
    onDismiss: () -> Unit,
    onSave: (ClientEntity, List<PetItem>) -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current
    val json = remember { Json { ignoreUnknownKeys = true } }

    val addressSuggestions by viewModel.addressSuggestions.collectAsState()
    val isSearchingAddress by viewModel.isSearchingAddress.collectAsState()

    var selectedLat by remember { mutableStateOf(client?.latitude) }
    var selectedLon by remember { mutableStateOf(client?.longitude) }

    LaunchedEffect(Unit) {
        viewModel.clearAddressSuggestions()
    }

    var fullName by remember { mutableStateOf(client?.fullName ?: "") }
    var phoneDigits by remember {
        mutableStateOf(extractRussianPhoneDigits(client?.phone ?: ""))
    }
    var messengerType by remember { mutableStateOf(client?.messengerType ?: MessengerType.TELEGRAM) }
    var messengerIdentifier by remember { mutableStateOf(client?.messengerIdentifier ?: "") }

    var hasSecondaryContact by remember {
        mutableStateOf(
            !client?.secondaryContactName.isNullOrBlank() || !client?.secondaryContactPhone.isNullOrBlank()
        )
    }
    var secondaryContactName by remember { mutableStateOf(client?.secondaryContactName ?: "") }
    var secondaryContactPhoneDigits by remember {
        mutableStateOf(extractRussianPhoneDigits(client?.secondaryContactPhone ?: ""))
    }

    var addressStreet by remember {
        mutableStateOf(client?.addressStreet ?: "Санкт-Петербург, ")
    }
    var entrance by remember { mutableStateOf(client?.entrance ?: "") }
    var floor by remember { mutableStateOf(client?.floor ?: "") }
    var apartment by remember { mutableStateOf(client?.apartment ?: "") }
    var clientComment by remember { mutableStateOf(client?.clientComment ?: "") }

    val initialPets = remember {
        if (client != null && client.petsJson.isNotBlank()) {
            try {
                json.decodeFromString<List<PetItem>>(client.petsJson)
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            listOf(PetItem(name = "", type = PetType.DOG))
        }
    }

    val petList = remember { mutableStateListOf<PetItem>().apply { addAll(initialPets) } }

    // Contact Picker from phone contacts (extracts both Name and Phone)
    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val contactUri = result.data?.data ?: return@rememberLauncherForActivityResult
            try {
                context.contentResolver.query(
                    contactUri,
                    arrayOf(
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Phone.NUMBER
                    ),
                    null, null, null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                        if (nameIdx >= 0) {
                            cursor.getString(nameIdx)?.let { if (it.isNotBlank()) fullName = it }
                        }
                        val numIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                        if (numIdx >= 0) {
                            cursor.getString(numIdx)?.let { rawNum ->
                                if (rawNum.isNotBlank()) {
                                    phoneDigits = extractRussianPhoneDigits(rawNum)
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

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
                // Header with Title and "From Contacts" button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (client == null) "Новый клиент" else "Редактирование",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = CharcoalPrimary
                    )

                    Button(
                        onClick = {
                            val intent = android.content.Intent(
                                android.content.Intent.ACTION_PICK,
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI
                            )
                            contactPickerLauncher.launch(intent)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SoftMint,
                            contentColor = DeepForest
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Rounded.PersonSearch, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Из контактов", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Full Name
                AppTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = "ФИО клиента",
                    placeholder = "например: Иванов Иван",
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Phone with Russian Mask & Visual Transformation
                AppTextField(
                    value = phoneDigits,
                    onValueChange = { input ->
                        val clean = input.filter { it.isDigit() }
                        val normalized = if (phoneDigits.isEmpty() && (clean.startsWith("79") || clean.startsWith("89"))) {
                            clean.substring(1)
                        } else if (phoneDigits.isEmpty() && (clean == "7" || clean == "8")) {
                            ""
                        } else if (clean.length > 10) {
                            if ((clean.startsWith("7") || clean.startsWith("8")) && clean.length == 11) {
                                clean.substring(1)
                            } else {
                                clean.take(10)
                            }
                        } else {
                            clean
                        }
                        phoneDigits = normalized.take(10)
                    },
                    label = "Номер телефона",
                    placeholder = "(___) ___-__-__",
                    prefix = {
                        Text(
                            text = "+7 ",
                            fontWeight = FontWeight.SemiBold,
                            color = CharcoalPrimary
                        )
                    },
                    visualTransformation = RussianPhoneVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Messenger Type Selector
                Text("Основной мессенджер:", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MessengerType.entries.forEach { type ->
                        val isSelected = type == messengerType
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (isSelected) SageGreenPrimary else SandBeige)
                                .clickable {
                                    HapticHelper.tick(view, context)
                                    messengerType = type
                                }
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = type.title,
                                color = if (isSelected) Color.White else CharcoalPrimary,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Messenger ID
                AppTextField(
                    value = messengerIdentifier,
                    onValueChange = { messengerIdentifier = it },
                    label = "Логин / ID в ${messengerType.title}",
                    placeholder = "например: @username",
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Additional Contact (Second owner/contact)
                if (!hasSecondaryContact) {
                    OutlinedButton(
                        onClick = { hasSecondaryContact = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, SageGreenPrimary.copy(alpha = 0.6f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SageGreenPrimary)
                    ) {
                        Icon(Icons.Rounded.PersonAdd, contentDescription = null, modifier = Modifier.size(17.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("+ Доп. номер и имя (второй хозяин)", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = CanvasBeige.copy(alpha = 0.5f)),
                        border = BorderStroke(1.dp, DividerMuted)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Rounded.PersonOutline,
                                        contentDescription = null,
                                        tint = SageGreenPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Второй контакт (хозяин):",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = CharcoalPrimary
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        hasSecondaryContact = false
                                        secondaryContactName = ""
                                        secondaryContactPhoneDigits = ""
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Rounded.Close,
                                        contentDescription = "Удалить доп. контакт",
                                        tint = OliveGreySecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            AppTextField(
                                value = secondaryContactName,
                                onValueChange = { secondaryContactName = it },
                                label = "Имя второго контакта",
                                placeholder = "например: Муж, Жена, Догситтер",
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            AppTextField(
                                value = secondaryContactPhoneDigits,
                                onValueChange = { input ->
                                    val clean = input.filter { it.isDigit() }
                                    val normalized = if (secondaryContactPhoneDigits.isEmpty() && (clean.startsWith("79") || clean.startsWith("89"))) {
                                        clean.substring(1)
                                    } else if (secondaryContactPhoneDigits.isEmpty() && (clean == "7" || clean == "8")) {
                                        ""
                                    } else if (clean.length > 10) {
                                        if ((clean.startsWith("7") || clean.startsWith("8")) && clean.length == 11) {
                                            clean.substring(1)
                                        } else {
                                            clean.take(10)
                                        }
                                    } else {
                                        clean
                                    }
                                    secondaryContactPhoneDigits = normalized.take(10)
                                },
                                label = "Второй номер телефона",
                                placeholder = "(___) ___-__-__",
                                prefix = {
                                    Text(
                                        text = "+7 ",
                                        fontWeight = FontWeight.SemiBold,
                                        color = CharcoalPrimary
                                    )
                                },
                                visualTransformation = RussianPhoneVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Pet List Dynamic Builder
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Питомцы:", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    Button(
                        onClick = {
                            petList.add(PetItem(name = "", type = PetType.DOG))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SoftMint, contentColor = DeepForest),
                        shape = CircleShape
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Добавить", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                petList.forEachIndexed { index, pet ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MintLight),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Pet type selector [Собака | Кошка]
                                AnimatedSegmentedControl(
                                    items = PetType.entries,
                                    selectedIndex = if (pet.type == PetType.DOG) 0 else 1,
                                    onItemSelected = { selIndex ->
                                        petList[index] = pet.copy(type = if (selIndex == 0) PetType.DOG else PetType.CAT)
                                    },
                                    itemLabel = { it.title },
                                    modifier = Modifier.width(170.dp),
                                    height = 34.dp
                                )

                                if (petList.size > 1) {
                                    IconButton(onClick = { petList.removeAt(index) }) {
                                        Icon(Icons.Rounded.Delete, contentDescription = "Удалить", tint = Color(0xFFD9534F), modifier = Modifier.size(18.dp))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            AppTextField(
                                value = pet.name,
                                onValueChange = { petList[index] = pet.copy(name = it) },
                                label = "Кличка питомца",
                                placeholder = "например: Барсик, Джеки",
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            AppTextField(
                                value = pet.breedOrDetails,
                                onValueChange = { petList[index] = pet.copy(breedOrDetails = it) },
                                label = "Порода / особенности",
                                placeholder = "например: корги, пугливый",
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Address Block
                Text("Адрес выгула:", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))

                AppTextField(
                    value = addressStreet,
                    onValueChange = { newValue ->
                        addressStreet = newValue
                        selectedLat = null
                        selectedLon = null
                        viewModel.onAddressQueryChanged(newValue)
                    },
                    label = "Город, улица, дом",
                    placeholder = "например: Санкт-Петербург, Невский пр. 1",
                    singleLine = false,
                    minLines = 1,
                    maxLines = 3,
                    trailingIcon = {
                        if (isSearchingAddress) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = SageGreenPrimary
                            )
                        } else if (selectedLat != null && selectedLon != null) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = "Координаты определены",
                                tint = SageGreenPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                if (selectedLat != null && selectedLon != null && addressStreet.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF4E9E76),
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        val cleanDisplay = addressStreet.removePrefix("Санкт-Петербург, ").trim()
                        Text(
                            text = "✓ Точный адрес выбран: $cleanDisplay",
                            color = Color(0xFF4E9E76),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (addressSuggestions.isNotEmpty()) {
                    val isDark = LocalIsDarkTheme.current
                    val dropContainerColor = if (isDark) Color(0xFF232D26) else Color.White
                    val dropBorderColor = if (isDark) Color(0xFF4C7A5B) else SageGreenPrimary.copy(alpha = 0.35f)
                    val dropTextColor = if (isDark) Color.White else Color(0xFF1B1B1B)
                    val dropDividerColor = if (isDark) Color(0xFF334237) else DividerMuted.copy(alpha = 0.5f)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                            .heightIn(max = 240.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = dropContainerColor),
                        border = BorderStroke(1.dp, dropBorderColor),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                                .padding(vertical = 4.dp)
                        ) {
                            addressSuggestions.forEachIndexed { index, suggestion ->
                                val full = suggestion.fullAddress
                                val spbPrefix = "Санкт-Петербург, "
                                val annotatedAddress = buildAnnotatedString {
                                    if (full.startsWith(spbPrefix, ignoreCase = true)) {
                                        append(spbPrefix)
                                        withStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold)) {
                                            append(full.substring(spbPrefix.length))
                                        }
                                    } else {
                                        withStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold)) {
                                            append(full)
                                        }
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            HapticHelper.lightClick(view, context)
                                            addressStreet = suggestion.fullAddress
                                            selectedLat = suggestion.lat
                                            selectedLon = suggestion.lon
                                            viewModel.clearAddressSuggestions()
                                        }
                                        .padding(horizontal = 14.dp, vertical = 10.dp)
                                ) {
                                    Text(
                                        text = annotatedAddress,
                                        fontSize = 14.sp,
                                        color = dropTextColor,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        lineHeight = 18.sp
                                    )
                                }
                                if (index < addressSuggestions.lastIndex) {
                                    HorizontalDivider(
                                        color = dropDividerColor,
                                        thickness = 0.5.dp,
                                        modifier = Modifier.padding(horizontal = 12.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AppTextField(
                        value = entrance,
                        onValueChange = { entrance = it },
                        label = "Парадная",
                        placeholder = "1",
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    AppTextField(
                        value = floor,
                        onValueChange = { floor = it },
                        label = "Этаж",
                        placeholder = "5",
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    AppTextField(
                        value = apartment,
                        onValueChange = { apartment = it },
                        label = "Квартира",
                        placeholder = "42",
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                AppTextField(
                    value = clientComment,
                    onValueChange = { clientComment = it },
                    label = "Постоянный комментарий",
                    placeholder = "домофон, поводок и т.д.",
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
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
                            val formattedPhone = formatRussianPhoneFromDigits(phoneDigits)
                            if (fullName.isNotBlank() && phoneDigits.isNotBlank()) {
                                val validPets = petList.filter { it.name.isNotBlank() }.ifEmpty {
                                    listOf(PetItem(name = "Питомец", type = PetType.DOG))
                                }
                                val addressChanged = client == null ||
                                    client.addressStreet.trim() != addressStreet.trim() ||
                                    client.entrance.trim() != entrance.trim()
                                val formattedSecondaryPhone = if (hasSecondaryContact && secondaryContactPhoneDigits.isNotBlank()) {
                                    formatRussianPhoneFromDigits(secondaryContactPhoneDigits)
                                } else ""
                                val updated = ClientEntity(
                                    id = client?.id ?: 0L,
                                    fullName = fullName.trim(),
                                    phone = formattedPhone,
                                    messengerType = messengerType,
                                    messengerIdentifier = messengerIdentifier.trim(),
                                    petsJson = "",
                                    addressStreet = addressStreet.trim(),
                                    entrance = entrance.trim(),
                                    floor = floor.trim(),
                                    apartment = apartment.trim(),
                                    clientComment = clientComment.trim(),
                                    secondaryContactName = if (hasSecondaryContact) secondaryContactName.trim() else "",
                                    secondaryContactPhone = formattedSecondaryPhone,
                                    latitude = selectedLat ?: (if (addressChanged) null else client?.latitude),
                                    longitude = selectedLon ?: (if (addressChanged) null else client?.longitude)
                                )
                                HapticHelper.success(view, context)
                                onSave(updated, validPets)
                            }
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
