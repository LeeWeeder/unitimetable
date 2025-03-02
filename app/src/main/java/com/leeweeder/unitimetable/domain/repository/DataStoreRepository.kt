package com.leeweeder.unitimetable.domain.repository

import com.leeweeder.timetable.data.source.TimeTablePref
import kotlinx.coroutines.flow.Flow

interface DataStoreRepository {
    val timeTablePrefFlow: Flow<TimeTablePref>

    val selectedTimetableIdFlow: Flow<Int>

    suspend fun setMainTimeTableId(id: Int)

    suspend fun setSelectedTimetable(id: Int)
}