package com.kinolog.walker.model

import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
enum class MessengerType(val title: String) {
    TELEGRAM("Telegram"),
    WHATSAPP("WhatsApp"),
    VK("ВКонтакте"),
    MAX("Max")
}

@Serializable
enum class PetType(val title: String, val icon: String) {
    DOG("Собака", "🐕"),
    CAT("Кошка", "🐈")
}

@Serializable
data class PetItem(
    val id: String = UUID.randomUUID().toString(),
    val type: PetType = PetType.DOG,
    val name: String,
    val breedOrDetails: String = ""
)

@Serializable
enum class WalkCategory(val title: String, val hexColor: Long) {
    REGULAR_WALK("Обычный выгул", 0xFF5E9E7E),
    CANINE_TRAINING("Кинология / Дрессировка", 0xFF3D7A60),
    BEHAVIOR_CORRECTION("Коррекция поведения", 0xFFD97736),
    PET_SITTING("Передержка / Визит", 0xFF4A89DC);

    val composeColor: Color
        get() = Color(hexColor)
}

@Serializable
@Entity(tableName = "clients")
data class ClientEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fullName: String,
    val phone: String,
    val messengerType: MessengerType,
    val messengerIdentifier: String,
    val petsJson: String,
    val addressStreet: String,
    val entrance: String = "",
    val floor: String = "",
    val apartment: String = "",
    val clientComment: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null
)

@Serializable
@Entity(
    tableName = "walk_events",
    foreignKeys = [
        ForeignKey(
            entity = ClientEntity::class,
            parentColumns = ["id"],
            childColumns = ["clientId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["clientId"]), Index(value = ["dateEpochDay"])]
)
data class WalkEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val seriesId: String? = null,
    val clientId: Long,
    val dateEpochDay: Long,
    val startTimeMinutes: Int,
    val durationMinutes: Int,
    val price: Int = 0,
    val category: WalkCategory,
    val walkComment: String = "",
    val isCompleted: Boolean = false
)

data class ClientWithWalks(
    val client: ClientEntity,
    val walk: WalkEventEntity
)

@Serializable
data class BackupData(
    val version: Int = 1,
    val exportDate: String,
    val clients: List<ClientEntity>,
    val walks: List<WalkEventEntity>
)
