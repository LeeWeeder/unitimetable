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

package com.leeweeder.unitimetable.data.data_source

import androidx.room.TypeConverter
import com.leeweeder.unitimetable.util.Hue
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