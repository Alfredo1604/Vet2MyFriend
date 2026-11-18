package com.alfredorosas.vet2myfriend.data.routing

import com.alfredorosas.vet2myfriend.domain.TravelMode
import com.alfredorosas.vet2myfriend.domain.model.PlaceSearchResult
import com.alfredorosas.vet2myfriend.domain.model.RemoteFailure
import com.alfredorosas.vet2myfriend.domain.model.RouteRequest
import com.alfredorosas.vet2myfriend.domain.model.RouteResult
import com.alfredorosas.vet2myfriend.domain.repository.GeocodingRepository
import com.alfredorosas.vet2myfriend.domain.repository.RoutingRepository
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import java.io.IOException
import java.util.concurrent.TimeUnit

/** One shared HTTP client; callers depend on domain contracts rather than this provider. */
class GeoapifyRepositories private constructor(
    val routing: RoutingRepository,
    val geocoding: GeocodingRepository,
) {
    companion object {
        fun create(apiKey: String): GeoapifyRepositories {
            val http = OkHttpClient.Builder()
                .callTimeout(20, TimeUnit.SECONDS)
                .retryOnConnectionFailure(false)
                .followRedirects(false)
                .followSslRedirects(false)
                .build()
            val client = GeoapifyClient(apiKey, http, "https://api.geoapify.com/".toHttpUrl())
            return GeoapifyRepositories(GeoapifyRoutingRepository(client), GeoapifyGeocodingRepository(client))
        }
    }
}

internal class GeoapifyRoutingRepository(private val client: GeoapifyClient) : RoutingRepository {
    override suspend fun calculate(request: RouteRequest): RouteResult = try {
        val mode = when (request.mode) {
            TravelMode.WALKING -> "walk"
            TravelMode.CYCLING -> "bicycle"
            TravelMode.DRIVING -> "drive"
        }
        val from = "${request.origin.latitude},${request.origin.longitude}"
        val to = "${request.destination.latitude},${request.destination.longitude}"
        val body = client.get("v1/routing", mapOf("waypoints" to "$from|$to", "mode" to mode))
        RouteResult.Success(GeoapifyJson.route(body.requireSuccess()))
    } catch (e: Exception) {
        RouteResult.Failure(e.toRemoteFailure())
    }
}

internal class GeoapifyGeocodingRepository(private val client: GeoapifyClient) : GeocodingRepository {
    override suspend fun search(address: String): PlaceSearchResult = try {
        require(address.isNotBlank())
        val body = client.get(
            "v1/geocode/search",
            mapOf("text" to address.trim(), "filter" to "countrycode:mx", "limit" to "3", "lang" to "es"),
        )
        val places = GeoapifyJson.places(body.requireSuccess())
        if (places.isEmpty()) PlaceSearchResult.Failure(RemoteFailure.NO_RESULTS)
        else PlaceSearchResult.Success(places)
    } catch (e: Exception) {
        PlaceSearchResult.Failure(e.toRemoteFailure())
    }
}

private fun Exception.toRemoteFailure(): RemoteFailure = when (this) {
    is CancellationException -> throw this
    is RemoteException -> reason
    is IOException -> RemoteFailure.NETWORK
    is SerializationException, is IllegalArgumentException, is IllegalStateException,
    is NoSuchElementException, is IndexOutOfBoundsException -> RemoteFailure.INVALID_RESPONSE
    else -> throw this
}
