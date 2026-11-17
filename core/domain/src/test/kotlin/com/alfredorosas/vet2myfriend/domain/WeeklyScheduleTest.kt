package com.alfredorosas.vet2myfriend.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class WeeklyScheduleTest {
    private val weekdayShifts = listOf(
        OpeningInterval(9 * 60, 13 * 60),
        OpeningInterval(15 * 60, 17 * 60),
    )

    @Test
    fun followsTheReportedWeekAndDistinguishesTheLunchBreak() {
        val weekdays = listOf(
            Weekday.MONDAY, Weekday.TUESDAY, Weekday.WEDNESDAY,
            Weekday.THURSDAY, Weekday.FRIDAY,
        )
        val schedule = WeeklySchedule(
            weekdays.associateWith { weekdayShifts } + mapOf(
                Weekday.SATURDAY to emptyList(),
                Weekday.SUNDAY to emptyList(),
            ),
        )

        for (day in weekdays) {
            assertEquals(Availability.CLOSED, schedule.availabilityOn(day, 8 * 60 + 59))
            assertEquals(Availability.OPEN, schedule.availabilityOn(day, 9 * 60))
            assertEquals(Availability.BREAK, schedule.availabilityOn(day, 13 * 60))
            assertEquals(Availability.OPEN, schedule.availabilityOn(day, 15 * 60))
            assertEquals(Availability.CLOSED, schedule.availabilityOn(day, 17 * 60))
            assertEquals(Availability.CLOSED, schedule.availabilityOn(day, 22 * 60))
        }
        assertEquals(Availability.CLOSED, schedule.availabilityOn(Weekday.SATURDAY, 10 * 60))
        assertEquals(Availability.CLOSED, schedule.availabilityOn(Weekday.SUNDAY, 10 * 60))
    }

    @Test
    fun leavesUnreportedDaysUnknown() {
        val schedule = WeeklySchedule(mapOf(Weekday.MONDAY to weekdayShifts))

        assertEquals(Availability.UNKNOWN, schedule.availabilityOn(Weekday.SUNDAY, 10 * 60))
    }
}
