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

import com.leeweeder.unitimetable.data.data_source.dao.InstructorDao
import com.leeweeder.unitimetable.data.data_source.dao.SubjectInstructorCrossRefDao
import com.leeweeder.unitimetable.domain.model.Instructor
import com.leeweeder.unitimetable.domain.repository.InstructorRepository
import kotlinx.coroutines.flow.Flow

class InstructorRepositoryImpl(
    private val instructorDao: InstructorDao,
    private val subjectInstructorCrossRefDao: SubjectInstructorCrossRefDao
) : InstructorRepository {
    override fun observeInstructors(): Flow<List<Instructor>> {
        return instructorDao.observeInstructors()
    }

    override suspend fun deleteInstructorById(id: Int): List<Int> {
        val crossRefIds =
            subjectInstructorCrossRefDao.getSubjectInstructorCrossRefsByInstructorId(id)
                .map { it.id }
        instructorDao.deleteInstructorById(id)
        return crossRefIds
    }

    override suspend fun insertInstructor(instructor: Instructor) {
        instructorDao.insertInstructor(instructor)
    }

    override suspend fun updateInstructor(instructor: Instructor) {
        instructorDao.updateInstructor(instructor)
    }

    override fun observeInstructor(id: Int): Flow<Instructor?> {
        return instructorDao.observeInstructor(id)
    }

    override suspend fun getInstructorById(id: Int): Instructor? {
        return instructorDao.getInstructorById(id)
    }
}