package com.kinolog.walker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kinolog.walker.model.WalkEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WalkEventDao {
    @Query("SELECT * FROM walk_events ORDER BY dateEpochDay ASC, startTimeMinutes ASC")
    fun getAllWalks(): Flow<List<WalkEventEntity>>

    @Query("SELECT * FROM walk_events ORDER BY dateEpochDay ASC, startTimeMinutes ASC")
    suspend fun getAllWalksList(): List<WalkEventEntity>

    @Query("SELECT * FROM walk_events WHERE dateEpochDay = :epochDay ORDER BY startTimeMinutes ASC")
    fun getWalksForDate(epochDay: Long): Flow<List<WalkEventEntity>>

    @Query("SELECT * FROM walk_events WHERE dateEpochDay = :epochDay ORDER BY startTimeMinutes ASC")
    suspend fun getWalksForDateSync(epochDay: Long): List<WalkEventEntity>

    @Query("SELECT * FROM walk_events WHERE id = :id LIMIT 1")
    fun getWalkById(id: Long): Flow<WalkEventEntity?>

    @Query("SELECT * FROM walk_events WHERE id = :id LIMIT 1")
    suspend fun getWalkByIdSync(id: Long): WalkEventEntity?

    @Query("SELECT * FROM walk_events WHERE dateEpochDay >= :fromEpochDay AND isCompleted = 0 ORDER BY dateEpochDay ASC, startTimeMinutes ASC")
    suspend fun getUpcomingUncompletedWalks(fromEpochDay: Long): List<WalkEventEntity>

    @Query("SELECT * FROM walk_events WHERE seriesId = :seriesId")
    suspend fun getWalksBySeries(seriesId: String): List<WalkEventEntity>

    @Query("SELECT * FROM walk_events WHERE seriesId = :seriesId AND dateEpochDay >= :fromEpochDay")
    suspend fun getFutureWalksBySeries(seriesId: String, fromEpochDay: Long): List<WalkEventEntity>

    @Query("UPDATE walk_events SET startTimeMinutes = :startTimeMinutes, durationMinutes = :durationMinutes, price = :price WHERE seriesId = :seriesId")
    suspend fun updateAllWalksInSeries(seriesId: String, startTimeMinutes: Int, durationMinutes: Int, price: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWalk(walk: WalkEventEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWalks(walks: List<WalkEventEntity>)

    @Update
    suspend fun updateWalk(walk: WalkEventEntity)

    @Delete
    suspend fun deleteWalk(walk: WalkEventEntity)

    @Query("DELETE FROM walk_events WHERE id = :id")
    suspend fun deleteWalkById(id: Long)

    @Query("DELETE FROM walk_events WHERE seriesId = :seriesId")
    suspend fun deleteWalksBySeries(seriesId: String)

    @Query("DELETE FROM walk_events WHERE seriesId = :seriesId AND dateEpochDay >= :fromEpochDay")
    suspend fun deleteFutureWalksBySeries(seriesId: String, fromEpochDay: Long)

    @Query("DELETE FROM walk_events")
    suspend fun deleteAllWalks()
}
