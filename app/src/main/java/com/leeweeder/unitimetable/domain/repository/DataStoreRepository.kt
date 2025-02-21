package com.leeweeder.unitimetable.domain.repository

import com.leeweeder.timetable.data.source.TimeTablePref
import kotlinx.coroutines.flow.Flow

interface DataStoreRepository {
    val timeTablePrefFlow: Flow<TimeTablePref>

    suspend fun setMainTimeTableId(id: Int)
}