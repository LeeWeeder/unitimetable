package com.leeweeder.unitimetable.data.data_source.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.leeweeder.unitimetable.domain.model.Subject
import kotlinx.coroutines.flow.Flow

@Dao
interface SubjectDao {
    @Query("SELECT * FROM subject")
    fun observeSubjects(): Flow<List<Subject>>

    @Update
    suspend fun updateSubject(subject: Subject)

    @Insert
    suspend fun insertSubject(subject: Subject): Long

    @Query("DELETE FROM subject WHERE id = :id")
    suspend fun deleteSubjectById(id: Int)

    @Query("SELECT * FROM subject WHERE id = :id")
    suspend fun getSubjectById(id: Int): Subject?

    @Query("SELECT * FROM subject WHERE id = :id")
    fun observeSubject(id: Int): Flow<Subject?>
}