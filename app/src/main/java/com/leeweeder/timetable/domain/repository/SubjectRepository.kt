package com.leeweeder.timetable.domain.repository

import com.leeweeder.timetable.domain.model.Session
import com.leeweeder.timetable.domain.model.Subject
import com.leeweeder.timetable.domain.model.SubjectInstructorCrossRef
import kotlinx.coroutines.flow.Flow

interface SubjectRepository {

    suspend fun updateSubject(subject: Subject)

    suspend fun insertSubject(subject: Subject): Int

    fun observeSubjects(): Flow<List<Subject>>

    /** @return The affected sessions and cross refs */
    suspend fun deleteSubjectById(id: Int): Pair<List<Session>, List<SubjectInstructorCrossRef>>

    suspend fun getSubjectById(id: Int): Subject?

    fun observeSubject(id: Int): Flow<Subject?>
}