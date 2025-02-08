package com.leeweeder.timetable.data.data_source.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.leeweeder.timetable.domain.model.Subject
import kotlinx.coroutines.flow.Flow

@Dao
interface SubjectDao {
    @Transaction
    @Query("SELECT * FROM subject")
    fun observeSubjects(): Flow<List<Subject>>

    @Upsert
    suspend fun upsertSubject(subject: Subject): Long

    @Query("DELETE FROM subject WHERE id = :id")
    suspend fun deleteSubjectById(id: Int)
}