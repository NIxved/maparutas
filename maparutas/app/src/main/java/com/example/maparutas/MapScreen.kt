package com.example.maparutas

import android.location.Location
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@Composable
fun MapScreen() {
    // Coordenadas del Parque Simón Bolívar
    val bolivarPark = LatLng(4.658768900734289, -74.0934688649813)

    // Estado de la cámara
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(bolivarPark, 14f)
    }

    // Lista de puntos de la ruta
    val routePoints = remember { mutableStateListOf<LatLng>() }

    // Distancia total
    var totalDistanceKm by remember { mutableStateOf(0.0) }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = false),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = true,
                zoomGesturesEnabled = true,
                rotationGesturesEnabled = true,
                tiltGesturesEnabled = true
            ),
            onMapLongClick = { latLng ->
                routePoints.add(latLng)
                totalDistanceKm = calculateTotalDistanceKm(routePoints)
            }
        ) {
            // Marcador de inicio (solo si hay al menos un punto)
            if (routePoints.isNotEmpty()) {
                Marker(
                    state = rememberMarkerState(position = routePoints.first()),
                    title = "Inicio",
                    icon = BitmapDescriptorFactory.fromResource(R.drawable.inicio)
                )
            }

            // Marcador de fin (solo si hay más de un punto)
            if (routePoints.size > 1) {
                Marker(
                    state = rememberMarkerState(position = routePoints.last()),
                    title = "Fin",
                    snippet = "Distancia: %.2f km".format(totalDistanceKm),
                    icon = BitmapDescriptorFactory.fromResource(R.drawable.fin)
                )
            }

            // Polyline de la ruta
            if (routePoints.size > 1) {
                val routeColor = when {
                    totalDistanceKm < 2.0 -> Color.Green
                    totalDistanceKm <= 5.0 -> Color.Yellow
                    else -> Color.Red
                }

                Polyline(
                    points = routePoints,
                    color = routeColor,
                    width = 20f
                )
            }
        }

        // Botón de reiniciar
        Button(
            onClick = {
                routePoints.clear()
                totalDistanceKm = 0.0
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6200EE)
            )
        ) {
            Text(
                text = "Reiniciar",
                color = Color.White
            )
        }
    }
}

/**
 * Función auxiliar para calcular la distancia total de la lista de coordenadas LatLng.
 * Utiliza Location.distanceBetween de Android.
 */
fun calculateTotalDistanceKm(points: List<LatLng>): Double {
    if (points.size < 2) return 0.0

    var totalDistanceMeters = 0.0
    val results = FloatArray(1)

    for (i in 0 until points.size - 1) {
        val p1 = points[i]
        val p2 = points[i + 1]

        Location.distanceBetween(p1.latitude, p1.longitude, p2.latitude, p2.longitude, results)
        totalDistanceMeters += results[0]
    }

    return totalDistanceMeters / 1000.0
}