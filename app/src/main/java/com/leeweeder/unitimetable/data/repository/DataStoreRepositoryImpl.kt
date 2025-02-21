package com.leeweeder.unitimetable.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.dataStore
import com.leeweeder.timetable.data.source.TimeTablePref
import com.leeweeder.unitimetable.data.data_source.TimeTablePrefSerializer
import com.leeweeder.unitimetable.domain.repository.DataStoreRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch

private val Context.timeTablePref: DataStore<TimeTablePref> by dataStore(
    fileName = "timeTablePref.pb", serializer = TimeTablePrefSerializer
)

class DataStoreRepositoryImpl(context: Context) : DataStoreRepository {
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