package com.alfredorosas.vet2myfriend

import android.app.Application
import com.alfredorosas.vet2myfriend.data.directory.LocalClinicDirectory
import com.alfredorosas.vet2myfriend.data.directory.RoomClinicRepository
import com.alfredorosas.vet2myfriend.domain.repository.ClinicRepository
import com.alfredorosas.vet2myfriend.domain.repository.RoutingRepository
import com.alfredorosas.vet2myfriend.domain.repository.GeocodingRepository
import com.alfredorosas.vet2myfriend.domain.location.LocationProvider
import com.alfredorosas.vet2myfriend.data.routing.GeoapifyRepositories

class Vet2MyFriendApplication : Application() {
    private val geoapify by lazy { GeoapifyRepositories.create(BuildConfig.GEOAPIFY_API_KEY) }
    val routingRepository: RoutingRepository get() = geoapify.routing
    val geocodingRepository: GeocodingRepository get() = geoapify.geocoding
    val locationProvider: LocationProvider by lazy { AndroidLocationProvider(this) }
    val clinicRepository: ClinicRepository by lazy {
        RoomClinicRepository(LocalClinicDirectory.open(this))
    }
}
