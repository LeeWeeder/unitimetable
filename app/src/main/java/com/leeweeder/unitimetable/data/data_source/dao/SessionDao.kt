package com.leeweeder.unitimetable.data.data_source.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.leeweeder.unitimetable.domain.model.Session

@Dao
interface SessionDao {

    @Insert
    suspend fun insertSessions(sessions: List<Session>)

    @Query("UPDATE session SET label = :label, subjectInstructorCrossRefId = NULL WHERE id = :id")
    suspend fun updateSession(id: Int, label: String?)

    @Query("UPDATE session SET subjectInstructorCrossRefId = :crossRefId, label = NULL WHERE id = :id")
    suspend fun updateSession(id: Int, crossRefId: Int)

    @Update
    suspend fun updateSessions(sessions: List<Session>)

    @Query("SELECT * FROM session WHERE id = :id")
    suspend fun getSessionById(id: Int): Session?

    @Query("DELETE FROM session WHERE id = :id")
    suspend fun deleteSessionById(id: Int)

    @Delete
    suspend fun deleteSessions(sessions: List<Session>)

    @Query("SELECT * FROM session WHERE timeTableId = :timeTableId")
    suspend fun getSessionsByTimeTableId(timeTableId: Int): List<Session>

    @Query("SELECT * FROM session WHERE subjectInstructorCrossRefId = :crossRefId")
    suspend fun getSessionBySubjectInstructorCrossRefId(crossRefId: Int) : List<Session>

    @Query("""
        SELECT s.* FROM session s
        INNER JOIN subjectinstructorcrossref si ON s.subjectInstructorCrossRefId = si.id
        WHERE si.subjectId = :subjectId
    """)
    suspend fun getSessionsBySubjectId(subjectId: Int): List<Session>
}