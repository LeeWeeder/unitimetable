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
import com.leeweeder.unitimetable.domain.model.Timetable
import com.leeweeder.unitimetable.domain.relation.TimetableWithSession
import kotlinx.coroutines.flow.Flow

interface TimetableRepository {

    fun observeTimetablesWithDetails(): Flow<List<TimetableWithSession>>

    fun observeTimetables(): Flow<List<Timetable>>

    suspend fun insertTimetable(timeTable: Timetable, sessions: List<Session>? = null): Int

    suspend fun deleteTimeTableById(id: Int)

    suspend fun updateTimetableName(id: Int, newName: String)

    suspend fun editTimetableLayout(timeTable: Timetable)

    suspend fun getTimeTableWithDetails(id: Int): TimetableWithSession?

    fun observeTimetableWithDetails(id: Int): Flow<TimetableWithSession?>

    suspend fun getTimeTableNames(): List<String>

    suspend fun getTimetableById(id: Int): Timetable?
}