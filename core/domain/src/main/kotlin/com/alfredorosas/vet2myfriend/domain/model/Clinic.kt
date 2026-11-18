package com.alfredorosas.vet2myfriend.domain.model

import com.alfredorosas.vet2myfriend.domain.GeoPoint

/** Public information about a clinic; location is unknown until coordinates are verified. */
data class Clinic(
    val id: String,
    val name: String,
    val address: String,
    val municipality: String,
    val location: GeoPoint? = null,
    val services: List<ClinicService> = emptyList(),
) {
    init {
        require(id.isNotBlank())
        require(name.isNotBlank())
        require(address.isNotBlank())
        require(municipality.isNotBlank())
    }
}
