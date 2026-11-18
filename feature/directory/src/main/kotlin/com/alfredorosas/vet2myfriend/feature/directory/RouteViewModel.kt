package com.alfredorosas.vet2myfriend.feature.directory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alfredorosas.vet2myfriend.domain.GeoPoint
import com.alfredorosas.vet2myfriend.domain.TravelMode
import com.alfredorosas.vet2myfriend.domain.location.LocationProvider
import com.alfredorosas.vet2myfriend.domain.location.LocationResult
import com.alfredorosas.vet2myfriend.domain.model.Clinic
import com.alfredorosas.vet2myfriend.domain.model.PlaceCandidate
import com.alfredorosas.vet2myfriend.domain.model.PlaceSearchResult
import com.alfredorosas.vet2myfriend.domain.model.RouteRequest
import com.alfredorosas.vet2myfriend.domain.model.RouteResult
import com.alfredorosas.vet2myfriend.domain.repository.GeocodingRepository
import com.alfredorosas.vet2myfriend.domain.repository.RoutingRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** All provider calls are injected. UI events change state in one direction. */
class RouteViewModel(
    private val routing: RoutingRepository,
    private val geocoding: GeocodingRepository,
    private val location: LocationProvider,
) : ViewModel() {
    private val state = MutableStateFlow(RouteUiState())
    val uiState = state.asStateFlow()
    private var routeJob: Job? = null
    private var locationJob: Job? = null
    private var searchJob: Job? = null
    private var routeRevision = 0L
    private var locationRevision = 0L
    private var searchRevision = 0L

    fun onAction(action: RouteAction) {
        when (action) {
            is RouteAction.SelectClinic -> selectClinic(action.clinic, action.mode)
            is RouteAction.SelectMode -> selectMode(action.mode)
            is RouteAction.SelectOnMap -> selectOnMap(action.target)
            is RouteAction.MapPointSelected -> mapPointSelected(action.point)
            is RouteAction.OpenSearch -> openSearch(action.target)
            is RouteAction.SearchAddress -> searchAddress(action.address)
            is RouteAction.AcceptCandidate -> acceptCandidate(action.candidate)
            RouteAction.DismissSearch -> dismissSearch()
            RouteAction.Calculate -> calculate()
            RouteAction.Close -> close()
        }
    }

    fun selectClinic(clinic: Clinic, mode: TravelMode) {
        invalidateRoute()
        cancelLocation()
        dismissSearch()
        state.update {
            RouteUiState(clinic = clinic, origin = it.origin, originAccuracyMeters = it.originAccuracyMeters,
                destination = clinic.location, mode = mode)
        }
    }

    fun close() {
        invalidateRoute()
        cancelLocation()
        dismissSearch()
        state.update { RouteUiState(origin = it.origin, originAccuracyMeters = it.originAccuracyMeters) }
    }

    fun selectMode(mode: TravelMode) {
        if (state.value.mode == mode) return
        invalidateRoute()
        state.update { it.copy(mode = mode) }
    }

    fun selectOnMap(target: MapSelection) {
        dismissSearch()
        invalidateRoute()
        if (target == MapSelection.ORIGIN) cancelLocation()
        state.update { it.copy(selection = target) }
    }

    fun mapPointSelected(point: GeoPoint) {
        val target = state.value.selection
        if (target != MapSelection.NONE) setPoint(target, point)
    }

    fun openSearch(target: MapSelection) {
        dismissSearch()
        invalidateRoute()
        if (target == MapSelection.ORIGIN) cancelLocation()
        state.update { it.copy(searchTarget = target, selection = MapSelection.NONE, failure = null) }
    }

    fun dismissSearch() {
        searchRevision++
        searchJob?.cancel()
        state.update { it.copy(searchTarget = MapSelection.NONE, searching = false, candidates = emptyList()) }
    }

    fun searchAddress(address: String) {
        if (address.isBlank() || state.value.searchTarget == MapSelection.NONE) return
        searchJob?.cancel()
        val revision = ++searchRevision
        state.update { it.copy(searching = true, candidates = emptyList(), failure = null) }
        searchJob = viewModelScope.launch {
            val result = geocoding.search(address)
            if (revision != searchRevision) return@launch
            state.update { current ->
                when (result) {
                    is PlaceSearchResult.Success -> current.copy(searching = false, candidates = result.candidates)
                    is PlaceSearchResult.Failure -> current.copy(searching = false, failure = result.reason)
                }
            }
        }
    }

    fun acceptCandidate(candidate: PlaceCandidate) {
        if (candidate !in state.value.candidates) return
        val target = state.value.searchTarget
        dismissSearch()
        setPoint(target, candidate.point)
    }

    fun useCurrentLocation() {
        cancelLocation()
        val revision = locationRevision
        state.update { it.copy(locating = true, locationProblem = null) }
        locationJob = viewModelScope.launch {
            val result = location.currentLocation()
            if (revision != locationRevision) return@launch
            when (result) {
                is LocationResult.Success -> {
                    invalidateRoute()
                    state.update { it.copy(origin = result.point, originAccuracyMeters = result.accuracyMeters,
                        locating = false, locationProblem = null) }
                }
                LocationResult.PermissionRequired -> locationPermissionDenied()
                LocationResult.Unavailable -> state.update {
                    it.copy(locating = false, locationProblem = LocationProblem.UNAVAILABLE)
                }
            }
        }
    }

    fun locationPermissionDenied() {
        cancelLocation()
        state.update { it.copy(locating = false, locationProblem = LocationProblem.PERMISSION) }
    }

    fun calculate() {
        val current = state.value
        if (!current.canCalculate) return
        val request = RouteRequest(requireNotNull(current.origin), requireNotNull(current.destination), current.mode)
        invalidateRoute()
        val revision = routeRevision
        state.update { it.copy(calculating = true, selection = MapSelection.NONE) }
        routeJob = viewModelScope.launch {
            val result = routing.calculate(request)
            if (revision != routeRevision) return@launch
            state.update {
                when (result) {
                    is RouteResult.Success -> it.copy(calculating = false, route = result.route)
                    is RouteResult.Failure -> it.copy(calculating = false, failure = result.reason)
                }
            }
        }
    }

    private fun setPoint(target: MapSelection, point: GeoPoint) {
        if (target == MapSelection.NONE) return
        invalidateRoute()
        if (target == MapSelection.ORIGIN) cancelLocation()
        state.update {
            when (target) {
                MapSelection.ORIGIN -> it.copy(origin = point, originAccuracyMeters = null, locating = false,
                    locationProblem = null, selection = MapSelection.NONE)
                MapSelection.DESTINATION -> it.copy(destination = point, selection = MapSelection.NONE)
                MapSelection.NONE -> it
            }
        }
    }

    private fun invalidateRoute() {
        routeRevision++
        routeJob?.cancel()
        state.update { it.copy(route = null, calculating = false, failure = null) }
    }

    private fun cancelLocation() {
        locationRevision++
        locationJob?.cancel()
        state.update { it.copy(locating = false) }
    }
}
