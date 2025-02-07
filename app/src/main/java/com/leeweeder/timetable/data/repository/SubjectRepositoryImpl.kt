package com.leeweeder.timetable.data.repository

import com.leeweeder.timetable.data.data_source.dao.InstructorDao
import com.leeweeder.timetable.data.data_source.dao.SessionDao
import com.leeweeder.timetable.data.data_source.dao.SubjectDao
import com.leeweeder.timetable.domain.model.Instructor
import com.leeweeder.timetable.domain.model.Subject
import com.leeweeder.timetable.domain.model.toEmptySession
import com.leeweeder.timetable.domain.relation.SubjectWithDetails
import com.leeweeder.timetable.domain.repository.SubjectRepository
import kotlinx.coroutines.flow.Flow

class SubjectRepositoryImpl(
    private val subjectDao: SubjectDao,
    private val sessionDao: SessionDao,
    private val instructorDao: InstructorDao
) : SubjectRepository {
    override suspend fun getSubjectWithDetailsById(id: Int): SubjectWithDetails? {
        return subjectDao.getSubjectWithDetailsById(id)
    }

    override fun observeSubjectWithDetails(): Flow<List<SubjectWithDetails>> {
        return subjectDao.observeSubjectsWithDetails()
    }

    override suspend fun upsertSubject(
        subject: Subject,
        instructor: Instructor?
    ): Int {
        require(!(subject.instructorId == null && instructor == null)) {
            "Subject.instructorId and instructor cannot be null at the same time to prevent ${if (subject.id == 0) "adding" else "editing"}  of subject without instructor"
        }

        val instructorId =
            if (instructor != null && instructor.id == 0) {
                instructorDao.upsertInstructor(instructor).toInt()
            } else {
                subject.instructorId ?: throw NullPointerException("Instructor ID is null")
            }

        return if (instructorId != 0) {
            subjectDao.upsertSubject(subject.copy(instructorId = instructorId)).toInt()
        } else {
            throw RuntimeException("Failed to insert subject without instructor ID")
        }
    }

    override suspend fun deleteSubjectById(id: Int) {
        sessionDao.updateSessions(
            sessionDao.getSessionWithSubjectId(id).map { it.toEmptySession() }
        )
        subjectDao.deleteSubjectById(id)
    }
}