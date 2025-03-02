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

import android.util.Log
import com.leeweeder.unitimetable.data.data_source.dao.SessionDao
import com.leeweeder.unitimetable.data.data_source.dao.SubjectDao
import com.leeweeder.unitimetable.data.data_source.dao.SubjectInstructorCrossRefDao
import com.leeweeder.unitimetable.domain.model.Session
import com.leeweeder.unitimetable.domain.model.Subject
import com.leeweeder.unitimetable.domain.model.SubjectInstructorCrossRef
import com.leeweeder.unitimetable.domain.model.toEmptySession
import com.leeweeder.unitimetable.domain.repository.SubjectRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach

class SubjectRepositoryImpl(
    private val subjectDao: SubjectDao,
    private val sessionDao: SessionDao,
    private val subjectInstructorCrossRefDao: SubjectInstructorCrossRefDao
) : SubjectRepository {
    override suspend fun updateSubject(subject: Subject) {
        subjectDao.updateSubject(subject)
    }

    override suspend fun insertSubject(subject: Subject): Int {
        return subjectDao.insertSubject(subject).toInt()
    }

    override fun observeSubjects(): Flow<List<Subject>> {
        return subjectDao.observeSubjects()
            .onEach { Log.d("SubjectRepositoryImpl", "subjects $it") }
    }

    override suspend fun deleteSubjectById(id: Int): Pair<List<Session>, List<SubjectInstructorCrossRef>> {
        val affectedSessions = sessionDao.getSessionsBySubjectId(id)
        val affectedSubjectInstructorCrossRefs =
            subjectInstructorCrossRefDao.getSubjectInstructorCrossRefsBySubjectId(id)

        sessionDao.updateSessions(
            affectedSessions.map { it.toEmptySession() }
        )

        subjectDao.deleteSubjectById(id)

        return affectedSessions to affectedSubjectInstructorCrossRefs
    }

    override suspend fun getSubjectById(id: Int): Subject? {
        return subjectDao.getSubjectById(id)
    }

    override fun observeSubject(id: Int): Flow<Subject?> {
        return subjectDao.observeSubject(id)
    }
}