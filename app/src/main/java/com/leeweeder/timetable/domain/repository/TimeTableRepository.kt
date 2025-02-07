package com.leeweeder.timetable.domain.repository

import com.leeweeder.timetable.domain.model.TimeTable
import com.leeweeder.timetable.domain.relation.TimeTableWithDetails
import kotlinx.coroutines.flow.Flow

interface TimeTableRepository {

    fun observeTimeTableWithDetails(): Flow<List<TimeTableWithDetails>>

    suspend fun insertTimeTable(timeTable: TimeTable): Int

    suspend fun deleteTimeTableById(id: Int)

    suspend fun updateTimeTableName(id: Int, newName: String)

    suspend fun editTimeTableLayout(timeTable: TimeTable)

    suspend fun getTimeTableWithDetails(id: Int): TimeTableWithDetails?

    suspend fun getTimeTableNames(): List<String>
}