package com.alfredorosas.vet2myfriend.domain.location

import com.alfredorosas.vet2myfriend.domain.GeoPoint

interface LocationProvider {
    suspend fun currentLocation(): LocationResult
}

sealed interface LocationResult {
    data class Success(val point: GeoPoint, val accuracyMeters: Float) : LocationResult
    data object PermissionRequired : LocationResult
    data object Unavailable : LocationResult
}
