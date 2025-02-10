package com.leeweeder.timetable.data.repository

import com.leeweeder.timetable.data.data_source.dao.SessionDao
import com.leeweeder.timetable.domain.repository.SessionRepository

class SessionRepositoryImpl(
    private val sessionDao: SessionDao
) : SessionRepository {

    override suspend fun updateSession(id: Int, label: String?) {
        sessionDao.updateSession(id = id, label = label)
    }

    override suspend fun updateSession(id: Int, crossRefId: Int) {
        sessionDao.updateSession(id = id, crossRefId = crossRefId)
    }
}