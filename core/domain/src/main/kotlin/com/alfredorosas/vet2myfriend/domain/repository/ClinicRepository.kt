package com.alfredorosas.vet2myfriend.domain.repository

import com.alfredorosas.vet2myfriend.domain.model.Clinic
import kotlinx.coroutines.flow.Flow

interface ClinicRepository {
    fun observeClinics(): Flow<List<Clinic>>
}
