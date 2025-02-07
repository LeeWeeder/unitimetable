package com.leeweeder.timetable.data.data_source

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