package com.alfredorosas.vet2myfriend.data.directory

import com.alfredorosas.vet2myfriend.domain.ClinicRepository
import com.alfredorosas.vet2myfriend.domain.ClinicSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomClinicRepository(private val directory: LocalClinicDirectory) : ClinicRepository {
    override fun observeClinics(): Flow<List<ClinicSummary>> =
        directory.observeClinics().map { clinics ->
            clinics.map { ClinicSummary(it.id, it.name, it.address) }
        }
}
