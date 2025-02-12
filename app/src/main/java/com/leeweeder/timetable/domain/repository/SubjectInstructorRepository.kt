package com.leeweeder.timetable.domain.repository

import com.leeweeder.timetable.domain.model.SubjectInstructorCrossRef
import com.leeweeder.timetable.domain.relation.SubjectInstructorCrossRefWithDetails
import kotlinx.coroutines.flow.Flow

interface SubjectInstructorRepository {
    fun observeSubjectInstructors(): Flow<List<SubjectInstructorCrossRefWithDetails>>

    suspend fun getSubjectInstructorWithId(id: Int): SubjectInstructorCrossRefWithDetails

    suspend fun insertSubjectInstructor(subjectInstructor: SubjectInstructorCrossRef): Int

    suspend fun updateSubjectInstructor(subjectInstructor: SubjectInstructorCrossRef)
}