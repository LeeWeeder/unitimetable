package com.leeweeder.timetable.data.repository

import android.util.Log
import com.leeweeder.timetable.data.data_source.dao.SessionDao
import com.leeweeder.timetable.data.data_source.dao.SubjectDao
import com.leeweeder.timetable.data.data_source.dao.SubjectInstructorCrossRefDao
import com.leeweeder.timetable.domain.model.Session
import com.leeweeder.timetable.domain.model.Subject
import com.leeweeder.timetable.domain.model.SubjectInstructorCrossRef
import com.leeweeder.timetable.domain.model.toEmptySession
import com.leeweeder.timetable.domain.repository.SubjectRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach

class SubjectRepositoryImpl(
    private val subjectDao: SubjectDao,
    private val sessionDao: SessionDao,
    private val subjectInstructorCrossRefDao: SubjectInstructorCrossRefDao
) : SubjectRepository {
    override suspend fun updateSubject(subject: Subject) {
        subjectDao.updateSubject(subject)
    }

    override suspend fun insertSubject(subject: Subject): Int {
        return subjectDao.insertSubject(subject).toInt()
    }

    override fun observeSubjects(): Flow<List<Subject>> {
        return subjectDao.observeSubjects()
            .onEach { Log.d("SubjectRepositoryImpl", "subjects $it") }
    }

    override suspend fun deleteSubjectById(id: Int): Pair<List<Session>, List<SubjectInstructorCrossRef>> {
        val affectedSessions = sessionDao.getSessionsBySubjectId(id)
        val affectedSubjectInstructorCrossRefs =
            subjectInstructorCrossRefDao.getSubjectInstructorCrossRefsBySubjectId(id)

        sessionDao.updateSessions(
            affectedSessions.map { it.toEmptySession() }
        )

        subjectDao.deleteSubjectById(id)

        return affectedSessions to affectedSubjectInstructorCrossRefs
    }

    override suspend fun getSubjectById(id: Int): Subject? {
        return subjectDao.getSubjectById(id)
    }

    override fun observeSubject(id: Int): Flow<Subject?> {
        return subjectDao.observeSubject(id)
    }
}