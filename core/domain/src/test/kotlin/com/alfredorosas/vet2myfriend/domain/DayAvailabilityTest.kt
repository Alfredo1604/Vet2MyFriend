package com.alfredorosas.vet2myfriend.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class DayAvailabilityTest {
    @Test
    fun distinguishesUnknownDayFromClosedDay() {
        assertEquals(Availability.UNKNOWN, availabilityAt(10 * 60, null))
        assertEquals(Availability.CLOSED, availabilityAt(10 * 60, emptyList()))
    }

    @Test
    fun evaluatesBothSeamsOfSplitHours() {
        val intervals = listOf(
            OpeningInterval(9 * 60, 13 * 60),
            OpeningInterval(15 * 60, 17 * 60),
        )

        assertEquals(Availability.CLOSED, availabilityAt(8 * 60 + 59, intervals))
        assertEquals(Availability.OPEN, availabilityAt(9 * 60, intervals))
        assertEquals(Availability.OPEN, availabilityAt(12 * 60 + 59, intervals))
        assertEquals(Availability.BREAK, availabilityAt(13 * 60, intervals))
        assertEquals(Availability.OPEN, availabilityAt(15 * 60, intervals))
        assertEquals(Availability.CLOSED, availabilityAt(17 * 60, intervals))
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsOverlappingIntervals() {
        availabilityAt(11 * 60, listOf(OpeningInterval(540, 780), OpeningInterval(720, 900)))
    }
}
