package com.leeweeder.timetable.data.data_source.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.leeweeder.timetable.domain.model.Instructor
import kotlinx.coroutines.flow.Flow

@Dao
interface InstructorDao {
    @Query("SELECT * FROM instructor")
    fun observeInstructors(): Flow<List<Instructor>>

    @Query("DELETE FROM instructor WHERE id = :id")
    suspend fun deleteInstructorById(id: Int)

    @Upsert
    suspend fun upsertInstructor(instructor: Instructor): Long
}