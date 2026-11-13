package com.kinolog.walker.data

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.kinolog.walker.model.BackupData
import com.kinolog.walker.model.ClientEntity
import com.kinolog.walker.model.PetItem
import com.kinolog.walker.model.WalkEventEntity
import com.kinolog.walker.util.GeocoderHelper
import com.kinolog.walker.util.NotificationHelper
import com.kinolog.walker.util.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class KinologRepository(
    private val context: Context,
    private val database: AppDatabase = AppDatabase.getInstance(context)
) {
    private val clientDao = database.clientDao()
    private val walkEventDao = database.walkEventDao()
    private val prefs = PreferencesManager(context)
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    val allClients: Flow<List<ClientEntity>> = clientDao.getAllClients()
    val allWalks: Flow<List<WalkEventEntity>> = walkEventDao.getAllWalks()

    fun searchClients(query: String): Flow<List<ClientEntity>> {
        val q = query.trim()
        if (q.isBlank()) {
            return clientDao.getAllClients()
        }
        return clientDao.getAllClients().map { list ->
            list.filter { client ->
                client.fullName.contains(q, ignoreCase = true) ||
                client.phone.contains(q, ignoreCase = true) ||
                client.addressStreet.contains(q, ignoreCase = true) ||
                try {
                    val pets = json.decodeFromString<List<PetItem>>(client.petsJson)
                    pets.any { pet ->
                        pet.name.contains(q, ignoreCase = true) ||
                        pet.breedOrDetails.contains(q, ignoreCase = true)
                    }
                } catch (e: Exception) {
                    client.petsJson.contains(q, ignoreCase = true)
                }
            }
        }
    }

    fun getClient(id: Long): Flow<ClientEntity?> = clientDao.getClientById(id)

    suspend fun getClientSync(id: Long): ClientEntity? = clientDao.getClientByIdSync(id)

    suspend fun saveClient(client: ClientEntity): Long = withContext(Dispatchers.IO) {
        var resolvedClient = client
        val existing = if (client.id != 0L) clientDao.getClientByIdSync(client.id) else null

        if (resolvedClient.latitude != null && resolvedClient.longitude != null) {
            // Exact coordinates explicitly provided (e.g. from selected address suggestion)
        } else if (resolvedClient.addressStreet.isNotBlank()) {
            val coords = GeocoderHelper.getCoordinates(
                context = context,
                address = resolvedClient.addressStreet.trim(),
                yandexApiKey = prefs.yandexApiKey
            )
            if (coords != null) {
                resolvedClient = resolvedClient.copy(
                    latitude = coords.first,
                    longitude = coords.second
                )
            } else {
                resolvedClient = resolvedClient.copy(
                    latitude = existing?.latitude,
                    longitude = existing?.longitude
                )
            }
        } else {
            resolvedClient = resolvedClient.copy(
                latitude = null,
                longitude = null
            )
        }
        if (resolvedClient.id == 0L) {
            clientDao.insertClient(resolvedClient)
        } else {
            clientDao.updateClient(resolvedClient)
            resolvedClient.id
        }
    }

    suspend fun autoGeocodeMissingClients() = withContext(Dispatchers.IO) {
        try {
            val forceRefresh = prefs.geocodingVersion < 2
            val all = clientDao.getAllClientsList()
            for (client in all) {
                if (client.addressStreet.isNotBlank()) {
                    val needsGeocode = client.latitude == null || client.longitude == null || forceRefresh
                    if (needsGeocode) {
                        val coords = GeocoderHelper.geocodeAddress(
                            context = context,
                            rawAddress = client.addressStreet.trim(),
                            yandexApiKey = prefs.yandexApiKey
                        )
                        if (coords != null) {
                            clientDao.updateClient(
                                client.copy(
                                    latitude = coords.first,
                                    longitude = coords.second
                                )
                            )
                        }
                        if (forceRefresh) {
                            kotlinx.coroutines.delay(1000)
                        }
                    }
                }
            }
            if (forceRefresh) {
                prefs.geocodingVersion = 2
            }
        } catch (_: Exception) {
        }
    }

    suspend fun deleteClient(client: ClientEntity) = withContext(Dispatchers.IO) {
        clientDao.deleteClient(client)
    }

    suspend fun deleteClientById(id: Long) = withContext(Dispatchers.IO) {
        clientDao.deleteClientById(id)
    }

    fun getWalksForDate(epochDay: Long): Flow<List<WalkEventEntity>> =
        walkEventDao.getWalksForDate(epochDay)

    suspend fun getWalksForDateSync(epochDay: Long): List<WalkEventEntity> =
        walkEventDao.getWalksForDateSync(epochDay)

    suspend fun saveWalk(walk: WalkEventEntity): Long = withContext(Dispatchers.IO) {
        val id = walkEventDao.insertWalk(walk)
        val savedWalk = if (walk.id == 0L) walk.copy(id = id) else walk
        val client = clientDao.getClientByIdSync(savedWalk.clientId)
        if (client != null && !savedWalk.isCompleted) {
            try {
                NotificationHelper.scheduleReminder(context, client, savedWalk)
            } catch (_: Exception) {}
        }
        id
    }

    suspend fun saveWalks(
        walks: List<WalkEventEntity>,
        isInfiniteRecurrence: Boolean = false
    ) = withContext(Dispatchers.IO) {
        val ids = walkEventDao.insertWalks(walks)
        val savedWalks = walks.mapIndexed { index, w ->
            if (w.id == 0L && index < ids.size) w.copy(id = ids[index]) else w
        }
        val minEpochDay = java.time.LocalDate.now().toEpochDay()
        val maxAlarmEpochDay = if (isInfiniteRecurrence) minEpochDay + 7 else Long.MAX_VALUE
        val maxAlarms = if (isInfiniteRecurrence) 20 else Int.MAX_VALUE

        var scheduledCount = 0
        for (w in savedWalks) {
            if (isInfiniteRecurrence && (w.dateEpochDay > maxAlarmEpochDay || scheduledCount >= maxAlarms)) {
                continue
            }
            val client = clientDao.getClientByIdSync(w.clientId)
            if (client != null && !w.isCompleted) {
                try {
                    NotificationHelper.scheduleReminder(context, client, w)
                    scheduledCount++
                } catch (_: Exception) {}
            }
        }
    }

    suspend fun updateWalk(walk: WalkEventEntity) = withContext(Dispatchers.IO) {
        walkEventDao.updateWalk(walk)
        val client = clientDao.getClientByIdSync(walk.clientId)
        if (client != null) {
            try {
                if (walk.isCompleted) {
                    NotificationHelper.cancelReminder(context, walk.id)
                } else {
                    NotificationHelper.scheduleReminder(context, client, walk)
                }
            } catch (_: Exception) {}
        }
    }

    suspend fun updateWalkSeries(
        seriesId: String,
        startTimeMinutes: Int,
        durationMinutes: Int,
        price: Int
    ) = withContext(Dispatchers.IO) {
        walkEventDao.updateAllWalksInSeries(seriesId, startTimeMinutes, durationMinutes, price)
        val seriesWalks = walkEventDao.getWalksBySeries(seriesId)
        val minEpochDay = java.time.LocalDate.now().toEpochDay()
        val maxAlarmEpochDay = minEpochDay + 7
        var scheduledCount = 0
        for (w in seriesWalks) {
            if (w.dateEpochDay < minEpochDay || w.dateEpochDay > maxAlarmEpochDay || scheduledCount >= 20) {
                continue
            }
            val client = clientDao.getClientByIdSync(w.clientId)
            if (client != null && !w.isCompleted) {
                try {
                    NotificationHelper.scheduleReminder(context, client, w)
                    scheduledCount++
                } catch (_: Exception) {}
            }
        }
    }

    suspend fun deleteWalk(walkId: Long) = withContext(Dispatchers.IO) {
        try {
            NotificationHelper.cancelReminder(context, walkId)
        } catch (_: Exception) {}
        walkEventDao.deleteWalkById(walkId)
    }

    suspend fun deleteWalkSeries(seriesId: String, fromEpochDay: Long? = null) = withContext(Dispatchers.IO) {
        if (fromEpochDay != null) {
            val futureWalks = walkEventDao.getFutureWalksBySeries(seriesId, fromEpochDay)
            for (w in futureWalks) {
                try {
                    NotificationHelper.cancelReminder(context, w.id)
                } catch (_: Exception) {}
            }
            walkEventDao.deleteFutureWalksBySeries(seriesId, fromEpochDay)
        } else {
            val seriesWalks = walkEventDao.getWalksBySeries(seriesId)
            for (w in seriesWalks) {
                try {
                    NotificationHelper.cancelReminder(context, w.id)
                } catch (_: Exception) {}
            }
            walkEventDao.deleteWalksBySeries(seriesId)
        }
    }

    suspend fun exportBackup(): File = withContext(Dispatchers.IO) {
        val clients = clientDao.getAllClientsList()
        val walks = walkEventDao.getAllWalksList()

        val dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd"))
        val backupData = BackupData(
            version = 1,
            exportDate = dateStr,
            clients = clients,
            walks = walks
        )
        val jsonText = json.encodeToString(backupData)

        val backupDir = File(context.cacheDir, "backups")
        if (!backupDir.exists()) {
            backupDir.mkdirs()
        }

        val backupFile = File(backupDir, "kinolog_backup_$dateStr.json")
        backupFile.writeText(jsonText, Charsets.UTF_8)
        backupFile
    }

    fun getBackupContentUri(file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    suspend fun importBackup(jsonString: String): Pair<Int, Int> = withContext(Dispatchers.IO) {
        val backupData = json.decodeFromString<BackupData>(jsonString)
        clientDao.insertClients(backupData.clients)
        walkEventDao.insertWalks(backupData.walks)

        // Reschedule notifications for pending walks
        NotificationHelper.rescheduleAllReminders(context)
        Pair(backupData.clients.size, backupData.walks.size)
    }
}
