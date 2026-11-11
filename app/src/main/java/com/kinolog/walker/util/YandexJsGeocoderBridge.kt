package com.kinolog.walker.util

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

object YandexJsGeocoderBridge {

    private var webView: WebView? = null
    @Volatile
    private var isYmapsReady = false
    private var readyDeferred = CompletableDeferred<Boolean>()
    private val mainHandler = Handler(Looper.getMainLooper())
    private val requestIdCounter = AtomicLong(0)
    private val pendingRequests = ConcurrentHashMap<Long, CompletableDeferred<List<AddressSuggestion>>>()
    private var currentApiKey: String? = null

    @SuppressLint("SetJavaScriptEnabled")
    fun initialize(context: Context, apiKey: String = "") {
        mainHandler.post {
            try {
                if (webView != null && currentApiKey == apiKey && isYmapsReady) {
                    return@post
                }

                if (webView == null || currentApiKey != apiKey) {
                    currentApiKey = apiKey
                    isYmapsReady = false
                    if (readyDeferred.isCompleted) {
                        readyDeferred = CompletableDeferred()
                    }

                    val wv = webView ?: WebView(context.applicationContext).also { webView = it }
                    wv.settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        cacheMode = WebSettings.LOAD_DEFAULT
                        mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    }
                    wv.resumeTimers()

                    wv.removeJavascriptInterface("AndroidBridge")
                    wv.addJavascriptInterface(JsInterface(), "AndroidBridge")

                    wv.webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                        }
                    }

                    val keyParam = if (apiKey.isNotBlank()) "apikey=$apiKey&" else ""
                    val html = """
                        <!DOCTYPE html>
                        <html>
                        <head>
                            <meta charset="utf-8">
                            <meta name="viewport" content="width=device-width, initial-scale=1.0">
                            <script src="https://api-maps.yandex.ru/2.1/?${keyParam}lang=ru_RU" type="text/javascript"></script>
                        </head>
                        <body>
                            <script>
                                var isYmapsReady = false;
                                function checkYmapsReady() {
                                    if (typeof ymaps !== 'undefined' && ymaps.ready) {
                                        ymaps.ready(function() {
                                            isYmapsReady = true;
                                            if (window.AndroidBridge && window.AndroidBridge.onYmapsReady) {
                                                window.AndroidBridge.onYmapsReady();
                                            }
                                        });
                                    } else {
                                        setTimeout(checkYmapsReady, 100);
                                    }
                                }
                                checkYmapsReady();

                                function performGeocode(reqId, query) {
                                    try {
                                        if (typeof ymaps === 'undefined' || !ymaps.geocode) {
                                            if (window.AndroidBridge && window.AndroidBridge.onGeocodeError) {
                                                window.AndroidBridge.onGeocodeError(reqId, "ymaps.geocode not available");
                                            }
                                            return;
                                        }
                                        ymaps.geocode(query, {
                                            results: 8,
                                            boundedBy: [[59.6, 29.4], [60.3, 31.0]]
                                        }).then(function(res) {
                                            var geoObjects = res.geoObjects;
                                            var list = [];
                                            geoObjects.each(function(obj) {
                                                var coords = obj.geometry ? obj.geometry.getCoordinates() : null;
                                                if (!coords || coords.length < 2) return;
                                                var full = (typeof obj.getAddressLine === 'function') ? obj.getAddressLine() : (obj.properties.get('text') || '');
                                                var name = obj.properties.get('name') || '';
                                                var desc = obj.properties.get('description') || '';
                                                list.push({
                                                    fullAddress: full || name,
                                                    name: name,
                                                    description: desc,
                                                    lat: Number(coords[0]),
                                                    lon: Number(coords[1])
                                                });
                                            });
                                            if (window.AndroidBridge && window.AndroidBridge.onGeocodeSuccess) {
                                                window.AndroidBridge.onGeocodeSuccess(reqId, JSON.stringify(list));
                                            }
                                        }).catch(function(err) {
                                            if (window.AndroidBridge && window.AndroidBridge.onGeocodeError) {
                                                window.AndroidBridge.onGeocodeError(reqId, String(err));
                                            }
                                        });
                                    } catch (e) {
                                        if (window.AndroidBridge && window.AndroidBridge.onGeocodeError) {
                                            window.AndroidBridge.onGeocodeError(reqId, String(e));
                                        }
                                    }
                                }
                            </script>
                        </body>
                        </html>
                    """.trimIndent()

                    wv.loadDataWithBaseURL("https://api-maps.yandex.ru", html, "text/html", "UTF-8", null)
                }
            } catch (e: Exception) {
                if (!readyDeferred.isCompleted) {
                    readyDeferred.complete(false)
                }
            }
        }
    }

    private class JsInterface {
        @JavascriptInterface
        fun onYmapsReady() {
            isYmapsReady = true
            if (!readyDeferred.isCompleted) {
                readyDeferred.complete(true)
            }
        }

        @JavascriptInterface
        fun onGeocodeSuccess(reqId: Long, jsonString: String) {
            val deferred = pendingRequests.remove(reqId) ?: return
            val suggestions = mutableListOf<AddressSuggestion>()
            try {
                val array = JSONArray(jsonString)
                for (i in 0 until array.length()) {
                    val item = array.getJSONObject(i)
                    val rawFull = item.optString("fullAddress", "")
                    val name = item.optString("name", "")
                    val desc = item.optString("description", "")
                    val lat = item.optDouble("lat", 0.0)
                    val lon = item.optDouble("lon", 0.0)
                    if (lat != 0.0 && lon != 0.0) {
                        val cleanedFull = cleanAddressText(if (rawFull.isNotBlank()) rawFull else name)
                        val subtitle = cleanSubtitle(desc, cleanedFull)
                        suggestions.add(
                            AddressSuggestion(
                                fullAddress = cleanedFull,
                                subtitle = subtitle,
                                lat = lat,
                                lon = lon
                            )
                        )
                    }
                }
            } catch (_: Exception) {
            }
            deferred.complete(suggestions)
        }

        @JavascriptInterface
        fun onGeocodeError(reqId: Long, error: String) {
            val deferred = pendingRequests.remove(reqId)
            deferred?.complete(emptyList())
        }
    }

    suspend fun geocode(context: Context, query: String, apiKey: String = ""): List<AddressSuggestion> {
        initialize(context, apiKey)

        if (!isYmapsReady) {
            val ready = withTimeoutOrNull(4500) { readyDeferred.await() }
            if (ready != true) {
                return emptyList()
            }
        }

        val reqId = requestIdCounter.incrementAndGet()
        val deferred = CompletableDeferred<List<AddressSuggestion>>()
        pendingRequests[reqId] = deferred

        withContext(Dispatchers.Main) {
            val escapedQuery = JSONObject.quote(query)
            webView?.evaluateJavascript("performGeocode($reqId, $escapedQuery);", null)
        }

        return withTimeoutOrNull(4500) {
            deferred.await()
        } ?: run {
            pendingRequests.remove(reqId)
            emptyList()
        }
    }

    fun cleanAddressText(address: String): String {
        return address
            .removePrefix("Россия, ")
            .removePrefix("Россия,")
            .removePrefix("Россия ")
            .trim()
    }

    fun cleanSubtitle(desc: String, fullAddress: String): String {
        val cleaned = desc
            .removePrefix("Россия, ")
            .removePrefix("Россия,")
            .removeSuffix(", Россия")
            .removeSuffix(", Санкт-Петербург")
            .trim()
        return if (cleaned.isNotBlank()) cleaned else fullAddress
    }
}
