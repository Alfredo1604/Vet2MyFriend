package com.alfredorosas.vet2myfriend.data.routing

import com.alfredorosas.vet2myfriend.domain.GeoPoint
import com.alfredorosas.vet2myfriend.domain.model.PlaceCandidate
import com.alfredorosas.vet2myfriend.domain.model.PlacePrecision
import com.alfredorosas.vet2myfriend.domain.model.RemoteFailure
import com.alfredorosas.vet2myfriend.domain.model.Route
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

internal object GeoapifyJson {
    fun route(body: String): Route {
        val feature = features(body).firstOrNull()?.jsonObject
            ?: throw RemoteException(RemoteFailure.NO_RESULTS)
        val properties = feature.getValue("properties").jsonObject
        val geometry = feature.getValue("geometry").jsonObject
        require(geometry.getValue("type").jsonPrimitive.content == "MultiLineString")
        val segments = geometry.getValue("coordinates").jsonArray.map { segment ->
            segment.jsonArray.map { point ->
                val coordinates = point.jsonArray
                // GeoJSON is longitude, latitude; request waypoints use latitude, longitude.
                GeoPoint(coordinates[1].jsonPrimitive.double, coordinates[0].jsonPrimitive.double)
            }
        }
        return Route(
            segments = segments,
            distanceMeters = properties.getValue("distance").jsonPrimitive.double,
            durationSeconds = properties.getValue("time").jsonPrimitive.double,
        )
    }

    fun places(body: String): List<PlaceCandidate> = features(body).map { feature ->
        val properties = feature.jsonObject.getValue("properties").jsonObject
        PlaceCandidate(
            label = properties.getValue("formatted").jsonPrimitive.content,
            point = GeoPoint(
                properties.getValue("lat").jsonPrimitive.double,
                properties.getValue("lon").jsonPrimitive.double,
            ),
            precision = when (properties["result_type"]?.jsonPrimitive?.content) {
                "building", "amenity" -> PlacePrecision.ADDRESS
                "street" -> PlacePrecision.STREET
                "city", "suburb", "district", "county", "state", "postcode", "country" -> PlacePrecision.AREA
                else -> PlacePrecision.UNKNOWN
            },
        )
    }

    private fun features(body: String): JsonArray {
        val root: JsonObject = Json.parseToJsonElement(body).jsonObject
        require(root.getValue("type").jsonPrimitive.content == "FeatureCollection")
        return root.getValue("features").jsonArray
    }
}
