package com.leeweeder.timetable.data.data_source

import androidx.room.TypeConverter
import com.leeweeder.timetable.util.Hue
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

    @TypeConverter
    fun fromHue(value: Int) = Hue(value)

    @TypeConverter
    fun toHue(hue: Hue) = hue.value
}