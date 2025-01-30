package com.leeweeder.timetable.data.source

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.leeweeder.timetable.data.source.instructor.Instructor
import com.leeweeder.timetable.data.source.instructor.InstructorDao
import com.leeweeder.timetable.data.source.session.Session
import com.leeweeder.timetable.data.source.session.SessionDao
import com.leeweeder.timetable.data.source.subject.Subject
import com.leeweeder.timetable.data.source.subject.SubjectDao
import com.leeweeder.timetable.data.source.timetable.TimeTable
import com.leeweeder.timetable.data.source.timetable.TimeTableDao

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
abstract class TimeTableDatabase : RoomDatabase() {
    abstract val timeTableDao: TimeTableDao
    abstract val sessionDao: SessionDao
    abstract val subjectDao: SubjectDao
    abstract val instructorDao: InstructorDao
}