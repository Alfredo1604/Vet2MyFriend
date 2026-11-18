package com.alfredorosas.vet2myfriend.feature.directory

import com.alfredorosas.vet2myfriend.domain.GeoPoint
import com.alfredorosas.vet2myfriend.domain.TravelMode
import com.alfredorosas.vet2myfriend.domain.model.Clinic
import com.alfredorosas.vet2myfriend.domain.model.PlaceCandidate
import com.alfredorosas.vet2myfriend.domain.model.RemoteFailure
import com.alfredorosas.vet2myfriend.domain.model.Route

enum class MapSelection { NONE, ORIGIN, DESTINATION }
enum class LocationProblem { PERMISSION, UNAVAILABLE }

data class RouteUiState(
    val clinic: Clinic? = null,
    val origin: GeoPoint? = null,
    val originAccuracyMeters: Float? = null,
    val destination: GeoPoint? = null,
    val mode: TravelMode = TravelMode.WALKING,
    val selection: MapSelection = MapSelection.NONE,
    val route: Route? = null,
    val calculating: Boolean = false,
    val locating: Boolean = false,
    val locationProblem: LocationProblem? = null,
    val failure: RemoteFailure? = null,
    val searchTarget: MapSelection = MapSelection.NONE,
    val searching: Boolean = false,
    val candidates: List<PlaceCandidate> = emptyList(),
) {
    val canCalculate: Boolean get() = origin != null && destination != null &&
        origin != destination && selection == MapSelection.NONE && !calculating && !locating && !searching
}
