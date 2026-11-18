package com.alfredorosas.vet2myfriend.feature.directory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.alfredorosas.vet2myfriend.domain.TravelMode
import com.alfredorosas.vet2myfriend.domain.model.RemoteFailure
import com.alfredorosas.vet2myfriend.domain.model.PlacePrecision
import java.text.NumberFormat
import kotlin.math.ceil

@Composable
internal fun RoutePlanner(state: RouteUiState, onAction: (RouteAction) -> Unit, onRequestLocation: () -> Unit) {
    Column(Modifier.fillMaxWidth().heightIn(max = 300.dp)) {
        Column(Modifier.weight(1f, fill = false).fillMaxWidth().verticalScroll(rememberScrollState()).padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(state.clinic?.name.orEmpty(), style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                TextButton(onClick = { onAction(RouteAction.Close) }) { Text(stringResource(R.string.close)) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TravelMode.entries.forEach { mode ->
                    FilterChip(selected = state.mode == mode, onClick = { onAction(RouteAction.SelectMode(mode)) },
                        label = { Text(stringResource(mode.label())) })
                }
            }
            Text(stringResource(if (state.origin == null) R.string.origin_missing else R.string.origin_selected))
            state.originAccuracyMeters?.let {
                Text(stringResource(R.string.location_accuracy, ceil(it.toDouble()).toInt()), style = MaterialTheme.typography.bodySmall)
            }
            Row {
                TextButton(onClick = onRequestLocation, enabled = !state.locating) {
                    Text(stringResource(if (state.locating) R.string.location_loading else R.string.use_location))
                }
                TextButton(onClick = { onAction(RouteAction.SelectOnMap(MapSelection.ORIGIN)) }) {
                    Text(stringResource(R.string.choose_on_map))
                }
                TextButton(onClick = { onAction(RouteAction.OpenSearch(MapSelection.ORIGIN)) }) {
                    Text(stringResource(R.string.address))
                }
            }
            Text(stringResource(if (state.destination == null) R.string.destination_missing else R.string.destination_selected))
            Row {
                TextButton(onClick = { onAction(RouteAction.OpenSearch(MapSelection.DESTINATION)) }) {
                    Text(stringResource(R.string.search_clinic_address))
                }
                TextButton(onClick = { onAction(RouteAction.SelectOnMap(MapSelection.DESTINATION)) }) {
                    Text(stringResource(R.string.choose_on_map))
                }
            }
            if (state.locationProblem != null) {
                Text(stringResource(if (state.locationProblem == LocationProblem.PERMISSION)
                    R.string.location_permission else R.string.location_unavailable), color = MaterialTheme.colorScheme.error)
            }
            if (state.failure != null && state.searchTarget == MapSelection.NONE) RemoteError(state.failure)
            state.route?.let {
                val distance = NumberFormat.getNumberInstance().apply { maximumFractionDigits = 1 }.format(it.distanceMeters / 1000)
                val minutes = ceil(it.durationSeconds / 60).toInt()
                Text(stringResource(R.string.route_summary, distance, minutes), style = MaterialTheme.typography.titleSmall)
                Text(stringResource(R.string.route_estimate), style = MaterialTheme.typography.bodySmall)
            }
            Text(stringResource(R.string.route_location_notice), style = MaterialTheme.typography.bodySmall)
        }
        Button(onClick = { onAction(RouteAction.Calculate) }, enabled = state.canCalculate,
            modifier = Modifier.padding(horizontal = 12.dp)) {
            Text(stringResource(if (state.calculating) R.string.route_loading else R.string.route_calculate))
        }
        RoutingAttribution()
    }
}

@Composable
internal fun AddressSearchDialog(state: RouteUiState, onAction: (RouteAction) -> Unit) {
    var address by rememberSaveable(state.searchTarget, state.clinic?.id) {
        mutableStateOf(if (state.searchTarget == MapSelection.DESTINATION) state.clinic?.address.orEmpty() else "")
    }
    AlertDialog(
        onDismissRequest = { onAction(RouteAction.DismissSearch) },
        title = { Text(stringResource(R.string.search_address)) },
        text = {
            Column(Modifier.heightIn(max = 360.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = address, onValueChange = { address = it },
                    label = { Text(stringResource(R.string.address)) }, modifier = Modifier.fillMaxWidth())
                Text(stringResource(R.string.address_confirmation), style = MaterialTheme.typography.bodySmall)
                TextButton(onClick = { onAction(RouteAction.SearchAddress(address)) },
                    enabled = address.isNotBlank() && !state.searching) { Text(stringResource(R.string.search)) }
                if (state.searching) CircularProgressIndicator()
                state.failure?.let { RemoteError(it) }
                state.candidates.forEach { candidate ->
                    TextButton(onClick = { onAction(RouteAction.AcceptCandidate(candidate)) }) {
                        Column {
                            Text(candidate.label)
                            Text(stringResource(when (candidate.precision) {
                                PlacePrecision.ADDRESS -> R.string.address_point
                                PlacePrecision.STREET -> R.string.address_street
                                PlacePrecision.AREA, PlacePrecision.UNKNOWN -> R.string.address_approximate
                            }), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                RoutingAttribution()
            }
        },
        confirmButton = {
            TextButton(onClick = { onAction(RouteAction.DismissSearch) }) { Text(stringResource(R.string.close)) }
        },
    )
}

@Composable
private fun RoutingAttribution() {
    val links = LocalUriHandler.current
    Row {
        TextButton(onClick = { links.openUri("https://www.geoapify.com/") }) { Text(stringResource(R.string.geoapify_attribution)) }
        TextButton(onClick = { links.openUri("https://www.openstreetmap.org/copyright") }) { Text(stringResource(R.string.osm_attribution)) }
    }
}

@Composable
private fun RemoteError(reason: RemoteFailure) {
    val message = when (reason) {
        RemoteFailure.NOT_CONFIGURED -> R.string.route_not_configured
        RemoteFailure.UNAUTHORIZED -> R.string.route_unauthorized
        RemoteFailure.QUOTA -> R.string.route_quota
        RemoteFailure.NO_RESULTS -> R.string.route_no_results
        RemoteFailure.NETWORK -> R.string.route_network
        RemoteFailure.SERVICE, RemoteFailure.INVALID_RESPONSE -> R.string.route_service
    }
    Text(stringResource(message), color = MaterialTheme.colorScheme.error)
}

private fun TravelMode.label() = when (this) {
    TravelMode.WALKING -> R.string.directions_walk
    TravelMode.CYCLING -> R.string.directions_cycle
    TravelMode.DRIVING -> R.string.directions_drive
}
