package com.leeweeder.timetable.domain.repository

import com.leeweeder.timetable.domain.model.TimeTable
import com.leeweeder.timetable.domain.relation.TimeTableWithSession
import kotlinx.coroutines.flow.Flow

interface TimeTableRepository {

    fun observeTimeTableWithDetails(): Flow<List<TimeTableWithSession>>

    fun observeTimeTables(): Flow<List<TimeTable>>

    suspend fun insertTimeTable(timeTable: TimeTable): Int

    suspend fun deleteTimeTableById(id: Int)

    suspend fun updateTimeTableName(id: Int, newName: String)

    suspend fun editTimeTableLayout(timeTable: TimeTable)

    suspend fun getTimeTableWithDetails(id: Int): TimeTableWithSession?

    suspend fun getTimeTableNames(): List<String>

    suspend fun getTimetableById(id: Int): TimeTable?
}