package com.leeweeder.timetable.domain.repository

import com.leeweeder.timetable.domain.model.Instructor
import com.leeweeder.timetable.domain.model.Subject
import kotlinx.coroutines.flow.Flow

interface SubjectRepository {

    suspend fun upsertSubject(
        subject: Subject,
        instructor: Instructor,
        subjectInstructorCrossRefId: Int = 0
    ): Int

    fun observeSubjects(): Flow<List<Subject>>

    suspend fun deleteSubjectById(id: Int)
}