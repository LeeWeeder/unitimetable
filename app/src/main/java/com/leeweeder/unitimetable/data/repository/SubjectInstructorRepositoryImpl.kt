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

package com.leeweeder.unitimetable.data.repository

import com.leeweeder.unitimetable.data.data_source.dao.SessionDao
import com.leeweeder.unitimetable.data.data_source.dao.SubjectInstructorCrossRefDao
import com.leeweeder.unitimetable.domain.model.Session
import com.leeweeder.unitimetable.domain.model.SubjectInstructorCrossRef
import com.leeweeder.unitimetable.domain.model.toEmptySession
import com.leeweeder.unitimetable.domain.relation.SubjectInstructorCrossRefWithDetails
import com.leeweeder.unitimetable.domain.repository.SubjectInstructorRepository
import kotlinx.coroutines.flow.Flow

class SubjectInstructorRepositoryImpl(
    private val subjectInstructorCrossRefDao: SubjectInstructorCrossRefDao,
    private val sessionDao: SessionDao
) : SubjectInstructorRepository {
    override fun observeSubjectInstructors(): Flow<List<SubjectInstructorCrossRefWithDetails>> {
        return subjectInstructorCrossRefDao.observeSubjectInstructors()
    }

    override suspend fun getSubjectInstructorById(id: Int): SubjectInstructorCrossRefWithDetails? {
        return subjectInstructorCrossRefDao.getSubjectInstructorById(id)
    }

    override suspend fun getSubjectInstructorCrossRefById(id: Int): SubjectInstructorCrossRef {
        return subjectInstructorCrossRefDao.getSubjectInstructorCrossRefById(id)
            ?: throw IllegalStateException("Why is this subjectinstructorcrossref not exist in the database with id: $id")
    }

    override suspend fun insertSubjectInstructor(subjectInstructor: SubjectInstructorCrossRef): Int {
        return subjectInstructorCrossRefDao.insertSubjectInstructorCrossRef(subjectInstructor)
            .toInt()
    }

    override suspend fun updateSubjectInstructor(subjectInstructor: SubjectInstructorCrossRef) {
        subjectInstructorCrossRefDao.updateSubjectInstructorCrossRef(subjectInstructor)
    }

    override suspend fun deleteSubjectInstructorCrossRefById(id: Int): List<Session> {
        val sessionsOfThisSubjectInstructorCrossRef =
            sessionDao.getSessionBySubjectInstructorCrossRefId(id)

        sessionDao.updateSessions(sessionsOfThisSubjectInstructorCrossRef.map {
            it.toEmptySession()
        })

        // Finally, delete the crossref
        subjectInstructorCrossRefDao.deleteSubjectInstructorCrossRefById(id)

        return sessionsOfThisSubjectInstructorCrossRef
    }

    override suspend fun insertSubjectInstructorCrossRefs(subjectInstructorCrossRefs: List<SubjectInstructorCrossRef>) {
        subjectInstructorCrossRefDao.insertSubjectInstructorCrossRefs(subjectInstructorCrossRefs)
    }

    override suspend fun getNullInstructorCrossRefIds(ids: List<Int>): List<SubjectInstructorCrossRef> {
        return subjectInstructorCrossRefDao.getNullInstructorCrossRefIds(ids)
    }

    override suspend fun updateSubjectInstructorCrossRefs(refs: List<SubjectInstructorCrossRef>) {
        subjectInstructorCrossRefDao.updateSubjectInstructorCrossRefs(refs)
    }
}