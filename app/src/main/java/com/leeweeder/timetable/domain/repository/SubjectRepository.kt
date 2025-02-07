package com.leeweeder.timetable.domain.repository

import com.leeweeder.timetable.domain.model.Instructor
import com.leeweeder.timetable.domain.model.Subject
import com.leeweeder.timetable.domain.relation.SubjectWithDetails
import kotlinx.coroutines.flow.Flow

interface SubjectRepository {
    suspend fun getSubjectWithDetailsById(id: Int): SubjectWithDetails?

    fun observeSubjectWithDetails(): Flow<List<SubjectWithDetails>>

    suspend fun upsertSubject(subject: Subject, instructor: Instructor?): Int

    suspend fun deleteSubjectById(id: Int)
}