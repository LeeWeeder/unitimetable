/*
 * Copyright (C) 2025 Lyniel Jhay G. Maquilan (@LeeWeeder)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

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

    @Query("SELECT * FROM session WHERE timetableId = :timeTableId")
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