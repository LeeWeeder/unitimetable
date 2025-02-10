package com.leeweeder.timetable.data.repository

import android.util.Log
import com.leeweeder.timetable.data.data_source.dao.SessionDao
import com.leeweeder.timetable.data.data_source.dao.SubjectDao
import com.leeweeder.timetable.domain.model.Subject
import com.leeweeder.timetable.domain.model.toEmptySession
import com.leeweeder.timetable.domain.repository.SubjectRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach

class SubjectRepositoryImpl(
    private val subjectDao: SubjectDao,
    private val sessionDao: SessionDao
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

    override suspend fun deleteSubjectById(id: Int) {
        sessionDao.updateSessions(
            sessionDao.getSessionWithSubjectId(id).map { it.toEmptySession() }
        )
        subjectDao.deleteSubjectById(id)
    }

    override suspend fun getSubjectById(id: Int): Subject? {
        return subjectDao.getSubjectById(id)
    }

    override fun observeSubject(id: Int): Flow<Subject?> {
        return subjectDao.observeSubject(id)
    }
}