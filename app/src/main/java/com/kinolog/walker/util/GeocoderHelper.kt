package com.kinolog.walker.util

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

data class AddressSuggestion(
    val fullAddress: String,
    val subtitle: String,
    val lat: Double,
    val lon: Double
)

object GeocoderHelper {

    private var appContext: Context? = null
    private var defaultApiKey: String = ""

    fun init(context: Context, apiKey: String = "") {
        appContext = context.applicationContext
        defaultApiKey = apiKey
        YandexJsGeocoderBridge.initialize(context.applicationContext, apiKey)
    }

    suspend fun suggestAddresses(query: String): List<AddressSuggestion> {
        val ctx = appContext ?: return emptyList()
        return suggestAddresses(ctx, query, defaultApiKey)
    }

    suspend fun suggestAddresses(
        context: Context,
        rawQuery: String,
        yandexApiKey: String = ""
    ): List<AddressSuggestion> = withContext(Dispatchers.IO) {
        val trimmed = rawQuery.trim()
        if (trimmed.length < 3) return@withContext emptyList()

        val spbQuery = formatQueryForSpb(trimmed)
        val key = if (yandexApiKey.isNotBlank()) yandexApiKey else defaultApiKey

        // 1. Try Yandex HTTP Geocoder if API key is provided
        if (key.isNotBlank()) {
            try {
                val httpResults = queryYandexHttpGeocoder(spbQuery, key)
                if (httpResults.isNotEmpty()) {
                    return@withContext httpResults
                }
            } catch (_: Exception) {
            }
        }

        // 2. Try Yandex JS Geocoder Bridge (WebView with JS API)
        try {
            val jsResults = YandexJsGeocoderBridge.geocode(context, spbQuery, key)
            if (jsResults.isNotEmpty()) {
                return@withContext jsResults
            }
        } catch (_: Exception) {
        }

        // 3. Fallback: OpenStreetMap Nominatim
        try {
            val osmResults = queryNominatimSuggestions(spbQuery)
            if (osmResults.isNotEmpty()) {
                return@withContext osmResults
            }
        } catch (_: Exception) {
        }

        emptyList()
    }

    suspend fun getCoordinates(
        context: Context,
        address: String,
        yandexApiKey: String = ""
    ): Pair<Double, Double>? = withContext(Dispatchers.IO) {
        val cleanAddress = cleanAddressForGeocoding(address)
        if (cleanAddress.isEmpty()) return@withContext null

        val key = if (yandexApiKey.isNotBlank()) yandexApiKey else defaultApiKey

        // 1. Primary: Yandex Geocoder suggestions (1-to-1 Yandex coordinates)
        try {
            val suggestions = suggestAddresses(context, cleanAddress, key)
            if (suggestions.isNotEmpty()) {
                val expectedHouse = extractExpectedHouse(cleanAddress)
                val normExpected = normalizeHouseNumber(expectedHouse)
                if (normExpected.isNotEmpty()) {
                    val matched = suggestions.firstOrNull { sug ->
                        val sugHouse = extractExpectedHouse(sug.fullAddress)
                        normalizeHouseNumber(sugHouse) == normExpected
                    }
                    if (matched != null) {
                        return@withContext Pair(matched.lat, matched.lon)
                    }
                }
                val top = suggestions.first()
                return@withContext Pair(top.lat, top.lon)
            }
        } catch (_: Exception) {
        }

        // 2. Fallback: OpenStreetMap Nominatim with exact house number matching
        try {
            val nominatimResult = geocodeWithNominatim(cleanAddress)
            if (nominatimResult != null) {
                return@withContext nominatimResult
            }
        } catch (_: Exception) {
        }

        // 3. Fallback: try raw address with Nominatim if clean differed
        if (address.trim() != cleanAddress) {
            try {
                val rawNominatim = geocodeWithNominatim(address.trim())
                if (rawNominatim != null) {
                    return@withContext rawNominatim
                }
            } catch (_: Exception) {
            }
        }

        // 4. Fallback: Android System Geocoder
        try {
            val systemResult = geocodeWithSystem(context, cleanAddress)
            if (systemResult != null) {
                return@withContext systemResult
            }
        } catch (_: Exception) {
        }

        null
    }

    suspend fun geocodeAddress(
        context: Context,
        rawAddress: String,
        yandexApiKey: String = ""
    ): Pair<Double, Double>? = getCoordinates(context, rawAddress, yandexApiKey)

    fun formatQueryForSpb(query: String): String {
        val trimmed = query.trim()
        val lower = trimmed.lowercase()
        val spbKeywords = listOf(
            "санкт-петербург", "санкт петербург", "спб", "питер",
            "мурино", "кудрово", "всеволожск", "пушкин", "петергоф",
            "колпино", "гатчина", "павловск", "сестрорецк", "красное село",
            "кронштадт", "ломоносов", "сертолово", "выборг", "сосновый бор",
            "кировск", "тосно", "луга", "шушары", "янино", "бугры",
            "металлострой", "понтонный", "стрельна"
        )
        val hasCity = spbKeywords.any { lower.contains(it) }
        return if (hasCity) trimmed else "Санкт-Петербург, $trimmed"
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

    private fun queryYandexHttpGeocoder(query: String, apiKey: String): List<AddressSuggestion> {
        val encoded = URLEncoder.encode(query, "UTF-8")
        val endpoint = "https://geocode-maps.yandex.ru/1.x/?apikey=$apiKey&geocode=$encoded&format=json&results=8"
        val url = URL(endpoint)
        val conn = url.openConnection() as HttpURLConnection
        conn.connectTimeout = 4000
        conn.readTimeout = 4000
        conn.requestMethod = "GET"

        return try {
            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                val body = conn.inputStream.bufferedReader().use { it.readText() }
                val root = JSONObject(body)
                val members = root.getJSONObject("response")
                    .getJSONObject("GeoObjectCollection")
                    .getJSONArray("featureMember")

                val suggestions = mutableListOf<AddressSuggestion>()
                for (i in 0 until members.length()) {
                    val geoObj = members.getJSONObject(i).getJSONObject("GeoObject")
                    val meta = geoObj.optJSONObject("metaDataProperty")
                        ?.optJSONObject("GeocoderMetaData")
                    val text = meta?.optString("text") ?: ""
                    val name = geoObj.optString("name", "")
                    val desc = geoObj.optString("description", "")
                    val pos = geoObj.getJSONObject("Point").getString("pos") // "lon lat"
                    val parts = pos.split(" ")
                    if (parts.size >= 2) {
                        val lon = parts[0].toDoubleOrNull()
                        val lat = parts[1].toDoubleOrNull()
                        if (lat != null && lon != null) {
                            val full = YandexJsGeocoderBridge.cleanAddressText(if (text.isNotBlank()) text else name)
                            val subtitle = YandexJsGeocoderBridge.cleanSubtitle(desc, full)
                            suggestions.add(
                                AddressSuggestion(
                                    fullAddress = full,
                                    subtitle = subtitle,
                                    lat = lat,
                                    lon = lon
                                )
                            )
                        }
                    }
                }
                suggestions
            } else {
                emptyList()
            }
        } catch (_: Exception) {
            emptyList()
        } finally {
            conn.disconnect()
        }
    }

    private fun queryNominatimSuggestions(query: String): List<AddressSuggestion> {
        val clean = cleanAddressForGeocoding(query)
        val encoded = URLEncoder.encode(clean, "UTF-8")
        val endpoint = "https://nominatim.openstreetmap.org/search?q=$encoded&format=json&addressdetails=1&limit=8"
        val url = URL(endpoint)
        val conn = url.openConnection() as HttpURLConnection
        conn.connectTimeout = 4000
        conn.readTimeout = 4000
        conn.setRequestProperty("User-Agent", "KinologWalkerApp/1.0 (contact@kinolog.local)")
        conn.requestMethod = "GET"

        return try {
            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                val body = conn.inputStream.bufferedReader().use { it.readText() }
                val arr = JSONArray(body)
                val suggestions = mutableListOf<AddressSuggestion>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val lat = obj.optString("lat").toDoubleOrNull()
                    val lon = obj.optString("lon").toDoubleOrNull()
                    val displayName = obj.optString("display_name", "")
                    val addr = obj.optJSONObject("address")
                    val road = addr?.optString("road", "") ?: ""
                    val house = addr?.optString("house_number", "") ?: ""
                    val city = addr?.optString("city", "")
                        ?: addr?.optString("town", "")
                        ?: addr?.optString("village", "")
                        ?: ""
                    if (lat != null && lon != null) {
                        val full = if (road.isNotEmpty() && house.isNotEmpty()) {
                            if (city.isNotEmpty()) "$city, $road, $house" else "$road, $house"
                        } else {
                            displayName.split(",").take(3).joinToString(",").trim()
                        }
                        val subtitle = if (city.isNotEmpty()) city else displayName
                        suggestions.add(
                            AddressSuggestion(
                                fullAddress = YandexJsGeocoderBridge.cleanAddressText(full),
                                subtitle = subtitle,
                                lat = lat,
                                lon = lon
                            )
                        )
                    }
                }
                suggestions
            } else {
                emptyList()
            }
        } catch (_: Exception) {
            emptyList()
        } finally {
            conn.disconnect()
        }
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
                val jsonArray = JSONArray(body)

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
}
