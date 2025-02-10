package com.leeweeder.timetable.domain.repository

interface SessionRepository {
    suspend fun updateSession(id: Int, label: String?)

    suspend fun updateSession(id: Int, crossRefId: Int)
}