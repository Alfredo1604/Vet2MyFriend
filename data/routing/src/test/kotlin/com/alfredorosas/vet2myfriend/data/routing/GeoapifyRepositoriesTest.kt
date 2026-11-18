package com.alfredorosas.vet2myfriend.data.routing

import com.alfredorosas.vet2myfriend.domain.GeoPoint
import com.alfredorosas.vet2myfriend.domain.TravelMode
import com.alfredorosas.vet2myfriend.domain.model.PlaceSearchResult
import com.alfredorosas.vet2myfriend.domain.model.PlacePrecision
import com.alfredorosas.vet2myfriend.domain.model.RemoteFailure
import com.alfredorosas.vet2myfriend.domain.model.RouteRequest
import com.alfredorosas.vet2myfriend.domain.model.RouteResult
import kotlinx.coroutines.runBlocking
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

class GeoapifyRepositoriesTest {
    private lateinit var server: MockWebServer
    private lateinit var http: OkHttpClient
    private lateinit var client: GeoapifyClient
    private val request = RouteRequest(GeoPoint(25.67, -100.25), GeoPoint(25.68, -100.24), TravelMode.WALKING)

    @Before fun setup() {
        server = MockWebServer()
        server.start()
        http = OkHttpClient.Builder().retryOnConnectionFailure(false).build()
        client = GeoapifyClient("test-key", http, server.url("/"))
    }
    @After fun cleanup() {
        server.close()
        http.connectionPool.evictAll()
        http.dispatcher.executorService.shutdown()
    }

    @Test fun mapsAllTravelModesAndWaypointOrder() = runBlocking {
        for ((mode, expected) in listOf(TravelMode.WALKING to "walk", TravelMode.CYCLING to "bicycle", TravelMode.DRIVING to "drive")) {
            server.enqueue(MockResponse.Builder().body(ROUTE_JSON).build())
            val result = GeoapifyRoutingRepository(client).calculate(request.copy(mode = mode))
            assertTrue(result is RouteResult.Success)
            val recorded = requireNotNull(server.takeRequest(2, TimeUnit.SECONDS))
            assertEquals("/v1/routing", recorded.url.encodedPath)
            assertEquals(expected, recorded.url.queryParameter("mode"))
            assertEquals("25.67,-100.25|25.68,-100.24", recorded.url.queryParameter("waypoints"))
        }
    }

    @Test fun preservesGeoJsonLegsAndConvertsCoordinatesAndUnits() = runBlocking {
        server.enqueue(MockResponse.Builder().body(ROUTE_JSON).build())
        val route = (GeoapifyRoutingRepository(client).calculate(request) as RouteResult.Success).route
        assertEquals(2, route.segments.size)
        assertEquals(GeoPoint(25.67, -100.25), route.segments.first().first())
        assertEquals(1200.5, route.distanceMeters, 0.0)
        assertEquals(900.0, route.durationSeconds, 0.0)
    }

    @Test fun translatesAuthorizationAndQuotaWithoutRetrying() = runBlocking {
        for ((code, failure) in listOf(401 to RemoteFailure.UNAUTHORIZED, 403 to RemoteFailure.UNAUTHORIZED, 429 to RemoteFailure.QUOTA)) {
            server.enqueue(MockResponse.Builder().code(code).body("{}").build())
            assertEquals(RouteResult.Failure(failure), GeoapifyRoutingRepository(client).calculate(request))
        }
        assertEquals(3, server.requestCount)
    }

    @Test fun distinguishesMissingRouteFromInvalidPayload() = runBlocking {
        server.enqueue(MockResponse.Builder().body("""{"type":"FeatureCollection","features":[]}""").build())
        assertEquals(RouteResult.Failure(RemoteFailure.NO_RESULTS), GeoapifyRoutingRepository(client).calculate(request))
        server.enqueue(MockResponse.Builder().body("<html>unavailable</html>").build())
        assertEquals(RouteResult.Failure(RemoteFailure.INVALID_RESPONSE), GeoapifyRoutingRepository(client).calculate(request))
        server.enqueue(MockResponse.Builder().body(ROUTE_JSON.replace("1200.5", "-1")).build())
        assertEquals(RouteResult.Failure(RemoteFailure.INVALID_RESPONSE), GeoapifyRoutingRepository(client).calculate(request))
    }

    @Test fun missingKeyDoesNotCallTheService() = runBlocking {
        val emptyKey = GeoapifyClient("", http, server.url("/"))
        assertEquals(RouteResult.Failure(RemoteFailure.NOT_CONFIGURED), GeoapifyRoutingRepository(emptyKey).calculate(request))
        assertEquals(0, server.requestCount)
    }

    @Test fun searchesMexicoAndReturnsCandidatesWithoutSelectingOne() = runBlocking {
        server.enqueue(MockResponse.Builder().body("""{"type":"FeatureCollection","features":[{"properties":{"formatted":"Centro, Guadalupe, México","lat":25.67,"lon":-100.25,"result_type":"street"}}]}""").build())
        val result = GeoapifyGeocodingRepository(client).search("Centro Guadalupe") as PlaceSearchResult.Success
        assertEquals(GeoPoint(25.67, -100.25), result.candidates.single().point)
        assertEquals(PlacePrecision.STREET, result.candidates.single().precision)
        val recorded = requireNotNull(server.takeRequest(2, TimeUnit.SECONDS))
        assertEquals("countrycode:mx", recorded.url.queryParameter("filter"))
        assertEquals("Centro Guadalupe", recorded.url.queryParameter("text"))
    }

    companion object {
        private const val ROUTE_JSON = """{"type":"FeatureCollection","features":[{"properties":{"distance":1200.5,"time":900},"geometry":{"type":"MultiLineString","coordinates":[[[-100.25,25.67],[-100.245,25.675]],[[-100.245,25.675],[-100.24,25.68]]]}}]}"""
    }
}
