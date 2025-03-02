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
import com.leeweeder.unitimetable.domain.model.Subject
import com.leeweeder.unitimetable.domain.model.SubjectInstructorCrossRef
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