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

package com.leeweeder.unitimetable.domain.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.leeweeder.unitimetable.ui.util.getDays
import com.leeweeder.unitimetable.ui.util.getTimes
import kotlinx.serialization.Serializable
import java.time.DayOfWeek
import java.time.LocalTime


@Entity(
    indices = [
        Index("name", unique = true)
    ]
)
data class Timetable(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val numberOfDays: Int,
    val startingDay: DayOfWeek,
    val startTime: LocalTime,
    /** End time is exclusive. Meaning, end time of 5:00 PM, means the last period is 4:00-5:00 PM */
    val endTime: LocalTime
) {
    fun serialize(): SerializableTimetable {
        return SerializableTimetable(
            id = id,
            name = name,
            numberOfDays = numberOfDays,
            startingDay = startingDay,
            startTimeHour = startTime.hour,
            endTimeHour = endTime.hour
        )
    }

    val startTimes: List<LocalTime>
        get() = getTimes(startTime, endTime)

    val days: List<DayOfWeek>
        get() = getDays(startingDay, numberOfDays)
}

@Serializable
data class SerializableTimetable(
    val id: Int,
    val name: String,
    val numberOfDays: Int,
    val startingDay: DayOfWeek,
    val startTimeHour: Int,
    /** End time is exclusive. Meaning, end time of 5:00 PM, means the last period is 4:00-5:00 PM */
    val endTimeHour: Int
) {
    fun normalize(): Timetable {
        return Timetable(
            id = id,
            numberOfDays = numberOfDays,
            startingDay = startingDay,
            startTime = LocalTime.of(startTimeHour, 0),
            endTime = LocalTime.of(endTimeHour, 0),
            name = name
        )
    }
}
