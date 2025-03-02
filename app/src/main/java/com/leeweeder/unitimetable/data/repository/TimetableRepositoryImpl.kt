package com.leeweeder.unitimetable.data.repository

import android.util.Log
import com.leeweeder.unitimetable.data.data_source.dao.SessionDao
import com.leeweeder.unitimetable.data.data_source.dao.TimetableDao
import com.leeweeder.unitimetable.domain.model.Session
import com.leeweeder.unitimetable.domain.model.Timetable
import com.leeweeder.unitimetable.domain.relation.TimetableWithSession
import com.leeweeder.unitimetable.domain.repository.TimetableRepository
import com.leeweeder.unitimetable.ui.util.getDays
import com.leeweeder.unitimetable.ui.util.getTimes
import kotlinx.coroutines.flow.Flow

class TimetableRepositoryImpl(
    private val timetableDao: TimetableDao,
    private val sessionDao: SessionDao
) : TimetableRepository {
    override fun observeTimetablesWithDetails(): Flow<List<TimetableWithSession>> {
        return timetableDao.observeTimeTablesWithDetails()
    }

    override fun observeTimetables(): Flow<List<Timetable>> {
        return timetableDao.observeTimeTables()
    }

    /**
     * Insert a new timetable and its associated sessions
     *
     * @return The id of the newly inserted timetable
     * */
    override suspend fun insertTimetable(
        timeTable: Timetable,
        sessions: List<Session>?
    ): Int {
        val newTimeTableId = timetableDao.insertTimeTable(timeTable).toInt()

        if (sessions == null) {
            sessionDao.insertSessions(
                getTimes(timeTable.startTime, timeTable.endTime).flatMap { startTime ->
                    getDays(timeTable.startingDay, timeTable.numberOfDays).map { dayOfWeek ->
                        Session.emptySession(newTimeTableId, dayOfWeek, startTime)
                    }
                }
            )
        } else {
            sessionDao.insertSessions(sessions)
        }

        return newTimeTableId
    }

    override suspend fun deleteTimeTableById(id: Int) {
        timetableDao.deleteTimeTableById(id)
    }

    override suspend fun updateTimetableName(id: Int, newName: String) {
        timetableDao.updateTimeTableName(id, newName)
    }

    override suspend fun editTimetableLayout(timeTable: Timetable) {
        val timeTableId = timeTable.id
        val numberOfDays = timeTable.numberOfDays
        val startingDay = timeTable.startingDay

        val startTime = timeTable.startTime
        val endTime = timeTable.endTime

        val days = getDays(startingDay, numberOfDays)
        val times = getTimes(startTime, endTime)

        sessionDao.deleteSessions(sessionDao.getSessionsByTimeTableId(timeTableId)
            .filterNot {
                Log.d("TimetableRepositoryImpl", "Current session: $it")
                Log.d("TimetableRepositoryImpl", "Days of week: $days, day of week: ${it.dayOfWeek}")
                Log.d("TimetableRepositoryImpl", "Times: $times, day of week: ${it.startTime}")
                it.dayOfWeek in days && it.startTime in times
            }.also { Log.d("TimetableRepositoryImpl", it.toString()) }
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

        timetableDao.updateTimeTableLayout(
            newNumberOfDays = numberOfDays,
            newStartingDay = startingDay,
            newStartTime = startTime,
            newEndTime = endTime,
            id = timeTableId
        )
    }

    override suspend fun getTimeTableWithDetails(id: Int): TimetableWithSession? {
        return timetableDao.getTimeTableWithDetailsById(id)
    }

    override fun observeTimetableWithDetails(id: Int): Flow<TimetableWithSession?> {
        return timetableDao.observeTimetableWithDetails(id)
    }

    override suspend fun getTimeTableNames(): List<String> {
        return timetableDao.getTimeTableNames()
    }

    override suspend fun getTimetableById(id: Int): Timetable? {
        return timetableDao.getTimetableById(id)
    }
}