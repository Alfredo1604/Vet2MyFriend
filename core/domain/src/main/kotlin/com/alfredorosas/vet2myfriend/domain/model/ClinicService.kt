package com.alfredorosas.vet2myfriend.domain.model

import java.math.BigDecimal

/** Prices are in MXN: null means unknown, zero means reported as free. */
data class ClinicService(
    val price: BigDecimal?,
    val service: String,
    val doctor: String? = null,
) {
    init {
        require(service.isNotBlank())
        require(price == null || price.signum() >= 0)
        require(doctor == null || doctor.isNotBlank())
    }
}
