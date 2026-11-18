package com.alfredorosas.vet2myfriend.feature.directory

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.alfredorosas.vet2myfriend.domain.GeoPoint
import com.alfredorosas.vet2myfriend.domain.model.Clinic
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState

@Composable
internal fun ClinicMap(
    clinics: List<Clinic>,
    routeState: RouteUiState,
    onPointSelected: (GeoPoint) -> Unit,
    onClinicSelected: (Clinic) -> Unit,
    modifier: Modifier = Modifier,
) {
    val camera = rememberCameraPositionState {
        // Regional viewport only, not a verified clinic location.
        position = CameraPosition.fromLatLngZoom(LatLng(25.67, -100.25), 12f)
    }
    var loaded by remember { mutableStateOf(false) }
    LaunchedEffect(loaded, routeState.route, routeState.origin, routeState.destination) {
        if (!loaded) return@LaunchedEffect
        val points = routeState.route?.segments?.flatten()
            ?: listOfNotNull(routeState.origin, routeState.destination).distinct()
        if (points.size > 1) {
            val bounds = LatLngBounds.builder().apply { points.forEach { include(it.toLatLng()) } }.build()
            camera.animate(CameraUpdateFactory.newLatLngBounds(bounds, 64))
        } else {
            (routeState.destination ?: routeState.origin)?.let {
                camera.animate(CameraUpdateFactory.newLatLngZoom(it.toLatLng(), 15f))
            }
        }
    }
    Box(modifier) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = camera,
            uiSettings = MapUiSettings(mapToolbarEnabled = false),
            onMapLoaded = { loaded = true },
            onMapClick = { onPointSelected(GeoPoint(it.latitude, it.longitude)) },
        ) {
            for (clinic in clinics) {
                val point = clinic.location ?: continue
                key(clinic.id) {
                    Marker(
                        state = rememberUpdatedMarkerState(point.toLatLng()),
                        title = clinic.name,
                        snippet = clinic.address,
                        onClick = { onClinicSelected(clinic); true },
                    )
                }
            }
            routeState.origin?.let {
                Marker(state = rememberUpdatedMarkerState(it.toLatLng()), title = stringResource(R.string.route_origin),
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
            }
            routeState.destination?.let {
                Marker(state = rememberUpdatedMarkerState(it.toLatLng()), title = stringResource(R.string.route_destination))
            }
            routeState.route?.segments?.forEach { segment ->
                Polyline(points = segment.map { it.toLatLng() }, color = MaterialTheme.colorScheme.primary, width = 12f)
            }
        }
        val notice = when (routeState.selection) {
            MapSelection.ORIGIN -> R.string.tap_origin
            MapSelection.DESTINATION -> R.string.tap_destination
            MapSelection.NONE -> if (routeState.clinic == null && clinics.any { it.location == null }) {
                R.string.map_locations_pending
            } else null
        }
        if (notice != null) {
            Surface(Modifier.align(Alignment.TopCenter).padding(12.dp), shape = MaterialTheme.shapes.small,
                tonalElevation = 3.dp) {
                Text(stringResource(notice), Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

private fun GeoPoint.toLatLng() = LatLng(latitude, longitude)
