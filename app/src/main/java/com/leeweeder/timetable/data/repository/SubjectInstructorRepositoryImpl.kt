package com.leeweeder.timetable.data.repository

import com.leeweeder.timetable.data.data_source.dao.SubjectInstructorCrossRefDao
import com.leeweeder.timetable.domain.relation.SubjectInstructorWithId
import com.leeweeder.timetable.domain.repository.SubjectInstructorRepository
import kotlinx.coroutines.flow.Flow

class SubjectInstructorRepositoryImpl(private val subjectInstructorCrossRefDao: SubjectInstructorCrossRefDao) :
    SubjectInstructorRepository {
    override fun observeSubjectInstructors(): Flow<List<SubjectInstructorWithId>> {
        return subjectInstructorCrossRefDao.observeSubjectInstructors()
    }

    override suspend fun getSubjectInstructorWithId(id: Int): SubjectInstructorWithId {
        return subjectInstructorCrossRefDao.getSubjectInstructorWithId(id)
            ?: throw IllegalStateException("There is no SubjectInstructor with id: $id")
    }
}