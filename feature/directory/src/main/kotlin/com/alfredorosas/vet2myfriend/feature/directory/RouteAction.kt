package com.alfredorosas.vet2myfriend.feature.directory

import com.alfredorosas.vet2myfriend.domain.GeoPoint
import com.alfredorosas.vet2myfriend.domain.TravelMode
import com.alfredorosas.vet2myfriend.domain.model.Clinic
import com.alfredorosas.vet2myfriend.domain.model.PlaceCandidate

sealed interface RouteAction {
    data class SelectClinic(val clinic: Clinic, val mode: TravelMode) : RouteAction
    data class SelectMode(val mode: TravelMode) : RouteAction
    data class SelectOnMap(val target: MapSelection) : RouteAction
    data class MapPointSelected(val point: GeoPoint) : RouteAction
    data class OpenSearch(val target: MapSelection) : RouteAction
    data class SearchAddress(val address: String) : RouteAction
    data class AcceptCandidate(val candidate: PlaceCandidate) : RouteAction
    data object DismissSearch : RouteAction
    data object Calculate : RouteAction
    data object Close : RouteAction
}
