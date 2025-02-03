package com.leeweeder.timetable.data.source.instructor

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface InstructorDao {
    @Query("SELECT * FROM instructor")
    suspend fun getInstructors(): List<Instructor>

    @Query("SELECT * FROM instructor WHERE id = :id")
    suspend fun getInstructorById(id: Int): Instructor?

    @Query("SELECT * FROM instructor")
    fun observeInstructors(): Flow<List<Instructor>>

    @Delete
    suspend fun deleteInstructor(instructor: Instructor)

    @Upsert
    suspend fun upsertInstructor(instructor: Instructor): Long
}