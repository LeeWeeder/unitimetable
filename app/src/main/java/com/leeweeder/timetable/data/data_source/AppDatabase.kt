package com.leeweeder.timetable.data.data_source

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.leeweeder.timetable.data.data_source.dao.InstructorDao
import com.leeweeder.timetable.data.data_source.dao.SessionDao
import com.leeweeder.timetable.data.data_source.dao.SubjectDao
import com.leeweeder.timetable.data.data_source.dao.TimeTableDao
import com.leeweeder.timetable.domain.model.Instructor
import com.leeweeder.timetable.domain.model.Session
import com.leeweeder.timetable.domain.model.Subject
import com.leeweeder.timetable.domain.model.TimeTable

@Database(
    entities = [
        TimeTable::class,
        Session::class,
        Subject::class,
        Instructor::class
    ],
    version = 1
)
@TypeConverters(
    Converters::class
)
abstract class AppDatabase : RoomDatabase() {
    abstract val timeTableDao: TimeTableDao
    abstract val sessionDao: SessionDao
    abstract val subjectDao: SubjectDao
    abstract val instructorDao: InstructorDao
}