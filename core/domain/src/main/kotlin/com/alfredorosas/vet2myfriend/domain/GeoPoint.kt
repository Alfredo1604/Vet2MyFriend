package com.alfredorosas.vet2myfriend.domain

/** WGS84 latitude and longitude, in decimal degrees. */
data class GeoPoint(val latitude: Double, val longitude: Double) {
    init {
        require(latitude.isFinite() && latitude in -90.0..90.0)
        require(longitude.isFinite() && longitude in -180.0..180.0)
    }
}
