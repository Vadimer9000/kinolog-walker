package com.kinolog.walker.util

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object GeocoderHelper {

    suspend fun geocodeAddress(
        context: Context,
        rawAddress: String,
        yandexApiKey: String = ""
    ): Pair<Double, Double>? = withContext(Dispatchers.IO) {
        val cleanAddress = cleanAddressForGeocoding(rawAddress)
        if (cleanAddress.isEmpty()) return@withContext null

        // 1. Try Android System Geocoder
        try {
            val systemResult = geocodeWithSystem(context, cleanAddress)
            if (systemResult != null) {
                return@withContext systemResult
            }
        } catch (_: Exception) {
        }

        // 2. Try Yandex HTTP Geocoder fallback if API key is provided
        if (yandexApiKey.isNotBlank()) {
            try {
                val yandexResult = geocodeWithYandex(cleanAddress, yandexApiKey)
                if (yandexResult != null) {
                    return@withContext yandexResult
                }
            } catch (_: Exception) {
            }
        }

        // 3. Try OpenStreetMap Nominatim Geocoder (free, no API key required)
        try {
            val nominatimResult = geocodeWithNominatim(cleanAddress)
            if (nominatimResult != null) {
                return@withContext nominatimResult
            }
        } catch (_: Exception) {
        }

        // 4. Fallback: try raw address if clean differed
        if (rawAddress.trim() != cleanAddress) {
            try {
                val rawNominatim = geocodeWithNominatim(rawAddress.trim())
                if (rawNominatim != null) {
                    return@withContext rawNominatim
                }
            } catch (_: Exception) {
            }
        }

        null
    }

    fun cleanAddressForGeocoding(raw: String): String {
        return raw
            .replace(Regex(",?\\s*подъезд.*", RegexOption.IGNORE_CASE), "")
            .replace(Regex(",?\\s*парадная.*", RegexOption.IGNORE_CASE), "")
            .replace(Regex(",?\\s*этаж.*", RegexOption.IGNORE_CASE), "")
            .replace(Regex(",?\\s*кв\\..*", RegexOption.IGNORE_CASE), "")
            .replace(Regex(",?\\s*квартира.*", RegexOption.IGNORE_CASE), "")
            .replace(Regex(",?\\s*домофон.*", RegexOption.IGNORE_CASE), "")
            .trim()
            .trimEnd(',')
    }

    private fun geocodeWithNominatim(address: String): Pair<Double, Double>? {
        return try {
            val encodedAddress = URLEncoder.encode(address, "UTF-8")
            val endpoint = "https://nominatim.openstreetmap.org/search?q=$encodedAddress&format=json&limit=1"
            val url = URL(endpoint)
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 5000
            conn.readTimeout = 5000
            conn.setRequestProperty("User-Agent", "KinologWalkerApp/1.0 (contact@kinolog.local)")
            conn.requestMethod = "GET"

            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                val body = conn.inputStream.bufferedReader().use { it.readText() }
                val jsonArray = org.json.JSONArray(body)
                if (jsonArray.length() > 0) {
                    val obj = jsonArray.getJSONObject(0)
                    val lat = obj.getString("lat").toDoubleOrNull()
                    val lon = obj.getString("lon").toDoubleOrNull()
                    if (lat != null && lon != null) {
                        return Pair(lat, lon)
                    }
                }
            }
            null
        } catch (_: Exception) {
            null
        }
    }

    @Suppress("DEPRECATION")
    private suspend fun geocodeWithSystem(context: Context, address: String): Pair<Double, Double>? {
        if (!Geocoder.isPresent()) return null
        val geocoder = Geocoder(context, Locale.getDefault())

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            suspendCoroutine { cont ->
                try {
                    geocoder.getFromLocationName(address, 1, object : Geocoder.GeocodeListener {
                        override fun onGeocode(addresses: MutableList<Address>) {
                            if (addresses.isNotEmpty()) {
                                val first = addresses[0]
                                cont.resume(Pair(first.latitude, first.longitude))
                            } else {
                                cont.resume(null)
                            }
                        }

                        override fun onError(errorMessage: String?) {
                            cont.resume(null)
                        }
                    })
                } catch (e: Exception) {
                    cont.resume(null)
                }
            }
        } else {
            val list = geocoder.getFromLocationName(address, 1)
            if (!list.isNullOrEmpty()) {
                Pair(list[0].latitude, list[0].longitude)
            } else {
                null
            }
        }
    }

    private fun geocodeWithYandex(address: String, apiKey: String): Pair<Double, Double>? {
        val encodedAddress = URLEncoder.encode(address, "UTF-8")
        val endpoint = "https://geocode-maps.yandex.ru/1.x/?apikey=$apiKey&geocode=$encodedAddress&format=json&results=1"
        val url = URL(endpoint)
        val conn = url.openConnection() as HttpURLConnection
        conn.connectTimeout = 6000
        conn.readTimeout = 6000
        conn.requestMethod = "GET"

        return try {
            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                val body = conn.inputStream.bufferedReader().use { it.readText() }
                val root = JSONObject(body)
                val members = root.getJSONObject("response")
                    .getJSONObject("GeoObjectCollection")
                    .getJSONArray("featureMember")

                if (members.length() > 0) {
                    val pos = members.getJSONObject(0)
                        .getJSONObject("GeoObject")
                        .getJSONObject("Point")
                        .getString("pos") // "lon lat"
                    val parts = pos.split(" ")
                    if (parts.size >= 2) {
                        val lon = parts[0].toDoubleOrNull()
                        val lat = parts[1].toDoubleOrNull()
                        if (lat != null && lon != null) {
                            Pair(lat, lon)
                        } else null
                    } else null
                } else null
            } else null
        } finally {
            conn.disconnect()
        }
    }
}
