package com.leeweeder.timetable.data.source.timetable

import com.leeweeder.timetable.data.source.TimeTableWithSessionsWithSubjectAndInstructor
import kotlinx.coroutines.flow.Flow

class TimeTableDataSource(private val dao: TimeTableDao) {
    fun observeTimeTables(): Flow<List<TimeTable>> {
        return dao.observeTimeTables()
    }

    fun observeTimeTableNames(): Flow<List<String>> {
        return dao.observeTimeTableNames()
    }

    suspend fun getTimeTableNames(): List<String> {
        return dao.getTimeTableNames()
    }

    suspend fun getTimeTableWithSessionsWithSubjectAndInstructorOfId(id: Int): TimeTableWithSessionsWithSubjectAndInstructor {
        return dao.getTimeTableWithSessionsWithSubjectAndInstructorOfId(id)
    }

    suspend fun insertTimeTable(timeTable: TimeTable): Int {
        return dao.insertTimeTable(timeTable).toInt()
    }

    suspend fun deleteTimeTable(timeTable: TimeTable) {
        dao.deleteTimeTable(timeTable)
    }
}