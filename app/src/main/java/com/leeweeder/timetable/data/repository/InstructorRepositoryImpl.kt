package com.leeweeder.timetable.data.repository

import com.leeweeder.timetable.data.data_source.dao.InstructorDao
import com.leeweeder.timetable.domain.model.Instructor
import com.leeweeder.timetable.domain.repository.InstructorRepository
import kotlinx.coroutines.flow.Flow

class InstructorRepositoryImpl(
    private val instructorDao: InstructorDao
) : InstructorRepository {
    override fun observeInstructors(): Flow<List<Instructor>> {
        return instructorDao.observeInstructors()
    }

    override suspend fun deleteInstructorById(id: Int) {
        instructorDao.deleteInstructorById(id)
    }

    override suspend fun upsertInstructor(instructor: Instructor) {
        instructorDao.upsertInstructor(instructor)
    }
}