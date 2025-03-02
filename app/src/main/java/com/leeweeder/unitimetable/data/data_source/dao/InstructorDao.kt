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

package com.leeweeder.unitimetable.data.data_source.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.leeweeder.unitimetable.domain.model.Instructor
import kotlinx.coroutines.flow.Flow

@Dao
interface InstructorDao {
    @Query("SELECT * FROM instructor")
    fun observeInstructors(): Flow<List<Instructor>>

    @Query("DELETE FROM instructor WHERE id = :id")
    suspend fun deleteInstructorById(id: Int)

    @Update
    suspend fun updateInstructor(instructor: Instructor)

    @Insert
    suspend fun insertInstructor(instructor: Instructor)

    @Query("SELECT * FROM instructor WHERE id = :id")
    fun observeInstructor(id: Int): Flow<Instructor?>

    @Query("SELECT * FROM instructor WHERE id = :id")
    suspend fun getInstructorById(id: Int): Instructor?
}