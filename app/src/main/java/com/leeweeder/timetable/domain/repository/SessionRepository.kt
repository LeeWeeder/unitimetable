package com.leeweeder.timetable.domain.repository

import com.leeweeder.timetable.domain.model.Session

interface SessionRepository {
    suspend fun updateSession(id: Int, label: String?)

    suspend fun updateSession(id: Int, crossRefId: Int)

    suspend fun updateSessions(sessions: List<Session>)
}