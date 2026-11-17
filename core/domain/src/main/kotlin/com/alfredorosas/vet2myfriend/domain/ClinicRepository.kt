package com.alfredorosas.vet2myfriend.domain

import kotlinx.coroutines.flow.Flow

data class ClinicSummary(val id: String, val name: String, val address: String)

interface ClinicRepository {
    fun observeClinics(): Flow<List<ClinicSummary>>
}