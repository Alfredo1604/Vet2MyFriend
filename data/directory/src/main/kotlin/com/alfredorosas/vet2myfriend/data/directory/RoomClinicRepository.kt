package com.alfredorosas.vet2myfriend.data.directory

import com.alfredorosas.vet2myfriend.domain.repository.ClinicRepository
import com.alfredorosas.vet2myfriend.domain.model.Clinic
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomClinicRepository(private val directory: LocalClinicDirectory) : ClinicRepository {
    override fun observeClinics(): Flow<List<Clinic>> =
        directory.observeClinics().map { clinics ->
            clinics.map { Clinic(it.id, it.name, it.address, it.municipality, it.location, it.services) }
        }
}
