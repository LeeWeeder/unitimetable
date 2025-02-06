package com.leeweeder.timetable.data.source.subject

import kotlinx.coroutines.flow.Flow

class SubjectDataSource(private val dao: SubjectDao) {
    suspend fun getSubjectsByInstructorId(instructorId: Int): List<Subject> {
        return dao.getSubjectsByInstructorId(instructorId)
    }

    suspend fun getSubjectById(id: Int): Subject? {
        return dao.getSubjectById(id)
    }

    suspend fun getSubjectWithInstructor(subjectId: Int) : SubjectWithInstructor? {
        return dao.getSubjectWithInstructor(subjectId)
    }

    fun observeFiveRecentlyAddedSubjectsWithSession(): Flow<List<SubjectWithSessionCount>> {
        return dao.observeFiveRecentlyAddedSubjectsWithSession()
    }

    fun observeFiveRecentlyAddedSubjects(): Flow<List<Subject>> {
        return dao.observeFiveRecentlyAddedSubjects()
    }

    fun observeSubjectsWithDetails(): Flow<List<SubjectWithDetails>> {
        return dao.observeSubjectsWithDetails()
    }

    suspend fun getSubjectWithDetails(id: Int): SubjectWithDetails? {
        return dao.getSubjectWithDetails(id)
    }

    fun observeSubjects(): Flow<List<Subject>> {
        return dao.observeSubjects()
    }

    suspend fun upsertSubject(subject: Subject): Int {
        val result = dao.upsertSubject(subject).toInt()
        return if (result == -1) subject.id else result
    }

    suspend fun deleteSubject(subject: Subject) {
        dao.deleteSubject(subject)
    }
}