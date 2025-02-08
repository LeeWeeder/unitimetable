package com.leeweeder.timetable.domain.repository

import com.leeweeder.timetable.domain.relation.SubjectInstructorWithId
import kotlinx.coroutines.flow.Flow

interface SubjectInstructorRepository {
    fun observeSubjectInstructors(): Flow<List<SubjectInstructorWithId>>

    suspend fun getSubjectInstructorWithId(id: Int): SubjectInstructorWithId
}