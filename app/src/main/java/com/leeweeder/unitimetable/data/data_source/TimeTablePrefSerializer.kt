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

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import com.leeweeder.timetable.data.source.TimeTablePref
import java.io.InputStream
import java.io.OutputStream

object TimeTablePrefSerializer : Serializer<TimeTablePref> {
    override suspend fun readFrom(input: InputStream): TimeTablePref {
        try {
            return TimeTablePref.parseFrom(input)
        } catch (e: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto", e)
        }
    }

    override suspend fun writeTo(
        t: TimeTablePref,
        output: OutputStream
    ) {
        t.writeTo(output)
    }

    override val defaultValue: TimeTablePref
        get() = TimeTablePref.getDefaultInstance().toBuilder().setMainTimeTableId(-1).build()
}