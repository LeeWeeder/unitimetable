package com.leeweeder.timetable.data.source.session

import androidx.room.Dao
import androidx.room.Insert

@Dao
interface SessionDao {

    @Insert
    suspend fun insertSessions(sessions: List<Session>)
}