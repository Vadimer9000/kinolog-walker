package com.kinolog.walker.util

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("kinolog_walker_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_YANDEX_API_KEY = "yandex_maps_api_key"
        private const val KEY_ROUTING_MODE = "routing_mode" // "pd" (pedestrian) or "mt" (masstransit)
    }

    var yandexApiKey: String
        get() = prefs.getString(KEY_YANDEX_API_KEY, "") ?: ""
        set(value) = prefs.edit().putString(KEY_YANDEX_API_KEY, value.trim()).apply()

    var routingMode: String
        get() = prefs.getString(KEY_ROUTING_MODE, "pd") ?: "pd"
        set(value) = prefs.edit().putString(KEY_ROUTING_MODE, value).apply()
}
