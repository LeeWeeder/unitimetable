package com.leeweeder.timetable.domain.repository

import com.leeweeder.timetable.domain.model.Instructor
import kotlinx.coroutines.flow.Flow

interface InstructorRepository {
    fun observeInstructors(): Flow<List<Instructor>>

    /** @return The ids of the crossrefs referring the given instructor */
    suspend fun deleteInstructorById(id: Int): List<Int>

    suspend fun insertInstructor(instructor: Instructor)

    suspend fun updateInstructor(instructor: Instructor)

    fun observeInstructor(id: Int): Flow<Instructor?>

    suspend fun getInstructorById(id: Int): Instructor?
}