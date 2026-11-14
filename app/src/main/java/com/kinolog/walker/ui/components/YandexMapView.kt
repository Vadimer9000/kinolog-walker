package com.kinolog.walker.ui.components

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.kinolog.walker.ui.theme.LocalIsDarkTheme
import org.json.JSONArray
import org.json.JSONObject

data class MapMarkerPoint(
    val number: Int,
    val timeLabel: String,
    val title: String,
    val address: String,
    val latitude: Double,
    val longitude: Double
)

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun YandexMapView(
    apiKey: String,
    points: List<MapMarkerPoint>,
    routingMode: String, // "pd" (pedestrian) or "mt" (masstransit)
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDark = LocalIsDarkTheme.current
    var isPageLoaded by remember { mutableStateOf(false) }

    val webView = remember {
        WebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                databaseEnabled = true
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                cacheMode = WebSettings.LOAD_DEFAULT
            }
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    isPageLoaded = true
                }
            }
        }
    }

    val pointsJson = remember(points) {
        val jsonArray = JSONArray()
        points.forEach { pt ->
            val obj = JSONObject()
            obj.put("number", pt.number)
            obj.put("time", pt.timeLabel)
            obj.put("title", pt.title)
            obj.put("address", pt.address)
            obj.put("lat", pt.latitude)
            obj.put("lon", pt.longitude)
            jsonArray.put(obj)
        }
        jsonArray.toString()
    }

    // Load base HTML only when API key or theme changes
    LaunchedEffect(apiKey, isDark) {
        isPageLoaded = false
        val htmlContent = generateMapHtml(apiKey, pointsJson, routingMode, isDark)
        webView.loadDataWithBaseURL("https://api-maps.yandex.ru", htmlContent, "text/html", "UTF-8", null)
    }

    // Smoothly update points and routes via JS evaluateJavascript without full page reloads
    LaunchedEffect(pointsJson, routingMode, isPageLoaded) {
        if (isPageLoaded) {
            val routerType = if (routingMode == "mt") "masstransit" else "pedestrian"
            val js = "if (window.updatePointsAndRoute) { window.updatePointsAndRoute($pointsJson, '$routerType'); }"
            webView.evaluateJavascript(js, null)
        }
    }

    AndroidView(
        factory = { webView },
        modifier = modifier.fillMaxSize()
    )
}

private fun generateMapHtml(apiKey: String, pointsJson: String, routingMode: String, isDark: Boolean): String {
    val keyParam = if (apiKey.isNotBlank()) "apikey=$apiKey&" else ""
    val routerType = if (routingMode == "mt") "masstransit" else "pedestrian"
    val bgColor = if (isDark) "#161D19" else "#F8F6F1"
    val darkFilter = if (isDark) """
        #map [class*="ground-pane"] {
            filter: invert(90%) hue-rotate(180deg) brightness(85%) contrast(90%);
        }
    """.trimIndent() else ""

    return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8" />
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
            <title>Yandex Map</title>
            <script src="https://api-maps.yandex.ru/2.1/?${keyParam}lang=ru_RU" type="text/javascript"></script>
            <style>
                html, body, #map {
                    width: 100%;
                    height: 100%;
                    padding: 0;
                    margin: 0;
                    background-color: $bgColor;
                    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
                }
                $darkFilter
                .custom-pin {
                    background: #5E9E7E;
                    color: #FFFFFF;
                    border-radius: 16px;
                    padding: 4px 8px;
                    font-size: 11px;
                    font-weight: bold;
                    white-space: nowrap;
                    box-shadow: 0 2px 6px rgba(0,0,0,0.25);
                    border: 1px solid rgba(255,255,255,0.8);
                }
            </style>
        </head>
        <body>
            <div id="map"></div>
            <script type="text/javascript">
                var map = null;
                var currentGeoObjects = null;
                var currentMultiRoute = null;
                var pendingPoints = $pointsJson;
                var pendingRoutingType = "$routerType";
                var isReady = false;

                window.updatePointsAndRoute = function(pointsData, routingType) {
                    if (!isReady || !map) {
                        pendingPoints = pointsData;
                        pendingRoutingType = routingType;
                        return;
                    }
                    if (currentGeoObjects) {
                        map.geoObjects.remove(currentGeoObjects);
                        currentGeoObjects = null;
                    }
                    if (currentMultiRoute) {
                        map.geoObjects.remove(currentMultiRoute);
                        currentMultiRoute = null;
                    }

                    currentGeoObjects = new ymaps.GeoObjectCollection();
                    var routePoints = [];

                    for (var i = 0; i < pointsData.length; i++) {
                        var p = pointsData[i];
                        var coords = [p.lat, p.lon];
                        routePoints.push(coords);

                        var placemark = new ymaps.Placemark(coords, {
                            iconContent: '<div style="font-size:11px; font-weight:bold; color:#10281E;">' + p.number + '</div>',
                            balloonContentHeader: '<b>' + p.time + ' — ' + p.title + '</b>',
                            balloonContentBody: p.address,
                            hintContent: p.time + ' ' + p.title
                        }, {
                            preset: 'islands#darkGreenCircleIcon'
                        });

                        currentGeoObjects.add(placemark);
                    }

                    map.geoObjects.add(currentGeoObjects);

                    if (routePoints.length >= 2) {
                        currentMultiRoute = new ymaps.multiRouter.MultiRoute({
                            referencePoints: routePoints,
                            params: {
                                routingMode: routingType,
                                results: 1
                            }
                        }, {
                            boundsAutoApply: true,
                            routeActiveStrokeWidth: 5,
                            routeActiveStrokeColor: "#5E9E7ECC"
                        });
                        map.geoObjects.add(currentMultiRoute);
                    } else if (pointsData.length > 0) {
                        map.setBounds(currentGeoObjects.getBounds(), {
                            checkZoomRange: true,
                            zoomMargin: 60
                        });
                    }
                };

                ymaps.ready(init);

                function init() {
                    var defaultCenter = [59.9343, 30.3351]; // St. Petersburg
                    var defaultZoom = 12;

                    if (pendingPoints && pendingPoints.length > 0) {
                        defaultCenter = [pendingPoints[0].lat, pendingPoints[0].lon];
                    }

                    map = new ymaps.Map("map", {
                        center: defaultCenter,
                        zoom: defaultZoom,
                        controls: ['zoomControl']
                    }, {
                        searchControlProvider: 'yandex#search'
                    });

                    isReady = true;
                    window.updatePointsAndRoute(pendingPoints, pendingRoutingType);
                }
            </script>
        </body>
        </html>
    """.trimIndent()
}
