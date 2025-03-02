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
import androidx.room.Transaction
import com.leeweeder.unitimetable.domain.model.Timetable
import com.leeweeder.unitimetable.domain.relation.TimetableWithSession
import kotlinx.coroutines.flow.Flow
import java.time.DayOfWeek
import java.time.LocalTime

@Dao
interface TimetableDao {
    @Transaction
    @Query("SELECT * FROM timetable")
    fun observeTimeTablesWithDetails(): Flow<List<TimetableWithSession>>

    @Query("SELECT * FROM timetable")
    fun observeTimeTables(): Flow<List<Timetable>>

    @Transaction
    @Query("SELECT * FROM timetable WHERE id = :id")
    suspend fun getTimeTableWithDetailsById(id: Int): TimetableWithSession

    @Query("UPDATE timetable SET name = :newName WHERE id = :id")
    suspend fun updateTimeTableName(id: Int, newName: String)

    @Query("DELETE FROM timetable WHERE id = :id")
    suspend fun deleteTimeTableById(id: Int)

    @Insert
    suspend fun insertTimeTable(timeTable: Timetable): Long

    @Query(
        """
        UPDATE timetable SET
        numberOfDays = :newNumberOfDays,
        startingDay = :newStartingDay,
        startTime = :newStartTime,
        endTime = :newEndTime
        WHERE id = :id
        """
    )
    suspend fun updateTimeTableLayout(
        newNumberOfDays: Int,
        newStartingDay: DayOfWeek,
        newStartTime: LocalTime,
        newEndTime: LocalTime,
        id: Int
    )

    @Query("SELECT name FROM timetable")
    suspend fun getTimeTableNames(): List<String>

    @Query("SELECT * FROM timetable WHERE id = :id")
    suspend fun getTimetableById(id: Int): Timetable?

    @Transaction
    @Query("SELECT * FROM timetable WHERE id = :id")
    fun observeTimetableWithDetails(id: Int): Flow<TimetableWithSession?>
}