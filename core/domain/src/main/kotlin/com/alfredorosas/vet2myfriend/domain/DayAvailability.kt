package com.alfredorosas.vet2myfriend.domain

enum class Availability {
    UNKNOWN,
    CLOSED,
    OPEN,
    BREAK,
}

/** Minute offsets in the center's local day, with an exclusive end. */
data class OpeningInterval(val startMinute: Int, val endMinute: Int) {
    init {
        require(startMinute in 0..1439 && endMinute in 1..1440)
        require(startMinute < endMinute)
    }
}

/**
 * Evaluates a single local day. Pass null for an unknown schedule and an empty list
 * for a confirmed closed day. The caller resolves the center's time zone and date exceptions.
 */
fun availabilityAt(minuteOfDay: Int, intervals: List<OpeningInterval>?): Availability {
    require(minuteOfDay in 0..1439)
    if (intervals == null) return Availability.UNKNOWN
    if (intervals.isEmpty()) return Availability.CLOSED

    val ordered = intervals.sortedBy { it.startMinute }
    require(ordered.zipWithNext().all { (first, second) ->
        first.endMinute <= second.startMinute
    })

    if (ordered.any { minuteOfDay >= it.startMinute && minuteOfDay < it.endMinute }) {
        return Availability.OPEN
    }

    return if (minuteOfDay in ordered.first().startMinute until ordered.last().endMinute) {
        Availability.BREAK
    } else {
        Availability.CLOSED
    }
}
