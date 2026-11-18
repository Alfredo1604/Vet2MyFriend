package com.alfredorosas.vet2myfriend.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class GeoPointTest {
    @Test
    fun acceptsCoordinateBounds() {
        assertEquals(-90.0, GeoPoint(-90.0, -180.0).latitude, 0.0)
        assertEquals(180.0, GeoPoint(90.0, 180.0).longitude, 0.0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsLatitudeOutsideTheEarth() {
        GeoPoint(91.0, 0.0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsNonFiniteCoordinates() {
        GeoPoint(0.0, Double.NaN)
    }
}
