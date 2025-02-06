package com.leeweeder.timetable.data.source.session

class SessionDataSource(
    private val dao: SessionDao
) {

    suspend fun insertSessions(sessions: List<Session>) {
        dao.insertSessions(sessions)
    }

    suspend fun updateSession(session: Session) {
        dao.updateSession(session)
    }

    suspend fun updateSessions(sessions: List<Session>) {
        dao.updateSessions(sessions)
    }
}