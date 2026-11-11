package com.kinolog.walker.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.CalendarContract
import android.widget.Toast
import com.kinolog.walker.model.ClientEntity
import com.kinolog.walker.model.MessengerType
import com.kinolog.walker.model.WalkEventEntity
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

object IntentHelper {

    fun dialPhone(context: Context, phone: String) {
        val cleanPhone = phone.replace("[^0-9+]".toRegex(), "")
        if (cleanPhone.isEmpty()) {
            Toast.makeText(context, "Номер телефона не указан", Toast.LENGTH_SHORT).show()
            return
        }
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$cleanPhone"))
        context.startActivity(intent)
    }

    fun openMessenger(context: Context, messengerType: MessengerType, identifier: String) {
        val cleanId = identifier.trim().removePrefix("@")
        if (cleanId.isEmpty()) {
            Toast.makeText(context, "Контакт мессенджера не указан", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            when (messengerType) {
                MessengerType.TELEGRAM -> {
                    val uri = Uri.parse("https://t.me/$cleanId")
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    intent.setPackage("org.telegram.messenger")
                    if (isPackageInstalled(context, "org.telegram.messenger")) {
                        context.startActivity(intent)
                    } else {
                        // Fallback to browser or any telegram client
                        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                    }
                }
                MessengerType.WHATSAPP -> {
                    val digitsOnly = cleanId.replace("[^0-9]".toRegex(), "")
                    val uri = Uri.parse("https://wa.me/$digitsOnly")
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    intent.setPackage("com.whatsapp")
                    if (isPackageInstalled(context, "com.whatsapp")) {
                        context.startActivity(intent)
                    } else {
                        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                    }
                }
                MessengerType.VK -> {
                    val uri = Uri.parse("https://vk.com/$cleanId")
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    intent.setPackage("com.vkontakte.android")
                    if (isPackageInstalled(context, "com.vkontakte.android")) {
                        context.startActivity(intent)
                    } else {
                        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                    }
                }
                MessengerType.MAX -> {
                    val deepLink = Uri.parse("https://max.ru/$cleanId")
                    val maxPackage = "ru.oneme.app"
                    if (isPackageInstalled(context, maxPackage)) {
                        val intent = Intent(Intent.ACTION_VIEW, deepLink)
                        intent.setPackage(maxPackage)
                        context.startActivity(intent)
                    } else {
                        val browserIntent = Intent(Intent.ACTION_VIEW, deepLink)
                        context.startActivity(browserIntent)
                    }
                }
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Не удалось открыть мессенджер: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    fun openYandexMapsRoute(
        context: Context,
        points: List<Pair<Double, Double>>,
        currentLocation: Pair<Double, Double>?,
        routingMode: String // "pd" (pedestrian) or "mt" (mass transit)
    ) {
        if (points.isEmpty()) {
            Toast.makeText(context, "Нет точек с координатами для построения маршрута", Toast.LENGTH_SHORT).show()
            return
        }

        // Prepare points string
        // If current location is available: "lat0,lon0~lat1,lon1~..."
        // If unavailable: "~lat1,lon1~lat2,lon2" (leading tilde tells Yandex Maps to use "My location")
        val rtextBuilder = StringBuilder()
        if (currentLocation != null) {
            rtextBuilder.append("${currentLocation.first},${currentLocation.second}~")
        } else {
            rtextBuilder.append("~")
        }

        val pointsStr = points.joinToString("~") { "${it.first},${it.second}" }
        rtextBuilder.append(pointsStr)

        val rtextParam = rtextBuilder.toString()
        val rttParam = if (routingMode == "mt") "mt" else "pd"

        val appUri = Uri.parse("yandexmaps://maps.yandex.ru/?rtext=$rtextParam&rtt=$rttParam")
        val webUri = Uri.parse("https://yandex.ru/maps/?rtext=$rtextParam&rtt=$rttParam")

        val mapIntent = Intent(Intent.ACTION_VIEW, appUri)
        mapIntent.setPackage("ru.yandex.yandexmaps")

        try {
            if (isPackageInstalled(context, "ru.yandex.yandexmaps")) {
                context.startActivity(mapIntent)
            } else {
                context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
            }
        } catch (e: Exception) {
            context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
        }
    }

    fun openYandexMapsRouteByAddress(
        context: Context,
        destinationAddress: String,
        currentLocation: Pair<Double, Double>?,
        routingMode: String
    ) {
        val cleanAddr = GeocoderHelper.cleanAddressForGeocoding(destinationAddress)
        if (cleanAddr.isEmpty()) {
            Toast.makeText(context, "Адрес не указан", Toast.LENGTH_SHORT).show()
            return
        }

        val rtext = if (currentLocation != null) {
            "${currentLocation.first},${currentLocation.second}~$cleanAddr"
        } else {
            "~$cleanAddr"
        }
        val encodedRtext = Uri.encode(rtext, ":,~")
        val rttParam = if (routingMode == "mt") "mt" else "pd"

        val appUri = Uri.parse("yandexmaps://maps.yandex.ru/?rtext=$encodedRtext&rtt=$rttParam")
        val webUri = Uri.parse("https://yandex.ru/maps/?rtext=$encodedRtext&rtt=$rttParam")

        val mapIntent = Intent(Intent.ACTION_VIEW, appUri)
        mapIntent.setPackage("ru.yandex.yandexmaps")

        try {
            if (isPackageInstalled(context, "ru.yandex.yandexmaps")) {
                context.startActivity(mapIntent)
            } else {
                context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
            }
        } catch (e: Exception) {
            try {
                context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
            } catch (_: Exception) {
                Toast.makeText(context, "Не удалось открыть карту", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun openGoogleCalendar(
        context: Context,
        client: ClientEntity,
        walk: WalkEventEntity
    ) {
        val date = LocalDate.ofEpochDay(walk.dateEpochDay)
        val hours = walk.startTimeMinutes / 60
        val mins = walk.startTimeMinutes % 60
        val startTime = LocalDateTime.of(date, LocalTime.of(hours, mins))
        val endTime = startTime.plusMinutes(walk.durationMinutes.toLong())

        val startMillis = startTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endMillis = endTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val fullAddress = buildString {
            append(client.addressStreet)
            if (client.entrance.isNotBlank()) append(", подъезд ${client.entrance}")
            if (client.floor.isNotBlank()) append(", этаж ${client.floor}")
            if (client.apartment.isNotBlank()) append(", кв. ${client.apartment}")
        }

        val title = "${walk.category.title}: ${client.fullName}"
        val desc = buildString {
            if (walk.walkComment.isNotBlank()) {
                append("Заметка: ${walk.walkComment}\n\n")
            }
            if (client.clientComment.isNotBlank()) {
                append("Комментарий клиента: ${client.clientComment}\n\n")
            }
            append("Телефон: ${client.phone}")
        }

        val intent = Intent(Intent.ACTION_INSERT)
            .setData(CalendarContract.Events.CONTENT_URI)
            .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMillis)
            .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endMillis)
            .putExtra(CalendarContract.Events.TITLE, title)
            .putExtra(CalendarContract.Events.DESCRIPTION, desc)
            .putExtra(CalendarContract.Events.EVENT_LOCATION, fullAddress)

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Приложение календаря не найдено", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isPackageInstalled(context: Context, packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
}
