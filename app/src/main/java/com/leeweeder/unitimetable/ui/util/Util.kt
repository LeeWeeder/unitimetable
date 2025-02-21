package com.leeweeder.unitimetable.ui.util

import java.time.DayOfWeek
import java.time.LocalTime
import java.time.temporal.ChronoUnit

/**
 * Returns a list of start times starting from start time and ending at end time hour - 1, incrementing by one hour for each subsequent time
 * */
// TODO: Test
fun getTimes(start: LocalTime, end: LocalTime): List<LocalTime> {
    return List(start.until(end, ChronoUnit.HOURS).toInt()) {
        start.plusHours(it.toLong())
    }.sorted()
}

fun getDays(startDay: DayOfWeek, numberOfDays: Int) = List(numberOfDays) {
    startDay.plus(it.toLong())
}

fun LocalTime.plusOneHour(): LocalTime = this.plusHours(1)