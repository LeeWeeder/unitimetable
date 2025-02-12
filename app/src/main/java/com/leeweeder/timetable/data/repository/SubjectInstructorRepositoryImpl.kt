package com.leeweeder.timetable.data.repository

import com.leeweeder.timetable.data.data_source.dao.SubjectInstructorCrossRefDao
import com.leeweeder.timetable.domain.model.SubjectInstructorCrossRef
import com.leeweeder.timetable.domain.relation.SubjectInstructorCrossRefWithDetails
import com.leeweeder.timetable.domain.repository.SubjectInstructorRepository
import kotlinx.coroutines.flow.Flow

class SubjectInstructorRepositoryImpl(private val subjectInstructorCrossRefDao: SubjectInstructorCrossRefDao) :
    SubjectInstructorRepository {
    override fun observeSubjectInstructors(): Flow<List<SubjectInstructorCrossRefWithDetails>> {
        return subjectInstructorCrossRefDao.observeSubjectInstructors()
    }

    override suspend fun getSubjectInstructorWithId(id: Int): SubjectInstructorCrossRefWithDetails {
        return subjectInstructorCrossRefDao.getSubjectInstructorWithId(id)
            ?: throw IllegalStateException("There is no SubjectInstructor with id: $id")
    }

    override suspend fun insertSubjectInstructor(subjectInstructor: SubjectInstructorCrossRef): Int {
        return subjectInstructorCrossRefDao.insertSubjectInstructorCrossRef(subjectInstructor).toInt()
    }

    override suspend fun updateSubjectInstructor(subjectInstructor: SubjectInstructorCrossRef) {
        subjectInstructorCrossRefDao.updateSubjectInstructorCrossRef(subjectInstructor)
    }
}