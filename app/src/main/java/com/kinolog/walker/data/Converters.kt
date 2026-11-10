package com.kinolog.walker.data

import androidx.room.TypeConverter
import com.kinolog.walker.model.MessengerType
import com.kinolog.walker.model.PetItem
import com.kinolog.walker.model.WalkCategory
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    @TypeConverter
    fun fromMessengerType(value: MessengerType?): String? = value?.name

    @TypeConverter
    fun toMessengerType(value: String?): MessengerType? = value?.let {
        try { MessengerType.valueOf(it) } catch (e: Exception) { MessengerType.TELEGRAM }
    }

    @TypeConverter
    fun fromWalkCategory(value: WalkCategory?): String? = value?.name

    @TypeConverter
    fun toWalkCategory(value: String?): WalkCategory? = value?.let {
        try { WalkCategory.valueOf(it) } catch (e: Exception) { WalkCategory.REGULAR_WALK }
    }

    @TypeConverter
    fun fromPetList(value: List<PetItem>?): String = json.encodeToString(value ?: emptyList())

    @TypeConverter
    fun toPetList(value: String?): List<PetItem> = if (value.isNullOrBlank()) {
        emptyList()
    } else {
        try {
            json.decodeFromString<List<PetItem>>(value)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
