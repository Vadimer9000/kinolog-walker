package com.kinolog.walker.util

import android.content.Context
import android.content.SharedPreferences

enum class ThemeMode(val title: String) {
    SYSTEM("Как в системе"),
    LIGHT("Светлая"),
    DARK("Тёмная")
}

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("kinolog_walker_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_YANDEX_API_KEY = "yandex_maps_api_key"
        private const val KEY_ROUTING_MODE = "routing_mode" // "pd" (pedestrian) or "mt" (masstransit)
        private const val KEY_IS_COMPACT_VIEW = "is_compact_view"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_IS_MAP_ENABLED = "is_map_enabled"
        private const val KEY_GEOCODING_VERSION = "geocoding_version"
        private const val KEY_PRICE_PRESETS = "price_presets"
        private const val KEY_HAPTIC_FEEDBACK_ENABLED = "haptic_feedback_enabled"
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        private const val KEY_NOTIFICATION_LEAD_TIME_MINUTES = "notification_lead_time_minutes"
        val DEFAULT_PRICE_PRESETS = listOf(500, 800, 1000, 1500)
    }

    var pricePresets: List<Int>
        get() {
            val raw = prefs.getString(KEY_PRICE_PRESETS, null) ?: return DEFAULT_PRICE_PRESETS
            val list = raw.split(",").mapNotNull { it.trim().toIntOrNull() }.filter { it > 0 }
            return if (list.isNotEmpty()) list else DEFAULT_PRICE_PRESETS
        }
        set(value) {
            val str = value.filter { it > 0 }.joinToString(",")
            prefs.edit().putString(KEY_PRICE_PRESETS, str).apply()
        }

    var geocodingVersion: Int
        get() = prefs.getInt(KEY_GEOCODING_VERSION, 0)
        set(value) = prefs.edit().putInt(KEY_GEOCODING_VERSION, value).apply()

    var isMapEnabled: Boolean
        get() = prefs.getBoolean(KEY_IS_MAP_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_IS_MAP_ENABLED, value).apply()

    var yandexApiKey: String
        get() = prefs.getString(KEY_YANDEX_API_KEY, "") ?: ""
        set(value) = prefs.edit().putString(KEY_YANDEX_API_KEY, value.trim()).apply()

    var routingMode: String
        get() = prefs.getString(KEY_ROUTING_MODE, "pd") ?: "pd"
        set(value) = prefs.edit().putString(KEY_ROUTING_MODE, value).apply()

    var isCompactView: Boolean
        get() = prefs.getBoolean(KEY_IS_COMPACT_VIEW, false)
        set(value) = prefs.edit().putBoolean(KEY_IS_COMPACT_VIEW, value).apply()

    var isHapticFeedbackEnabled: Boolean
        get() = prefs.getBoolean(KEY_HAPTIC_FEEDBACK_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_HAPTIC_FEEDBACK_ENABLED, value).apply()

    var isNotificationsEnabled: Boolean
        get() = prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, value).apply()

    var notificationLeadTimeMinutes: Int
        get() = prefs.getInt(KEY_NOTIFICATION_LEAD_TIME_MINUTES, 30)
        set(value) = prefs.edit().putInt(KEY_NOTIFICATION_LEAD_TIME_MINUTES, value).apply()

    var themeMode: ThemeMode
        get() {
            val name = prefs.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name
            return try { ThemeMode.valueOf(name) } catch (e: Exception) { ThemeMode.SYSTEM }
        }
        set(value) = prefs.edit().putString(KEY_THEME_MODE, value.name).apply()
}
