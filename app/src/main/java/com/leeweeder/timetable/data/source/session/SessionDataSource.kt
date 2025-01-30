package com.leeweeder.timetable.data.source.session

class SessionDataSource(
    private val dao: SessionDao
) {

    suspend fun insertSessions(sessions: List<Session>) {
        dao.insertSessions(sessions)
    }
}