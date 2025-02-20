package com.leeweeder.timetable.data.repository

import com.leeweeder.timetable.data.data_source.dao.InstructorDao
import com.leeweeder.timetable.data.data_source.dao.SubjectInstructorCrossRefDao
import com.leeweeder.timetable.domain.model.Instructor
import com.leeweeder.timetable.domain.repository.InstructorRepository
import kotlinx.coroutines.flow.Flow

class InstructorRepositoryImpl(
    private val instructorDao: InstructorDao,
    private val subjectInstructorCrossRefDao: SubjectInstructorCrossRefDao
) : InstructorRepository {
    override fun observeInstructors(): Flow<List<Instructor>> {
        return instructorDao.observeInstructors()
    }

    override suspend fun deleteInstructorById(id: Int): List<Int> {
        val crossRefIds =
            subjectInstructorCrossRefDao.getSubjectInstructorCrossRefsByInstructorId(id)
                .map { it.id }
        instructorDao.deleteInstructorById(id)
        return crossRefIds
    }

    override suspend fun insertInstructor(instructor: Instructor) {
        instructorDao.insertInstructor(instructor)
    }

    override suspend fun updateInstructor(instructor: Instructor) {
        instructorDao.updateInstructor(instructor)
    }

    override fun observeInstructor(id: Int): Flow<Instructor?> {
        return instructorDao.observeInstructor(id)
    }

    override suspend fun getInstructorById(id: Int): Instructor? {
        return instructorDao.getInstructorById(id)
    }
}