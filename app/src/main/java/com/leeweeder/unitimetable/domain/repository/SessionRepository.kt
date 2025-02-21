package com.leeweeder.unitimetable.domain.repository

import com.leeweeder.unitimetable.domain.model.Session

interface SessionRepository {
    suspend fun updateSession(id: Int, label: String?)

    suspend fun updateSession(id: Int, crossRefId: Int)

    suspend fun updateSessions(sessions: List<Session>)
}