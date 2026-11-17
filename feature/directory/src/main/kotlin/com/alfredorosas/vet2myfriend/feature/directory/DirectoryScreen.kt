package com.alfredorosas.vet2myfriend.feature.directory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

data class ClinicListItem(
    val id: String,
    val name: String,
    val address: String,
)

sealed interface DirectoryUiState {
    data object Loading : DirectoryUiState
    data object Error : DirectoryUiState
    data class Ready(val clinics: List<ClinicListItem>) : DirectoryUiState
}

@Composable
fun DirectoryScreen(
    state: DirectoryUiState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize()) {
        Text(
            text = stringResource(R.string.directory_title),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
        )
        when (state) {
            DirectoryUiState.Loading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }

            DirectoryUiState.Error -> Column(Modifier.padding(horizontal = 20.dp)) {
                Text(stringResource(R.string.directory_error))
                TextButton(onClick = onRetry) { Text(stringResource(R.string.directory_retry)) }
            }

            is DirectoryUiState.Ready -> if (state.clinics.isEmpty()) {
                Text(
                    text = stringResource(R.string.directory_empty),
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(state.clinics, key = { it.id }) { clinic ->
                        Card(Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Text(clinic.name, style = MaterialTheme.typography.titleMedium)
                                Text(clinic.address, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}
