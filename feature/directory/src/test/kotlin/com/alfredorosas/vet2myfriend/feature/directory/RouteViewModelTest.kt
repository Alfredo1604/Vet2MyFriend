package com.alfredorosas.vet2myfriend.feature.directory

import com.alfredorosas.vet2myfriend.domain.GeoPoint
import com.alfredorosas.vet2myfriend.domain.TravelMode
import com.alfredorosas.vet2myfriend.domain.location.LocationProvider
import com.alfredorosas.vet2myfriend.domain.location.LocationResult
import com.alfredorosas.vet2myfriend.domain.model.*
import com.alfredorosas.vet2myfriend.domain.repository.GeocodingRepository
import com.alfredorosas.vet2myfriend.domain.repository.RoutingRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.test.*
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RouteViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val origin = GeoPoint(25.67, -100.25)
    private val destination = GeoPoint(25.68, -100.24)
    private val clinic = Clinic("clinic", "Centro", "Dirección", "Guadalupe", destination)
    private val route = Route(listOf(listOf(origin, destination)), 1200.0, 900.0)
    private var requests = 0
    private var calculate: suspend (RouteRequest) -> RouteResult = { requests++; RouteResult.Success(route) }
    private var locate: suspend () -> LocationResult = { LocationResult.PermissionRequired }
    private var search: suspend (String) -> PlaceSearchResult = { PlaceSearchResult.Success(emptyList()) }
    private fun model() = RouteViewModel(
        object : RoutingRepository { override suspend fun calculate(request: RouteRequest) = calculate.invoke(request) },
        object : GeocodingRepository { override suspend fun search(address: String) = search.invoke(address) },
        object : LocationProvider { override suspend fun currentLocation() = locate.invoke() },
    )

    @Before fun setup() { Dispatchers.setMain(dispatcher) }
    @After fun cleanup() { Dispatchers.resetMain() }

    @Test fun requiresTwoDifferentPointsAndExplicitCalculation() = runTest {
        val vm = model()
        vm.selectClinic(clinic, TravelMode.WALKING)
        vm.calculate()
        runCurrent()
        assertEquals(0, requests)
        vm.selectOnMap(MapSelection.ORIGIN)
        vm.mapPointSelected(destination)
        assertFalse(vm.uiState.value.canCalculate)
        vm.selectOnMap(MapSelection.ORIGIN)
        vm.mapPointSelected(origin)
        runCurrent()
        assertEquals(0, requests)
        vm.calculate()
        advanceUntilIdle()
        assertEquals(1, requests)
        assertEquals(route, vm.uiState.value.route)
        vm.selectMode(TravelMode.CYCLING)
        assertNull(vm.uiState.value.route)
        assertEquals(1, requests)
    }

    @Test fun changingModeDiscardsAnOlderResponseEvenIfProviderIgnoresCancellation() = runTest {
        val pending = CompletableDeferred<RouteResult>()
        calculate = { withContext(NonCancellable) { pending.await() } }
        val vm = model()
        vm.selectClinic(clinic, TravelMode.WALKING)
        vm.selectOnMap(MapSelection.ORIGIN)
        vm.mapPointSelected(origin)
        vm.calculate()
        runCurrent()
        vm.selectMode(TravelMode.CYCLING)
        pending.complete(RouteResult.Success(route))
        advanceUntilIdle()
        assertEquals(TravelMode.CYCLING, vm.uiState.value.mode)
        assertNull(vm.uiState.value.route)
        assertFalse(vm.uiState.value.calculating)
    }

    @Test fun deniedLocationStillAllowsManualOrigin() = runTest {
        val vm = model()
        vm.selectClinic(clinic, TravelMode.WALKING)
        vm.useCurrentLocation()
        advanceUntilIdle()
        assertEquals(LocationProblem.PERMISSION, vm.uiState.value.locationProblem)
        vm.selectOnMap(MapSelection.ORIGIN)
        vm.mapPointSelected(origin)
        assertTrue(vm.uiState.value.canCalculate)
        assertNull(vm.uiState.value.locationProblem)
    }

    @Test fun lateGpsResultCannotOverwriteAManuallyChosenOrigin() = runTest {
        val pending = CompletableDeferred<LocationResult>()
        locate = { withContext(NonCancellable) { pending.await() } }
        val vm = model()
        vm.selectClinic(clinic, TravelMode.WALKING)
        vm.useCurrentLocation()
        runCurrent()
        vm.selectOnMap(MapSelection.ORIGIN)
        vm.mapPointSelected(origin)
        pending.complete(LocationResult.Success(destination, 5f))
        advanceUntilIdle()
        assertEquals(origin, vm.uiState.value.origin)
    }

    @Test fun choosingManualOriginKeepsMapSelectionActiveWhenGpsArrivesLate() = runTest {
        val pending = CompletableDeferred<LocationResult>()
        locate = { withContext(NonCancellable) { pending.await() } }
        val vm = model()
        vm.selectClinic(clinic, TravelMode.WALKING)
        vm.useCurrentLocation()
        runCurrent()
        vm.selectOnMap(MapSelection.ORIGIN)
        pending.complete(LocationResult.Success(destination, 5f))
        advanceUntilIdle()
        assertEquals(MapSelection.ORIGIN, vm.uiState.value.selection)
        assertNull(vm.uiState.value.origin)
    }

    @Test fun addressCandidatesRequireExplicitAcceptance() = runTest {
        val candidate = PlaceCandidate("Centro", destination)
        search = { PlaceSearchResult.Success(listOf(candidate)) }
        val vm = model()
        vm.selectClinic(clinic.copy(location = null), TravelMode.WALKING)
        vm.openSearch(MapSelection.DESTINATION)
        vm.searchAddress("Centro Guadalupe")
        advanceUntilIdle()
        assertNull(vm.uiState.value.destination)
        vm.acceptCandidate(candidate)
        assertEquals(destination, vm.uiState.value.destination)
        assertEquals(MapSelection.NONE, vm.uiState.value.searchTarget)
    }
}
