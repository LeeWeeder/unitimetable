package com.leeweeder.unitimetable.data.repository

import com.leeweeder.unitimetable.data.data_source.dao.SessionDao
import com.leeweeder.unitimetable.domain.model.Session
import com.leeweeder.unitimetable.domain.repository.SessionRepository

class SessionRepositoryImpl(
    private val sessionDao: SessionDao
) : SessionRepository {

    override suspend fun updateSession(id: Int, label: String?) {
        sessionDao.updateSession(id = id, label = label)
    }

    override suspend fun updateSession(id: Int, crossRefId: Int) {
        sessionDao.updateSession(id = id, crossRefId = crossRefId)
    }

    override suspend fun updateSessions(sessions: List<Session>) {
        sessionDao.updateSessions(sessions)
    }
}