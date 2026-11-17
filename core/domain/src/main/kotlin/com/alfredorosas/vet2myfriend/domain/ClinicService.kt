package com.alfredorosas.vet2myfriend.domain

import java.math.BigDecimal

data class ClinicService(
    val price: BigDecimal,
    val service: String,
    val doctor: String
)

