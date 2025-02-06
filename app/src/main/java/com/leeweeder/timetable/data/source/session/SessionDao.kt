package com.leeweeder.timetable.data.source.session

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Update

@Dao
interface SessionDao {

    @Insert
    suspend fun insertSessions(sessions: List<Session>)

    @Update
    suspend fun updateSession(session: Session)

    @Update
    suspend fun updateSessions(sessions: List<Session>)
}