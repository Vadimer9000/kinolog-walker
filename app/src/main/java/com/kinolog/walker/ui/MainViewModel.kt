package com.kinolog.walker.ui

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.kinolog.walker.data.KinologRepository
import com.kinolog.walker.model.ClientEntity
import com.kinolog.walker.model.MessengerType
import com.kinolog.walker.model.PetItem
import com.kinolog.walker.model.PetType
import com.kinolog.walker.model.WalkCategory
import com.kinolog.walker.model.WalkEventEntity
import com.kinolog.walker.ui.components.MapMarkerPoint
import com.kinolog.walker.ui.components.NavigationTab
import com.kinolog.walker.util.IntentHelper
import com.kinolog.walker.util.PreferencesManager
import com.kinolog.walker.util.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.UUID

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = KinologRepository(application)
    private val prefs = PreferencesManager(application)
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)

    // Current navigation tab
    private val _currentTab = MutableStateFlow(NavigationTab.SCHEDULE)
    val currentTab: StateFlow<NavigationTab> = _currentTab.asStateFlow()

    // Selected Date for Schedule
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    // Transport slider mode ("pd" or "mt")
    private val _routingMode = MutableStateFlow(prefs.routingMode)
    val routingMode: StateFlow<String> = _routingMode.asStateFlow()

    // Yandex API key
    private val _yandexApiKey = MutableStateFlow(prefs.yandexApiKey)
    val yandexApiKey: StateFlow<String> = _yandexApiKey.asStateFlow()

    // Theme mode flow
    private val _themeMode = MutableStateFlow(prefs.themeMode)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    fun setThemeMode(mode: ThemeMode) {
        prefs.themeMode = mode
        _themeMode.value = mode
    }

    // Embedded map enabled flow (if disabled, app displays pure fast Plan mode)
    private val _isMapEnabled = MutableStateFlow(prefs.isMapEnabled)
    val isMapEnabled: StateFlow<Boolean> = _isMapEnabled.asStateFlow()

    fun setMapEnabled(enabled: Boolean) {
        prefs.isMapEnabled = enabled
        _isMapEnabled.value = enabled
    }

    // Client search query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Live GPS coordinates of user (myLat, myLon)
    private val _currentLocation = MutableStateFlow<Pair<Double, Double>?>(null)
    val currentLocation: StateFlow<Pair<Double, Double>?> = _currentLocation.asStateFlow()

    // Clients flow
    val clients: StateFlow<List<ClientEntity>> = _searchQuery
        .flatMapLatest { query -> repository.searchClients(query) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All clients mapped by ID for fast lookup
    val clientsMap: StateFlow<Map<Long, ClientEntity>> = repository.allClients
        .combine(MutableStateFlow(Unit)) { list, _ -> list.associateBy { it.id } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // Walks for selected date
    val walksForSelectedDate: StateFlow<List<WalkEventEntity>> = _selectedDate
        .flatMapLatest { date -> repository.getWalksForDate(date.toEpochDay()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All walks flow for reporting / analytics
    val allWalks: StateFlow<List<WalkEventEntity>> = repository.allWalks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Map Markers for selected date (only non-completed walks are shown on map)
    val mapMarkerPoints: StateFlow<List<MapMarkerPoint>> = combine(
        walksForSelectedDate,
        clientsMap
    ) { walks, map ->
        val sortedWalks = walks.filter { !it.isCompleted }.sortedBy { it.startTimeMinutes }
        var counter = 1
        val points = mutableListOf<MapMarkerPoint>()

        for (w in sortedWalks) {
            val client = map[w.clientId]
            if (client != null && client.latitude != null && client.longitude != null) {
                val hours = w.startTimeMinutes / 60
                val mins = w.startTimeMinutes % 60
                val timeStr = String.format("%02d:%02d", hours, mins)
                val pets = try {
                    json.decodeFromString<List<PetItem>>(client.petsJson)
                } catch (e: Exception) {
                    emptyList()
                }
                val petsTitle = pets.joinToString(", ") { "${it.type.icon} ${it.name}" }
                val label = if (petsTitle.isNotBlank()) petsTitle else client.fullName

                points.add(
                    MapMarkerPoint(
                        number = counter++,
                        timeLabel = timeStr,
                        title = label,
                        address = client.addressStreet,
                        latitude = client.latitude,
                        longitude = client.longitude
                    )
                )
            }
        }
        points
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        fetchCurrentLocation()
    }

    fun setTab(tab: NavigationTab) {
        _currentTab.value = tab
    }

    fun setSelectedDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setRoutingMode(mode: String) {
        _routingMode.value = mode
        prefs.routingMode = mode
    }

    fun setYandexApiKey(key: String) {
        _yandexApiKey.value = key
        prefs.yandexApiKey = key
    }

    @SuppressLint("MissingPermission")
    fun fetchCurrentLocation() {
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { loc: Location? ->
                if (loc != null) {
                    _currentLocation.value = Pair(loc.latitude, loc.longitude)
                }
            }
        } catch (_: Exception) {
        }
    }

    fun toggleWalkCompletion(walk: WalkEventEntity) {
        viewModelScope.launch {
            repository.updateWalk(walk.copy(isCompleted = !walk.isCompleted))
        }
    }

    fun saveClient(
        id: Long = 0,
        fullName: String,
        phone: String,
        messengerType: MessengerType,
        messengerIdentifier: String,
        pets: List<PetItem>,
        addressStreet: String,
        entrance: String,
        floor: String,
        apartment: String,
        clientComment: String,
        latitude: Double? = null,
        longitude: Double? = null,
        onDone: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val petsJsonStr = json.encodeToString(pets)
            val entity = ClientEntity(
                id = id,
                fullName = fullName.trim(),
                phone = phone.trim(),
                messengerType = messengerType,
                messengerIdentifier = messengerIdentifier.trim(),
                petsJson = petsJsonStr,
                addressStreet = addressStreet.trim(),
                entrance = entrance.trim(),
                floor = floor.trim(),
                apartment = apartment.trim(),
                clientComment = clientComment.trim(),
                latitude = latitude,
                longitude = longitude
            )
            repository.saveClient(entity)
            onDone()
        }
    }

    fun deleteClient(client: ClientEntity) {
        viewModelScope.launch {
            repository.deleteClient(client)
        }
    }

    fun saveWalk(
        clientId: Long,
        category: WalkCategory,
        date: LocalDate,
        startTimeMinutes: Int,
        durationMinutes: Int,
        price: Int = 0,
        walkComment: String,
        isRecurring: Boolean,
        selectedDays: Set<DayOfWeek>,
        recurrenceEndDate: LocalDate,
        onDone: () -> Unit
    ) {
        viewModelScope.launch {
            if (!isRecurring) {
                val walk = WalkEventEntity(
                    seriesId = null,
                    clientId = clientId,
                    dateEpochDay = date.toEpochDay(),
                    startTimeMinutes = startTimeMinutes,
                    durationMinutes = durationMinutes,
                    price = price,
                    category = category,
                    walkComment = walkComment.trim(),
                    isCompleted = false
                )
                repository.saveWalk(walk)
            } else {
                val seriesId = UUID.randomUUID().toString()
                val walksToCreate = mutableListOf<WalkEventEntity>()
                var cur = date
                while (!cur.isAfter(recurrenceEndDate)) {
                    if (selectedDays.contains(cur.dayOfWeek)) {
                        walksToCreate.add(
                            WalkEventEntity(
                                seriesId = seriesId,
                                clientId = clientId,
                                dateEpochDay = cur.toEpochDay(),
                                startTimeMinutes = startTimeMinutes,
                                durationMinutes = durationMinutes,
                                price = price,
                                category = category,
                                walkComment = walkComment.trim(),
                                isCompleted = false
                            )
                        )
                    }
                    cur = cur.plusDays(1)
                }
                repository.saveWalks(walksToCreate)
            }
            onDone()
        }
    }

    fun updateWalk(walk: WalkEventEntity, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repository.updateWalk(walk)
            onDone()
        }
    }

    fun updateWalkSeries(
        walk: WalkEventEntity,
        updateEntireSeries: Boolean,
        onDone: () -> Unit = {}
    ) {
        viewModelScope.launch {
            if (updateEntireSeries && !walk.seriesId.isNullOrBlank()) {
                repository.updateWalkSeries(
                    seriesId = walk.seriesId,
                    startTimeMinutes = walk.startTimeMinutes,
                    durationMinutes = walk.durationMinutes,
                    price = walk.price
                )
                repository.updateWalk(walk)
            } else {
                repository.updateWalk(walk)
            }
            onDone()
        }
    }

    fun deleteWalk(walk: WalkEventEntity, deleteEntireSeries: Boolean) {
        viewModelScope.launch {
            if (deleteEntireSeries && !walk.seriesId.isNullOrBlank()) {
                repository.deleteWalkSeries(walk.seriesId, fromEpochDay = walk.dateEpochDay)
            } else {
                repository.deleteWalk(walk.id)
            }
        }
    }

    fun routeDayWalks(context: Context) {
        val walks = walksForSelectedDate.value.filter { !it.isCompleted }
        val sortedWalks = walks.sortedBy { it.startTimeMinutes }
        val map = clientsMap.value
        val points = mutableListOf<Pair<Double, Double>>()

        for (w in sortedWalks) {
            val c = map[w.clientId]
            if (c != null && c.latitude != null && c.longitude != null) {
                points.add(Pair(c.latitude, c.longitude))
            }
        }

        if (points.isEmpty()) {
            Toast.makeText(context, "Нет точек с координатами на выбранный день", Toast.LENGTH_SHORT).show()
            return
        }

        IntentHelper.openYandexMapsRoute(
            context = context,
            points = points,
            currentLocation = _currentLocation.value,
            routingMode = _routingMode.value
        )
    }

    fun routeSingleWalk(context: Context, walk: WalkEventEntity) {
        val client = clientsMap.value[walk.clientId]
        if (client?.latitude == null || client.longitude == null) {
            Toast.makeText(context, "У клиента не определены координаты", Toast.LENGTH_SHORT).show()
            return
        }

        IntentHelper.openYandexMapsRoute(
            context = context,
            points = listOf(Pair(client.latitude, client.longitude)),
            currentLocation = _currentLocation.value,
            routingMode = _routingMode.value
        )
    }

    fun exportDatabaseToTelegram(context: Context) {
        viewModelScope.launch {
            try {
                val backupFile = repository.exportBackup()
                val uri = repository.getBackupContentUri(backupFile)

                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/json"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "Резервная копия базы Кинолог-Выгульщик")
                    putExtra(Intent.EXTRA_TEXT, "Файл резервной копии: ${backupFile.name}")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                val tgPackage = "org.telegram.messenger"
                if (isPackageInstalled(context, tgPackage)) {
                    sendIntent.setPackage(tgPackage)
                    context.startActivity(sendIntent)
                } else {
                    val chooser = Intent.createChooser(sendIntent, "Отправить бэкап...")
                    context.startActivity(chooser)
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Ошибка экспорта: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun importDatabaseFromJson(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val jsonString = inputStream?.bufferedReader()?.use { it.readText() }
                if (jsonString.isNullOrBlank()) {
                    Toast.makeText(context, "Файл пуст", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val result = repository.importBackup(jsonString)
                Toast.makeText(
                    context,
                    "База восстановлена: ${result.first} клиентов, ${result.second} выгулов",
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Ошибка восстановления: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun isPackageInstalled(context: Context, packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: Exception) {
            false
        }
    }
}
