package com.kinolog.walker.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Chat
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.kinolog.walker.ui.theme.DeepForest
import com.kinolog.walker.ui.theme.DividerMuted
import com.kinolog.walker.ui.theme.MintLight
import com.kinolog.walker.ui.theme.OliveGreySecondary
import com.kinolog.walker.ui.theme.SageGreenPrimary
import com.kinolog.walker.ui.theme.SandBeige
import com.kinolog.walker.ui.theme.SoftMint
import com.kinolog.walker.util.IntentHelper
import kotlinx.serialization.json.Json
import java.util.UUID

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
                            elevation = 4.dp
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
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
                                            fontSize = 17.sp,
                                            color = CharcoalPrimary
                                        )
                                        Text(
                                            text = client.phone,
                                            fontSize = 13.sp,
                                            color = OliveGreySecondary
                                        )
                                    }

                                    Row {
                                        IconButton(onClick = {
                                            clientToEdit = client
                                            showEditDialog = true
                                        }) {
                                            Icon(Icons.Rounded.Edit, contentDescription = "Редактировать", tint = SageGreenPrimary)
                                        }
                                        IconButton(onClick = { clientToDelete = client }) {
                                            Icon(Icons.Rounded.Delete, contentDescription = "Удалить", tint = Color(0xFFD9534F))
                                        }
                                    }
                                }

                                // Pets Tags
                                if (pets.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
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
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Rounded.LocationOn,
                                        contentDescription = "Адрес",
                                        tint = SageGreenPrimary,
                                        modifier = Modifier.size(16.dp)
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
                                        color = CharcoalPrimary
                                    )
                                }

                                if (client.clientComment.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Заметка: ${client.clientComment}",
                                        fontSize = 12.sp,
                                        color = OliveGreySecondary
                                    )
                                }

                                // Quick Action Buttons
                                Spacer(modifier = Modifier.height(12.dp))
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
                                        shape = CircleShape,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Rounded.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Звонок", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
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
                                            containerColor = SageGreenPrimary,
                                            contentColor = Color.White
                                        ),
                                        shape = CircleShape,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Rounded.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(client.messengerType.title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }

        // FAB to Add Client
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
                .padding(end = 20.dp, bottom = 90.dp)
        ) {
            Icon(Icons.Rounded.Add, contentDescription = "Добавить клиента")
        }
    }

    // Delete Confirmation Dialog
    if (clientToDelete != null) {
        val client = clientToDelete!!
        AlertDialog(
            onDismissRequest = { clientToDelete = null },
            title = { Text("Удалить клиента?") },
            text = { Text("Вы действительно хотите удалить клиента «${client.fullName}» и все связанные с ним выгулы?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteClient(client)
                        clientToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD9534F))
                ) {
                    Text("Удалить", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { clientToDelete = null }) {
                    Text("Отмена", color = CharcoalPrimary)
                }
            }
        )
    }

    // Add / Edit Client Form Dialog
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
                    longitude = updatedClient.longitude
                ) {
                    showEditDialog = false
                }
            }
        )
    }
}

@Composable
fun ClientFormDialog(
    client: ClientEntity?,
    onDismiss: () -> Unit,
    onSave: (ClientEntity, List<PetItem>) -> Unit
) {
    val json = remember { Json { ignoreUnknownKeys = true } }

    var fullName by remember { mutableStateOf(client?.fullName ?: "") }
    var phone by remember { mutableStateOf(client?.phone ?: "") }
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
                    text = if (client == null) "Новый клиент" else "Редактирование",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = CharcoalPrimary
                )

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

                // Phone
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Номер телефона (+7...)") },
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
