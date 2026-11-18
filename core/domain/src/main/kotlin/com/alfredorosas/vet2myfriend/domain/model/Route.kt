package com.alfredorosas.vet2myfriend.domain.model

import com.alfredorosas.vet2myfriend.domain.GeoPoint
import com.alfredorosas.vet2myfriend.domain.TravelMode

data class RouteRequest(val origin: GeoPoint, val destination: GeoPoint, val mode: TravelMode)

data class Route(
    val segments: List<List<GeoPoint>>,
    val distanceMeters: Double,
    val durationSeconds: Double,
) {
    init {
        require(segments.isNotEmpty() && segments.all { it.size >= 2 })
        require(distanceMeters.isFinite() && distanceMeters >= 0)
        require(durationSeconds.isFinite() && durationSeconds >= 0)
    }
}

enum class RemoteFailure { NOT_CONFIGURED, UNAUTHORIZED, QUOTA, NO_RESULTS, NETWORK, SERVICE, INVALID_RESPONSE }

sealed interface RouteResult {
    data class Success(val route: Route) : RouteResult
    data class Failure(val reason: RemoteFailure) : RouteResult
}

enum class PlacePrecision { ADDRESS, STREET, AREA, UNKNOWN }

data class PlaceCandidate(
    val label: String,
    val point: GeoPoint,
    val precision: PlacePrecision = PlacePrecision.UNKNOWN,
)

sealed interface PlaceSearchResult {
    data class Success(val candidates: List<PlaceCandidate>) : PlaceSearchResult
    data class Failure(val reason: RemoteFailure) : PlaceSearchResult
}
