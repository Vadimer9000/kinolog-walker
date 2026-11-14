package com.kinolog.walker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.kinolog.walker.model.ClientEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClientDao {
    @Query("SELECT * FROM clients ORDER BY fullName ASC")
    fun getAllClients(): Flow<List<ClientEntity>>

    @Query("SELECT * FROM clients ORDER BY fullName ASC")
    suspend fun getAllClientsList(): List<ClientEntity>

    @Query("SELECT * FROM clients WHERE id = :id LIMIT 1")
    fun getClientById(id: Long): Flow<ClientEntity?>

    @Query("SELECT * FROM clients WHERE id = :id LIMIT 1")
    suspend fun getClientByIdSync(id: Long): ClientEntity?

    @Query("SELECT * FROM clients WHERE fullName LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%' OR addressStreet LIKE '%' || :query || '%' OR petsJson LIKE '%' || :query || '%' ORDER BY fullName ASC")
    fun searchClients(query: String): Flow<List<ClientEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClient(client: ClientEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClients(clients: List<ClientEntity>)

    @Upsert
    suspend fun upsertClient(client: ClientEntity): Long

    @Upsert
    suspend fun upsertClients(clients: List<ClientEntity>)

    @Update
    suspend fun updateClient(client: ClientEntity)

    @Delete
    suspend fun deleteClient(client: ClientEntity)

    @Query("DELETE FROM clients WHERE id = :id")
    suspend fun deleteClientById(id: Long)

    @Query("DELETE FROM clients")
    suspend fun deleteAllClients()
}
