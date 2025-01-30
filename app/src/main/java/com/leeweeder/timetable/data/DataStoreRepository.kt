package com.leeweeder.timetable.data

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import com.google.protobuf.InvalidProtocolBufferException
import com.leeweeder.timetable.data.source.TimeTablePref
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import java.io.InputStream
import java.io.OutputStream

interface DataStoreRepository {
    val timeTablePrefFlow: Flow<TimeTablePref>

    suspend fun setMainTimeTableId(id: Int)
}

private val Context.timeTablePref: DataStore<TimeTablePref> by dataStore(
    fileName = "timeTablePref.pb", serializer = TimeTablePrefSerializer
)

class DefaultDataStoreRepository(context: Context) : DataStoreRepository {
    val timeTablePrefDataStore = context.timeTablePref

    override val timeTablePrefFlow: Flow<TimeTablePref>
        get() = timeTablePrefDataStore.data.catch { exception ->
            if (exception is IOException) {
                emit(TimeTablePref.getDefaultInstance())
            } else {
                throw exception
            }
        }

    override suspend fun setMainTimeTableId(id: Int) {
        timeTablePrefDataStore.updateData {
            it.toBuilder().setMainTimeTableId(id).build()
        }
    }
}

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