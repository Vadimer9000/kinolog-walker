package com.kinolog.walker.ui.screens

import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Chat
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.PersonSearch
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
import com.kinolog.walker.ui.components.GlassBox
import com.kinolog.walker.ui.components.PetTag
import com.kinolog.walker.ui.theme.CanvasAlabaster
import com.kinolog.walker.ui.theme.CharcoalPrimary
import com.kinolog.walker.ui.theme.DangerRedBg
import com.kinolog.walker.ui.theme.DangerRedText
import com.kinolog.walker.ui.theme.DeepForest
import com.kinolog.walker.ui.theme.MintLight
import com.kinolog.walker.ui.theme.OliveGreySecondary
import com.kinolog.walker.ui.theme.SageGreenPrimary
import com.kinolog.walker.ui.theme.SandBeige
import com.kinolog.walker.ui.theme.SoftMint
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

            // Search Bar (Liquid Glass)
            GlassBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = CircleShape,
                elevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = "Поиск",
                        tint = SageGreenPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = { Text("Поиск по имени, телефону, адресу...", fontSize = 13.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Rounded.Clear, contentDescription = "Очистить", tint = OliveGreySecondary)
                        }
                    }
                }
            }

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
            onDismiss = { showEditDialog = false },
            onSave = { updatedClient, pets ->
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
                    latitude = updatedClient.latitude,
                    longitude = updatedClient.longitude,
                    onDone = { showEditDialog = false }
                )
            }
        )
    }
}

fun formatRussianPhone(raw: String): String {
    val digits = raw.filter { it.isDigit() }
    if (digits.isEmpty()) return ""
    val nationalDigits = when {
        (digits.startsWith("7") || digits.startsWith("8")) && digits.length > 1 -> digits.substring(1)
        digits.startsWith("7") || digits.startsWith("8") -> ""
        else -> digits
    }.take(10)

    val sb = StringBuilder("+7 (")
    for (i in nationalDigits.indices) {
        if (i == 3) sb.append(") ")
        if (i == 6) sb.append("-")
        if (i == 8) sb.append("-")
        sb.append(nationalDigits[i])
    }
    return sb.toString()
}

@Composable
fun ClientFormDialog(
    client: ClientEntity?,
    onDismiss: () -> Unit,
    onSave: (ClientEntity, List<PetItem>) -> Unit
) {
    val context = LocalContext.current
    val json = remember { Json { ignoreUnknownKeys = true } }

    var fullName by remember { mutableStateOf(client?.fullName ?: "") }
    var phone by remember {
        mutableStateOf(if (client != null && client.phone.isNotBlank()) formatRussianPhone(client.phone) else "")
    }
    var messengerType by remember { mutableStateOf(client?.messengerType ?: MessengerType.TELEGRAM) }
    var messengerIdentifier by remember { mutableStateOf(client?.messengerIdentifier ?: "") }

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
                                    phone = formatRussianPhone(rawNum)
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
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("ФИО клиента") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Phone with Russian Mask
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = formatRussianPhone(it) },
                    label = { Text("Номер телефона") },
                    placeholder = { Text("+7 (___) ___-__-__") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Messenger Type Selector
                Text("Основной мессенджер:", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    MessengerType.entries.forEach { type ->
                        val isSelected = type == messengerType
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(CircleShape)
                                .background(if (isSelected) SageGreenPrimary else SandBeige)
                                .clickable { messengerType = type }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = type.title,
                                color = if (isSelected) Color.White else CharcoalPrimary,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Messenger ID
                OutlinedTextField(
                    value = messengerIdentifier,
                    onValueChange = { messengerIdentifier = it },
                    label = { Text("Логин / ID в ${messengerType.title} (например: @username)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

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
                                // Pet type selector [🐕 Собака | 🐈 Кошка]
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    PetType.entries.forEach { pType ->
                                        val isSelected = pet.type == pType
                                        Box(
                                            modifier = Modifier
                                                .clip(CircleShape)
                                                .background(if (isSelected) SageGreenPrimary else SandBeige)
                                                .clickable {
                                                    petList[index] = pet.copy(type = pType)
                                                }
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = "${pType.icon} ${pType.title}",
                                                color = if (isSelected) Color.White else CharcoalPrimary,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }

                                if (petList.size > 1) {
                                    IconButton(onClick = { petList.removeAt(index) }) {
                                        Icon(Icons.Rounded.Delete, contentDescription = "Удалить", tint = Color(0xFFD9534F), modifier = Modifier.size(18.dp))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = pet.name,
                                onValueChange = { petList[index] = pet.copy(name = it) },
                                label = { Text("Кличка питомца") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            OutlinedTextField(
                                value = pet.breedOrDetails,
                                onValueChange = { petList[index] = pet.copy(breedOrDetails = it) },
                                label = { Text("Порода / особенности (опционально)") },
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

                OutlinedTextField(
                    value = addressStreet,
                    onValueChange = { addressStreet = it },
                    label = { Text("Город, улица, дом") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = entrance,
                        onValueChange = { entrance = it },
                        label = { Text("Парадная") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = floor,
                        onValueChange = { floor = it },
                        label = { Text("Этаж") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = apartment,
                        onValueChange = { apartment = it },
                        label = { Text("Квартира") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = clientComment,
                    onValueChange = { clientComment = it },
                    label = { Text("Постоянный комментарий (домофон, поводок и т.д.)") },
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
                            if (fullName.isNotBlank() && phone.isNotBlank()) {
                                val validPets = petList.filter { it.name.isNotBlank() }.ifEmpty {
                                    listOf(PetItem(name = "Питомец", type = PetType.DOG))
                                }
                                val updated = ClientEntity(
                                    id = client?.id ?: 0L,
                                    fullName = fullName,
                                    phone = phone,
                                    messengerType = messengerType,
                                    messengerIdentifier = messengerIdentifier,
                                    petsJson = "",
                                    addressStreet = addressStreet,
                                    entrance = entrance,
                                    floor = floor,
                                    apartment = apartment,
                                    clientComment = clientComment,
                                    latitude = client?.latitude,
                                    longitude = client?.longitude
                                )
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
