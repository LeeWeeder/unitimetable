package com.leeweeder.timetable.data.source.subject

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface SubjectDao {
    @Query("SELECT * FROM subject WHERE instructorId = :instructorId")
    suspend fun getSubjectsByInstructorId(instructorId: Int): List<Subject>

    @Query(
        """
        SELECT subjects.*, COUNT(sessions.id) as sessionCount
        FROM subject AS subjects
        LEFT JOIN session AS sessions ON subjects.id = sessions.subjectId
        GROUP BY subjects.id
        ORDER BY subjects.dateAdded DESC
        LIMIT 5
    """
    )
    fun observeFiveRecentlyAddedSubjectsWithSession(): Flow<List<SubjectWithSessionCount>>

    @Transaction
    @Query("SELECT * FROM subject WHERE id = :id")
    suspend fun getSubjectWithInstructor(id: Int): SubjectWithInstructor?

    @Query("SELECT * FROM subject")
    fun observeSubjects(): Flow<List<Subject>>

    @Query("SELECT * FROM subject WHERE id = :id")
    suspend fun getSubjectById(id: Int): Subject?

    @Upsert
    suspend fun upsertSubject(subject: Subject): Long

    @Delete
    suspend fun deleteSubject(subject: Subject)
}