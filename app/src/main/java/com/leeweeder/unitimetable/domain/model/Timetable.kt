package com.leeweeder.unitimetable.domain.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
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
