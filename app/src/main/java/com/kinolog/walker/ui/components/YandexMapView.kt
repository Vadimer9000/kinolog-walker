package com.kinolog.walker.ui.components

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
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
            webViewClient = WebViewClient()
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

    LaunchedEffect(apiKey, pointsJson, routingMode) {
        val htmlContent = generateMapHtml(apiKey, pointsJson, routingMode)
        webView.loadDataWithBaseURL("https://api-maps.yandex.ru", htmlContent, "text/html", "UTF-8", null)
    }

    AndroidView(
        factory = { webView },
        modifier = modifier.fillMaxSize()
    )
}

private fun generateMapHtml(apiKey: String, pointsJson: String, routingMode: String): String {
    val keyParam = if (apiKey.isNotBlank()) "apikey=$apiKey&" else ""
    val routerType = if (routingMode == "mt") "masstransit" else "pedestrian"

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
                    background-color: #F8F6F1;
                    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
                }
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
                var pointsData = $pointsJson;
                var routingType = "$routerType";

                ymaps.ready(init);

                function init() {
                    var defaultCenter = [59.9343, 30.3351]; // St. Petersburg
                    var defaultZoom = 12;

                    if (pointsData.length > 0) {
                        defaultCenter = [pointsData[0].lat, pointsData[0].lon];
                    }

                    var map = new ymaps.Map("map", {
                        center: defaultCenter,
                        zoom: defaultZoom,
                        controls: ['zoomControl']
                    }, {
                        searchControlProvider: 'yandex#search'
                    });

                    var geoObjects = new ymaps.GeoObjectCollection();
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

                        geoObjects.add(placemark);
                    }

                    map.geoObjects.add(geoObjects);

                    // Build preview multi-route if there are 2 or more points
                    if (routePoints.length >= 2) {
                        var multiRoute = new ymaps.multiRouter.MultiRoute({
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
                        map.geoObjects.add(multiRoute);
                    } else if (pointsData.length > 0) {
                        map.setBounds(geoObjects.getBounds(), {
                            checkZoomRange: true,
                            zoomMargin: 60
                        });
                    }
                }
            </script>
        </body>
        </html>
    """.trimIndent()
}
