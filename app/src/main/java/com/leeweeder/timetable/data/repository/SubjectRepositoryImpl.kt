package com.leeweeder.timetable.data.repository

import com.leeweeder.timetable.data.data_source.dao.InstructorDao
import com.leeweeder.timetable.data.data_source.dao.SessionDao
import com.leeweeder.timetable.data.data_source.dao.SubjectDao
import com.leeweeder.timetable.data.data_source.dao.SubjectInstructorCrossRefDao
import com.leeweeder.timetable.domain.model.Instructor
import com.leeweeder.timetable.domain.model.Subject
import com.leeweeder.timetable.domain.model.SubjectInstructorCrossRef
import com.leeweeder.timetable.domain.model.toEmptySession
import com.leeweeder.timetable.domain.repository.SubjectRepository
import kotlinx.coroutines.flow.Flow

class SubjectRepositoryImpl(
    private val subjectDao: SubjectDao,
    private val sessionDao: SessionDao,
    private val instructorDao: InstructorDao,
    private val subjectInstructorCrossRefDao: SubjectInstructorCrossRefDao
) : SubjectRepository {
    override suspend fun upsertSubject(
        subject: Subject,
        instructor: Instructor,
        subjectInstructorCrossRefId: Int
    ): Int {
        val instructorResult = instructorDao.upsertInstructor(instructor)
        val instructorId = if (instructorResult > 0) {
            instructorResult.toInt()
        } else if (instructor.id != 0) {
            instructor.id
        } else {
            throw RuntimeException("Failed to upsert instructor")
        }

        val subjectResult = subjectDao.upsertSubject(subject)
        val subjectId = if (subjectResult > 0) {
            subjectResult.toInt()
        } else if (subject.id != 0) {
            subject.id
        } else {
            throw RuntimeException("Failed to upsert subject")
        }

        val crossRefResult = subjectInstructorCrossRefDao.upsertSubjectInstructorCrossRef(
            SubjectInstructorCrossRef(
                subjectInstructorCrossRefId,
                subjectId = subjectId,
                instructorId = instructorId
            )
        )

        return if (crossRefResult > 0) crossRefResult.toInt() else subjectInstructorCrossRefId
    }

    override fun observeSubjects(): Flow<List<Subject>> {
        return subjectDao.observeSubjects()
    }

    override suspend fun deleteSubjectById(id: Int) {
        sessionDao.updateSessions(
            sessionDao.getSessionWithSubjectId(id).map { it.toEmptySession() }
        )
        subjectDao.deleteSubjectById(id)
    }
}