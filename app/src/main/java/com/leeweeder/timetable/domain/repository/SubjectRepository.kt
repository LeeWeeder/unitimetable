package com.leeweeder.timetable.domain.repository

import com.leeweeder.timetable.domain.model.Subject
import kotlinx.coroutines.flow.Flow

interface SubjectRepository {

    suspend fun updateSubject(subject: Subject)

    suspend fun insertSubject(subject: Subject): Int

    fun observeSubjects(): Flow<List<Subject>>

    suspend fun deleteSubjectById(id: Int)

    suspend fun getSubjectById(id: Int): Subject?

    fun observeSubject(id: Int): Flow<Subject?>
}