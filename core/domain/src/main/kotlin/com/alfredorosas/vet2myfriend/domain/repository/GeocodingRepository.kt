package com.alfredorosas.vet2myfriend.domain.repository

import com.alfredorosas.vet2myfriend.domain.model.PlaceSearchResult

interface GeocodingRepository {
    suspend fun search(address: String): PlaceSearchResult
}
