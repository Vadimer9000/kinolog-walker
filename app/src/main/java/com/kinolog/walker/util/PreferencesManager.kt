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
    }

    var yandexApiKey: String
        get() = prefs.getString(KEY_YANDEX_API_KEY, "") ?: ""
        set(value) = prefs.edit().putString(KEY_YANDEX_API_KEY, value.trim()).apply()

    var routingMode: String
        get() = prefs.getString(KEY_ROUTING_MODE, "pd") ?: "pd"
        set(value) = prefs.edit().putString(KEY_ROUTING_MODE, value).apply()

    var isCompactView: Boolean
        get() = prefs.getBoolean(KEY_IS_COMPACT_VIEW, false)
        set(value) = prefs.edit().putBoolean(KEY_IS_COMPACT_VIEW, value).apply()

    var themeMode: ThemeMode
        get() {
            val name = prefs.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name
            return try { ThemeMode.valueOf(name) } catch (e: Exception) { ThemeMode.SYSTEM }
        }
        set(value) = prefs.edit().putString(KEY_THEME_MODE, value.name).apply()
}
