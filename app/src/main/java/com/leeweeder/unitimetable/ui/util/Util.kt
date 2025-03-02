/*
 * Copyright (C) 2025 Lyniel Jhay G. Maquilan (@LeeWeeder)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

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