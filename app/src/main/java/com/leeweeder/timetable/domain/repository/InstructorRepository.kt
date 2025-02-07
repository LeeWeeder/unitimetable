package com.leeweeder.timetable.domain.repository

import com.leeweeder.timetable.domain.model.Instructor
import kotlinx.coroutines.flow.Flow

interface InstructorRepository {
    fun observeInstructors(): Flow<List<Instructor>>

    suspend fun deleteInstructorById(id: Int)

    suspend fun upsertInstructor(instructor: Instructor)
}