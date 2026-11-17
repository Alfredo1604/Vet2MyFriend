package com.alfredorosas.vet2myfriend.domain

enum class Weekday {
    MONDAY,
    TUESDAY,
    WEDNESDAY,
    THURSDAY,
    FRIDAY,
    SATURDAY,
    SUNDAY,
}

/**
 * Recurring schedule for one clinic in its local time.
 * An absent day is unknown. An explicitly empty day is closed.
 */
class WeeklySchedule(intervalsByDay: Map<Weekday, List<OpeningInterval>>) {
    private val schedule = intervalsByDay.mapValues { (_, intervals) -> intervals.toList() }

    fun availabilityOn(day: Weekday, minuteOfDay: Int): Availability =
        availabilityAt(minuteOfDay, schedule[day])
}
