package com.leeweeder.timetable.data.source.subject

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface SubjectDao {
    @Query("SELECT * from subject WHERE instructorId = :instructorId")
    suspend fun getSubjectsByInstructorId(instructorId: Int): List<Subject>

    @Query("SELECT * from subject")
    fun observeSubjects(): Flow<List<Subject>>

    @Upsert
    suspend fun insertSubject(subject: Subject)

    @Delete
    suspend fun deleteSubject(subject: Subject)
}