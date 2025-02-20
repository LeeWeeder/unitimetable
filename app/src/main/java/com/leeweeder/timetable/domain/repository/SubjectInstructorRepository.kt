package com.leeweeder.timetable.domain.repository

import com.leeweeder.timetable.domain.model.Session
import com.leeweeder.timetable.domain.model.SubjectInstructorCrossRef
import com.leeweeder.timetable.domain.relation.SubjectInstructorCrossRefWithDetails
import kotlinx.coroutines.flow.Flow

interface SubjectInstructorRepository {
    fun observeSubjectInstructors(): Flow<List<SubjectInstructorCrossRefWithDetails>>

    suspend fun getSubjectInstructorById(id: Int): SubjectInstructorCrossRefWithDetails?

    suspend fun getSubjectInstructorCrossRefById(id: Int): SubjectInstructorCrossRef

    suspend fun insertSubjectInstructor(subjectInstructor: SubjectInstructorCrossRef): Int

    suspend fun updateSubjectInstructor(subjectInstructor: SubjectInstructorCrossRef)

    /** @return the sessions that are affected by this deletion (before converting it to empty session) */
    suspend fun deleteSubjectInstructorCrossRefById(id: Int): List<Session>

    suspend fun insertSubjectInstructorCrossRefs(subjectInstructorCrossRefs: List<SubjectInstructorCrossRef>)

    /** Checks if the given instructor has no instructor*/
    suspend fun getNullInstructorCrossRefIds(ids: List<Int>): List<SubjectInstructorCrossRef>

    suspend fun updateSubjectInstructorCrossRefs(refs: List<SubjectInstructorCrossRef>)
}