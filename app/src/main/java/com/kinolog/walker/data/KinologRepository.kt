package com.kinolog.walker.data

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.kinolog.walker.model.BackupData
import com.kinolog.walker.model.ClientEntity
import com.kinolog.walker.model.WalkEventEntity
import com.kinolog.walker.util.GeocoderHelper
import com.kinolog.walker.util.PreferencesManager
import com.kinolog.walker.worker.WalkNotificationWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
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
        return if (query.isBlank()) {
            clientDao.getAllClients()
        } else {
            clientDao.searchClients(query.trim())
        }
    }

    fun getClient(id: Long): Flow<ClientEntity?> = clientDao.getClientById(id)

    suspend fun getClientSync(id: Long): ClientEntity? = clientDao.getClientByIdSync(id)

    suspend fun saveClient(client: ClientEntity): Long = withContext(Dispatchers.IO) {
        var resolvedClient = client
        val existing = if (client.id != 0L) clientDao.getClientByIdSync(client.id) else null
        val addressChanged = existing == null ||
                existing.addressStreet.trim() != client.addressStreet.trim()

        if (resolvedClient.addressStreet.isNotBlank()) {
            val coords = GeocoderHelper.geocodeAddress(
                context = context,
                rawAddress = resolvedClient.addressStreet.trim(),
                yandexApiKey = prefs.yandexApiKey
            )
            if (coords != null) {
                resolvedClient = resolvedClient.copy(
                    latitude = coords.first,
                    longitude = coords.second
                )
            } else if (!addressChanged && existing?.latitude != null) {
                resolvedClient = resolvedClient.copy(
                    latitude = existing.latitude,
                    longitude = existing.longitude
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
            val all = clientDao.getAllClientsList()
            val forceRefresh = prefs.geocodingVersion < 2
            for (client in all) {
                val needsGeocode = (client.latitude == null || client.longitude == null || forceRefresh) && client.addressStreet.isNotBlank()
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
            WalkNotificationWorker.scheduleReminder(context, client, savedWalk)
        }
        id
    }

    suspend fun saveWalks(walks: List<WalkEventEntity>) = withContext(Dispatchers.IO) {
        walkEventDao.insertWalks(walks)
        for (w in walks) {
            val client = clientDao.getClientByIdSync(w.clientId)
            if (client != null && !w.isCompleted) {
                WalkNotificationWorker.scheduleReminder(context, client, w)
            }
        }
    }

    suspend fun updateWalk(walk: WalkEventEntity) = withContext(Dispatchers.IO) {
        walkEventDao.updateWalk(walk)
        val client = clientDao.getClientByIdSync(walk.clientId)
        if (client != null) {
            if (walk.isCompleted) {
                WalkNotificationWorker.cancelReminder(context, walk.id)
            } else {
                WalkNotificationWorker.scheduleReminder(context, client, walk)
            }
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
        for (w in seriesWalks) {
            val client = clientDao.getClientByIdSync(w.clientId)
            if (client != null && !w.isCompleted) {
                WalkNotificationWorker.scheduleReminder(context, client, w)
            }
        }
    }

    suspend fun deleteWalk(walkId: Long) = withContext(Dispatchers.IO) {
        WalkNotificationWorker.cancelReminder(context, walkId)
        walkEventDao.deleteWalkById(walkId)
    }

    suspend fun deleteWalkSeries(seriesId: String, fromEpochDay: Long? = null) = withContext(Dispatchers.IO) {
        if (fromEpochDay != null) {
            val futureWalks = walkEventDao.getFutureWalksBySeries(seriesId, fromEpochDay)
            for (w in futureWalks) {
                WalkNotificationWorker.cancelReminder(context, w.id)
            }
            walkEventDao.deleteFutureWalksBySeries(seriesId, fromEpochDay)
        } else {
            val seriesWalks = walkEventDao.getWalksBySeries(seriesId)
            for (w in seriesWalks) {
                WalkNotificationWorker.cancelReminder(context, w.id)
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
        for (w in backupData.walks) {
            if (!w.isCompleted) {
                val client = clientDao.getClientByIdSync(w.clientId)
                if (client != null) {
                    WalkNotificationWorker.scheduleReminder(context, client, w)
                }
            }
        }
        Pair(backupData.clients.size, backupData.walks.size)
    }
}
