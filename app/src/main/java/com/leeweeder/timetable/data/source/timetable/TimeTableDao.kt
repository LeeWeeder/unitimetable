package com.leeweeder.timetable.data.source.timetable

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.leeweeder.timetable.data.source.TimeTableWithSessionsWithSubjectAndInstructor
import kotlinx.coroutines.flow.Flow

@Dao
interface TimeTableDao {
    @Query("SELECT * FROM timetable")
    fun observeTimeTables(): Flow<List<TimeTable>>

    @Transaction
    @Query("SELECT * FROM timetable WHERE id = :id")
    suspend fun getTimeTableWithSessionsWithSubjectAndInstructorOfId(id: Int): TimeTableWithSessionsWithSubjectAndInstructor

    @Query("SELECT name FROM timetable")
    fun observeTimeTableNames(): Flow<List<String>>

    @Query("SELECT name FROM timetable")
    suspend fun getTimeTableNames(): List<String>

    @Upsert
    suspend fun insertTimeTable(timeTable: TimeTable): Long

    @Delete
    suspend fun deleteTimeTable(timeTable: TimeTable)
}