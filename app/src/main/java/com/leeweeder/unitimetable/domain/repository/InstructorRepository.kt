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

import com.leeweeder.unitimetable.domain.model.Instructor
import kotlinx.coroutines.flow.Flow

interface InstructorRepository {
    fun observeInstructors(): Flow<List<Instructor>>

    /** @return The ids of the crossrefs referring the given instructor */
    suspend fun deleteInstructorById(id: Int): List<Int>

    suspend fun insertInstructor(instructor: Instructor)

    suspend fun updateInstructor(instructor: Instructor)

    fun observeInstructor(id: Int): Flow<Instructor?>

    suspend fun getInstructorById(id: Int): Instructor?
}