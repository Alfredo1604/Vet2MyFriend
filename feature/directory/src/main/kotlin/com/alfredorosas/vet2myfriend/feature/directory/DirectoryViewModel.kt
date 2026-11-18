package com.alfredorosas.vet2myfriend.feature.directory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alfredorosas.vet2myfriend.domain.repository.ClinicRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class DirectoryViewModel(private val repository: ClinicRepository) : ViewModel() {
    private val state = MutableStateFlow<DirectoryUiState>(DirectoryUiState.Loading)
    val uiState = state.asStateFlow()
    private var observation: Job? = null

    init {
        retry()
    }

    fun retry() {
        observation?.cancel()
        state.value = DirectoryUiState.Loading
        observation = viewModelScope.launch {
            repository.observeClinics()
                .catch { state.value = DirectoryUiState.Error }
                .collect { clinics ->
                    state.value = DirectoryUiState.Ready(
                        clinics,
                    )
                }
        }
    }
}
