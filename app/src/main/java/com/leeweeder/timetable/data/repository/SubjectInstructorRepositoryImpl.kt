package com.leeweeder.timetable.data.repository

import com.leeweeder.timetable.data.data_source.dao.SessionDao
import com.leeweeder.timetable.data.data_source.dao.SubjectInstructorCrossRefDao
import com.leeweeder.timetable.domain.model.Session
import com.leeweeder.timetable.domain.model.SubjectInstructorCrossRef
import com.leeweeder.timetable.domain.model.toEmptySession
import com.leeweeder.timetable.domain.relation.SubjectInstructorCrossRefWithDetails
import com.leeweeder.timetable.domain.repository.SubjectInstructorRepository
import kotlinx.coroutines.flow.Flow

class SubjectInstructorRepositoryImpl(
    private val subjectInstructorCrossRefDao: SubjectInstructorCrossRefDao,
    private val sessionDao: SessionDao
) : SubjectInstructorRepository {
    override fun observeSubjectInstructors(): Flow<List<SubjectInstructorCrossRefWithDetails>> {
        return subjectInstructorCrossRefDao.observeSubjectInstructors()
    }

    override suspend fun getSubjectInstructorById(id: Int): SubjectInstructorCrossRefWithDetails? {
        return subjectInstructorCrossRefDao.getSubjectInstructorById(id)
    }

    override suspend fun getSubjectInstructorCrossRefById(id: Int): SubjectInstructorCrossRef {
        return subjectInstructorCrossRefDao.getSubjectInstructorCrossRefById(id)
            ?: throw IllegalStateException("Why is this subjectinstructorcrossref not exist in the database with id: $id")
    }

    override suspend fun insertSubjectInstructor(subjectInstructor: SubjectInstructorCrossRef): Int {
        return subjectInstructorCrossRefDao.insertSubjectInstructorCrossRef(subjectInstructor)
            .toInt()
    }

    override suspend fun updateSubjectInstructor(subjectInstructor: SubjectInstructorCrossRef) {
        subjectInstructorCrossRefDao.updateSubjectInstructorCrossRef(subjectInstructor)
    }

    override suspend fun deleteSubjectInstructorCrossRefById(id: Int): List<Session> {
        val sessionsOfThisSubjectInstructorCrossRef =
            sessionDao.getSessionBySubjectInstructorCrossRefId(id)

        sessionDao.updateSessions(sessionsOfThisSubjectInstructorCrossRef.map {
            it.toEmptySession()
        })

        // Finally, delete the crossref
        subjectInstructorCrossRefDao.deleteSubjectInstructorCrossRefById(id)

        return sessionsOfThisSubjectInstructorCrossRef
    }

    override suspend fun insertSubjectInstructorCrossRefs(subjectInstructorCrossRefs: List<SubjectInstructorCrossRef>) {
        subjectInstructorCrossRefDao.insertSubjectInstructorCrossRefs(subjectInstructorCrossRefs)
    }
}