package com.alfredorosas.vet2myfriend.domain.repository

import com.alfredorosas.vet2myfriend.domain.model.RouteRequest
import com.alfredorosas.vet2myfriend.domain.model.RouteResult

interface RoutingRepository {
    suspend fun calculate(request: RouteRequest): RouteResult
}
