package com.leeweeder.timetable.data.repository

import com.leeweeder.timetable.data.data_source.dao.SessionDao
import com.leeweeder.timetable.data.data_source.dao.TimeTableDao
import com.leeweeder.timetable.domain.model.Session
import com.leeweeder.timetable.domain.model.TimeTable
import com.leeweeder.timetable.domain.relation.TimeTableWithSession
import com.leeweeder.timetable.domain.repository.TimeTableRepository
import com.leeweeder.timetable.ui.util.getDays
import com.leeweeder.timetable.ui.util.getTimes
import kotlinx.coroutines.flow.Flow

class TimeTableRepositoryImpl(
    private val timeTableDao: TimeTableDao,
    private val sessionDao: SessionDao
) : TimeTableRepository {
    override fun observeTimeTableWithDetails(): Flow<List<TimeTableWithSession>> {
        return timeTableDao.observeTimeTablesWithDetails()
    }

    override fun observeTimeTables(): Flow<List<TimeTable>> {
        return timeTableDao.observeTimeTables()
    }

    /**
     * Insert a new timetable and its associated sessions
     *
     * @return The id of the newly inserted timetable
     * */
    override suspend fun insertTimeTable(
        timeTable: TimeTable
    ): Int {
        val newTimeTableId = timeTableDao.insertTimeTable(timeTable).toInt()

        sessionDao.insertSessions(
            getTimes(timeTable.startTime, timeTable.endTime).flatMap { startTime ->
                getDays(timeTable.startingDay, timeTable.numberOfDays).map { dayOfWeek ->
                    Session.emptySession(newTimeTableId, dayOfWeek, startTime)
                }
            }
        )

        return newTimeTableId
    }

    override suspend fun deleteTimeTableById(id: Int) {
        timeTableDao.deleteTimeTableById(id)
    }

    override suspend fun updateTimeTableName(id: Int, newName: String) {
        timeTableDao.updateTimeTableName(id, newName)
    }

    override suspend fun editTimeTableLayout(timeTable: TimeTable) {
        val timeTableId = timeTable.id
        val numberOfDays = timeTable.numberOfDays
        val startingDay = timeTable.startingDay
        val startTime = timeTable.startTime
        val endTime = timeTable.endTime

        val days = getDays(startingDay, numberOfDays)
        val times = getTimes(startTime, endTime)

        sessionDao.deleteSessions(sessionDao.getSessionsByTimeTableId(timeTableId)
            .filterNot { it.dayOfWeek in days || it.startTime in times }
        )

        sessionDao.insertSessions(
            buildList {
                days.forEach { dayOfWeek ->
                    times.forEach { startTime ->
                        sessionDao.getSessionsByTimeTableId(timeTableId)
                            .find { dayOfWeek == it.dayOfWeek && startTime == it.startTime }
                            ?: add(
                                Session.emptySession(
                                    timeTableId,
                                    dayOfWeek = dayOfWeek,
                                    startTime = startTime
                                )
                            )
                    }
                }
            }
        )

        timeTableDao.updateTimeTableLayout(
            newNumberOfDays = numberOfDays,
            newStartingDay = startingDay,
            newStartTime = startTime,
            newEndTime = endTime,
            id = timeTableId
        )
    }

    override suspend fun getTimeTableWithDetails(id: Int): TimeTableWithSession? {
        return timeTableDao.getTimeTableWithDetailsById(id)
    }

    override suspend fun getTimeTableNames(): List<String> {
        return timeTableDao.getTimeTableNames()
    }

    override suspend fun getTimetableById(id: Int): TimeTable? {
        return timeTableDao.getTimetableById(id)
    }
}