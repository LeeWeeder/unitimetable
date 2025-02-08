package com.leeweeder.timetable.data.data_source.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.leeweeder.timetable.domain.model.TimeTable
import com.leeweeder.timetable.domain.relation.TimeTableWithSession
import kotlinx.coroutines.flow.Flow
import java.time.DayOfWeek
import java.time.LocalTime

@Dao
interface TimeTableDao {
    @Transaction
    @Query("SELECT * FROM timetable")
    fun observeTimeTablesWithDetails(): Flow<List<TimeTableWithSession>>

    @Transaction
    @Query("SELECT * FROM timetable WHERE id = :id")
    suspend fun getTimeTableWithDetailsById(id: Int): TimeTableWithSession

    @Query("UPDATE timetable SET name = :newName WHERE id = :id")
    suspend fun updateTimeTableName(id: Int, newName: String)

    @Query("DELETE FROM timetable WHERE id = :id")
    suspend fun deleteTimeTableById(id: Int)

    @Insert
    suspend fun insertTimeTable(timeTable: TimeTable): Long

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
}