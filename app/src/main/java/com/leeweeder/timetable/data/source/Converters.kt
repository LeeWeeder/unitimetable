package com.leeweeder.timetable.data.source

import androidx.room.TypeConverter
import java.time.DayOfWeek
import java.time.LocalTime

class Converters {
    @TypeConverter
    fun fromLocalTime(localTime: LocalTime) = localTime.toSecondOfDay()

    @TypeConverter
    fun toLocalTime(value: Int) = LocalTime.ofSecondOfDay(value.toLong())

    @TypeConverter
    fun fromDayOfWeek(dayOfWeek: DayOfWeek) = dayOfWeek.value

    @TypeConverter
    fun toDayOfWeek(value: Int) = DayOfWeek.of(value)
}