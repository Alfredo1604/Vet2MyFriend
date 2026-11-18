package com.alfredorosas.vet2myfriend.feature.directory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.alfredorosas.vet2myfriend.domain.TravelMode
import com.alfredorosas.vet2myfriend.domain.model.Clinic
import java.text.NumberFormat
import java.util.Locale

sealed interface DirectoryUiState {
    data object Loading : DirectoryUiState
    data object Error : DirectoryUiState
    data class Ready(val clinics: List<Clinic>) : DirectoryUiState
}

@Composable
fun DirectoryScreen(
    state: DirectoryUiState,
    onRetry: () -> Unit,
    mapsConfigured: Boolean,
    routeState: RouteUiState,
    onRouteAction: (RouteAction) -> Unit,
    onRequestLocation: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showMap by rememberSaveable { mutableStateOf(true) }
    var detailId by rememberSaveable { mutableStateOf<String?>(null) }
    val onDirections: (Clinic, TravelMode) -> Unit = { clinic, mode ->
        onRouteAction(RouteAction.SelectClinic(clinic, mode))
        showMap = true
    }
    Column(modifier.fillMaxSize()) {
        PrimaryTabRow(selectedTabIndex = if (showMap) 0 else 1) {
            Tab(selected = showMap, onClick = { showMap = true }, text = {
                Text(stringResource(R.string.directory_map))
            })
            Tab(selected = !showMap, onClick = { showMap = false }, text = {
                Text(stringResource(R.string.directory_list))
            })
        }
        when (state) {
            DirectoryUiState.Loading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }

            DirectoryUiState.Error -> Column(Modifier.padding(20.dp)) {
                Text(stringResource(R.string.directory_error))
                TextButton(onClick = onRetry) { Text(stringResource(R.string.directory_retry)) }
            }

            is DirectoryUiState.Ready -> when {
                !showMap -> ClinicList(state.clinics, onDirections) { detailId = it.id }
                mapsConfigured -> {
                    ClinicMap(state.clinics, routeState, { onRouteAction(RouteAction.MapPointSelected(it)) },
                        { detailId = it.id }, Modifier.weight(1f))
                    if (routeState.clinic != null) {
                        RoutePlanner(routeState, onRouteAction, onRequestLocation)
                    } else {
                        TextButton(onClick = { showMap = false }) {
                            Text(stringResource(R.string.choose_clinic))
                        }
                    }
                }
                else -> Column(Modifier.padding(20.dp)) {
                    Text(stringResource(R.string.map_unavailable))
                    TextButton(onClick = { showMap = false }) {
                        Text(stringResource(R.string.directory_show_list))
                    }
                }
            }
        }
    }
    if (state is DirectoryUiState.Ready) {
        state.clinics.find { it.id == detailId }?.let { clinic ->
            ClinicDetails(clinic, onDismiss = { detailId = null }, onRoute = {
                detailId = null
                onDirections(clinic, TravelMode.WALKING)
            })
        }
    }
    if (routeState.searchTarget != MapSelection.NONE) AddressSearchDialog(routeState, onRouteAction)
}

@Composable
private fun ClinicList(
    clinics: List<Clinic>,
    onDirections: (Clinic, TravelMode) -> Unit,
    onDetails: (Clinic) -> Unit,
) {
    if (clinics.isEmpty()) {
        Text(
            text = stringResource(R.string.directory_empty),
            modifier = Modifier.padding(20.dp),
        )
        return
    }
    LazyColumn(
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(clinics, key = { it.id }) { clinic ->
            Card(Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(clinic.name, style = MaterialTheme.typography.titleMedium)
                    Text(clinic.address, style = MaterialTheme.typography.bodyMedium)
                    TextButton(onClick = { onDetails(clinic) }) {
                        Text(stringResource(R.string.clinic_details))
                    }
                    Text(
                        stringResource(R.string.directions_open),
                        style = MaterialTheme.typography.labelMedium,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        TextButton(onClick = { onDirections(clinic, TravelMode.WALKING) }) {
                            Text(stringResource(R.string.directions_walk))
                        }
                        TextButton(onClick = { onDirections(clinic, TravelMode.CYCLING) }) {
                            Text(stringResource(R.string.directions_cycle))
                        }
                        TextButton(onClick = { onDirections(clinic, TravelMode.DRIVING) }) {
                            Text(stringResource(R.string.directions_drive))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ClinicDetails(clinic: Clinic, onDismiss: () -> Unit, onRoute: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(clinic.name) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(clinic.address)
                Text(clinic.municipality)
                Text(stringResource(R.string.clinic_services), style = MaterialTheme.typography.titleSmall)
                if (clinic.services.isEmpty()) Text(stringResource(R.string.services_unknown))
                for (service in clinic.services) {
                    val amount = service.price
                    val price = when {
                        amount == null -> stringResource(R.string.price_unknown)
                        amount.signum() == 0 -> stringResource(R.string.price_free)
                        else -> NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-MX")).format(amount)
                    }
                    Text("${service.service}: $price")
                    service.doctor?.let { Text(stringResource(R.string.service_doctor, it)) }
                }
                Text(stringResource(R.string.services_reported), style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = { TextButton(onClick = onRoute) { Text(stringResource(R.string.route_plan)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.close)) } },
    )
}
