package com.leeweeder.timetable.data.source.subject

import kotlinx.coroutines.flow.Flow

class SubjectDataSource(private val dao: SubjectDao) {
    suspend fun getSubjectsByInstructorId(instructorId: Int): List<Subject> {
        return dao.getSubjectsByInstructorId(instructorId)
    }

    fun observeSubjects(): Flow<List<Subject>> {
        return dao.observeSubjects()
    }

    suspend fun insertSubject(subject: Subject) {
        dao.insertSubject(subject)
    }

    suspend fun deleteSubject(subject: Subject) {
        dao.deleteSubject(subject)
    }
}