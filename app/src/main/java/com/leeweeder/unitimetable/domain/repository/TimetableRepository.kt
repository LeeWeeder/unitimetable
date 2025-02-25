package com.leeweeder.unitimetable.domain.repository

import com.leeweeder.unitimetable.domain.model.Session
import com.leeweeder.unitimetable.domain.model.Timetable
import com.leeweeder.unitimetable.domain.relation.TimetableWithSession
import kotlinx.coroutines.flow.Flow

interface TimetableRepository {

    fun observeTimeTableWithDetails(): Flow<List<TimetableWithSession>>

    fun observeTimeTables(): Flow<List<Timetable>>

    suspend fun insertTimetable(timeTable: Timetable, sessions: List<Session>? = null): Int

    suspend fun deleteTimeTableById(id: Int)

    suspend fun updateTimetableName(id: Int, newName: String)

    suspend fun editTimetableLayout(timeTable: Timetable)

    suspend fun getTimeTableWithDetails(id: Int): TimetableWithSession?

    fun observeTimetableWithDetails(id: Int): Flow<TimetableWithSession?>

    suspend fun getTimeTableNames(): List<String>

    suspend fun getTimetableById(id: Int): Timetable?
}