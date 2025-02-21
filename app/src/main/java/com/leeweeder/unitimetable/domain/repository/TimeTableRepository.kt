package com.leeweeder.unitimetable.domain.repository

import com.leeweeder.unitimetable.domain.model.Session
import com.leeweeder.unitimetable.domain.model.TimeTable
import com.leeweeder.unitimetable.domain.relation.TimeTableWithSession
import kotlinx.coroutines.flow.Flow

interface TimeTableRepository {

    fun observeTimeTableWithDetails(): Flow<List<TimeTableWithSession>>

    fun observeTimeTables(): Flow<List<TimeTable>>

    suspend fun insertTimeTable(timeTable: TimeTable, sessions: List<Session>? = null): Int

    suspend fun deleteTimeTableById(id: Int)

    suspend fun updateTimeTableName(id: Int, newName: String)

    suspend fun editTimeTableLayout(timeTable: TimeTable)

    suspend fun getTimeTableWithDetails(id: Int): TimeTableWithSession?

    suspend fun getTimeTableNames(): List<String>

    suspend fun getTimetableById(id: Int): TimeTable?
}