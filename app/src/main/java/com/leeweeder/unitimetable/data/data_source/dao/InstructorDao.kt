package com.leeweeder.unitimetable.data.data_source.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.leeweeder.unitimetable.domain.model.Instructor
import kotlinx.coroutines.flow.Flow

@Dao
interface InstructorDao {
    @Query("SELECT * FROM instructor")
    fun observeInstructors(): Flow<List<Instructor>>

    @Query("DELETE FROM instructor WHERE id = :id")
    suspend fun deleteInstructorById(id: Int)

    @Update
    suspend fun updateInstructor(instructor: Instructor)

    @Insert
    suspend fun insertInstructor(instructor: Instructor)

    @Query("SELECT * FROM instructor WHERE id = :id")
    fun observeInstructor(id: Int): Flow<Instructor?>

    @Query("SELECT * FROM instructor WHERE id = :id")
    suspend fun getInstructorById(id: Int): Instructor?
}