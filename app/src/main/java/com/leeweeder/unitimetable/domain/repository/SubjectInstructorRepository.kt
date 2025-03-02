/*
 * Copyright (C) 2025 Lyniel Jhay G. Maquilan (@LeeWeeder)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.leeweeder.unitimetable.domain.repository

import com.leeweeder.unitimetable.domain.model.Session
import com.leeweeder.unitimetable.domain.model.SubjectInstructorCrossRef
import com.leeweeder.unitimetable.domain.relation.SubjectInstructorCrossRefWithDetails
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