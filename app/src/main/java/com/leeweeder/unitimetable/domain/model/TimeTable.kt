package com.leeweeder.unitimetable.domain.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.DayOfWeek
import java.time.LocalTime


@Entity(
    indices = [
        Index("name", unique = true)
    ]
)
data class TimeTable(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val numberOfDays: Int,
    val startingDay: DayOfWeek,
    val startTime: LocalTime,
    /** End time is exclusive. Meaning, end time of 5:00 PM, means the last period is 4:00-5:00 PM */
    val endTime: LocalTime
) {
    constructor(
        id: Int,
        numberOfDays: Int,
        startingDay: DayOfWeek,
        startTime: LocalTime,
        endTime: LocalTime
    ) : this(
        id = id,
        name = "",
        numberOfDays = numberOfDays,
        startingDay = startingDay,
        startTime = startTime,
        endTime = endTime
    )
}