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

        // 1. Try Yandex HTTP Geocoder if API key is provided
        if (yandexApiKey.isNotBlank()) {
            try {
                val yandexResult = geocodeWithYandex(cleanAddress, yandexApiKey)
                if (yandexResult != null) {
                    return@withContext yandexResult
                }
            } catch (_: Exception) {
            }
        }

        // 2. Try OpenStreetMap Nominatim Geocoder with exact house number matching
        try {
            val nominatimResult = geocodeWithNominatim(cleanAddress)
            if (nominatimResult != null) {
                return@withContext nominatimResult
            }
        } catch (_: Exception) {
        }

        // 3. Fallback: try raw address with Nominatim if clean differed
        if (rawAddress.trim() != cleanAddress) {
            try {
                val rawNominatim = geocodeWithNominatim(rawAddress.trim())
                if (rawNominatim != null) {
                    return@withContext rawNominatim
                }
            } catch (_: Exception) {
            }
        }

        // 4. Fallback: Android System Geocoder (Google Geocoder fallback)
        try {
            val systemResult = geocodeWithSystem(context, cleanAddress)
            if (systemResult != null) {
                return@withContext systemResult
            }
        } catch (_: Exception) {
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
            .replace(Regex("(?:^|[,\\s])(?:дом|д\\.?)\\s*(\\d+)", RegexOption.IGNORE_CASE), ", $1")
            .replace(Regex(",\\s*,", RegexOption.IGNORE_CASE), ",")
            .trim()
            .trimEnd(',')
            .trim()
    }

    fun extractExpectedHouse(s: String): String {
        val clean = cleanAddressForGeocoding(s)
        val m = Regex("(?:^|[,\\s])(?:дом|д\\.?)\\s*(\\d+[\\s\\-/]?(?:(?:к|корп|корпус|лит|литера|/|стр|строение)\\.?\\s*[0-9А-Яа-яA-Za-z]+)?)", RegexOption.IGNORE_CASE).find(clean)
            ?: Regex("(?:^|[,\\s])(\\d+[\\s\\-/]?(?:(?:к|корп|корпус|лит|литера|/|стр|строение)\\.?\\s*[0-9А-Яа-яA-Za-z]+)?)\\s*$", RegexOption.IGNORE_CASE).find(clean)
        return m?.groupValues?.get(1)?.trim() ?: ""
    }

    fun normalizeHouseNumber(house: String): String {
        return house.lowercase()
            .replace(Regex("[\\s.\\-/]+"), "")
            .replace("корпус", "к")
            .replace("корп", "к")
            .replace("строение", "с")
            .replace("стр", "с")
            .replace("литера", "лит")
    }

    private fun geocodeWithNominatim(address: String): Pair<Double, Double>? {
        val clean = cleanAddressForGeocoding(address)
        val expectedHouse = extractExpectedHouse(address)
        val normExpected = normalizeHouseNumber(expectedHouse)

        val candidateQueries = mutableListOf<String>()
        candidateQueries.add(clean)

        val parts = clean.split(',').map { it.trim() }.filter { it.isNotEmpty() }
        if (parts.size >= 2 && expectedHouse.isNotBlank()) {
            val withoutHouse = parts.filter { !it.contains(expectedHouse) }.joinToString(", ")
            if (withoutHouse.isNotBlank()) {
                candidateQueries.add("$expectedHouse, $withoutHouse")
            }
        }

        for (query in candidateQueries.distinct()) {
            val result = queryNominatimEndpoint(query, normExpected)
            if (result != null) return result
        }
        return null
    }

    private fun queryNominatimEndpoint(query: String, normExpectedHouse: String): Pair<Double, Double>? {
        return try {
            val encodedAddress = URLEncoder.encode(query, "UTF-8")
            val endpoint = "https://nominatim.openstreetmap.org/search?q=$encodedAddress&format=json&addressdetails=1&limit=10"
            val url = URL(endpoint)
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 6000
            conn.readTimeout = 6000
            conn.setRequestProperty("User-Agent", "KinologWalkerApp/1.0 (contact@kinolog.local)")
            conn.requestMethod = "GET"

            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                val body = conn.inputStream.bufferedReader().use { it.readText() }
                val jsonArray = org.json.JSONArray(body)

                // Pass 1: exact match on address.house_number
                if (normExpectedHouse.isNotEmpty()) {
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        val addr = obj.optJSONObject("address")
                        val houseNumber = addr?.optString("house_number", "") ?: ""
                        if (houseNumber.isNotEmpty() && normalizeHouseNumber(houseNumber) == normExpectedHouse) {
                            val lat = obj.optString("lat").toDoubleOrNull()
                            val lon = obj.optString("lon").toDoubleOrNull()
                            if (lat != null && lon != null) {
                                return Pair(lat, lon)
                            }
                        }
                    }
                }

                // Pass 2: if no expected house, match building
                if (normExpectedHouse.isEmpty()) {
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        val clazz = obj.optString("class")
                        if (clazz == "building" || clazz == "place") {
                            val lat = obj.optString("lat").toDoubleOrNull()
                            val lon = obj.optString("lon").toDoubleOrNull()
                            if (lat != null && lon != null) {
                                return Pair(lat, lon)
                            }
                        }
                    }
                    if (jsonArray.length() > 0) {
                        val obj = jsonArray.getJSONObject(0)
                        val lat = obj.optString("lat").toDoubleOrNull()
                        val lon = obj.optString("lon").toDoubleOrNull()
                        if (lat != null && lon != null) {
                            return Pair(lat, lon)
                        }
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
