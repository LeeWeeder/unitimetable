package com.leeweeder.timetable.data.data_source

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.leeweeder.timetable.data.data_source.dao.InstructorDao
import com.leeweeder.timetable.data.data_source.dao.SessionDao
import com.leeweeder.timetable.data.data_source.dao.SubjectDao
import com.leeweeder.timetable.data.data_source.dao.SubjectInstructorCrossRefDao
import com.leeweeder.timetable.data.data_source.dao.TimeTableDao
import com.leeweeder.timetable.domain.model.Instructor
import com.leeweeder.timetable.domain.model.Session
import com.leeweeder.timetable.domain.model.Subject
import com.leeweeder.timetable.domain.model.SubjectInstructorCrossRef
import com.leeweeder.timetable.domain.model.TimeTable
import com.leeweeder.timetable.domain.relation.SubjectInstructorCrossRefWithDetails

@Database(
    entities = [
        TimeTable::class,
        Session::class,
        Subject::class,
        Instructor::class,
        SubjectInstructorCrossRef::class
    ],
    views = [
        SubjectInstructorCrossRefWithDetails::class
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
    abstract val subjectInstructorCrossRefDao: SubjectInstructorCrossRefDao

    companion object {
        private const val ENFORCE_SINGLE_SUBJECT_INSTRUCTOR_TRIGGER = """ 
            CREATE TRIGGER IF NOT EXISTS enforce_single_subject_instructor
            BEFORE INSERT ON Session
            WHEN NEW.subjectInstructorCrossRefId IS NOT NULL
            BEGIN
                SELECT CASE
                    WHEN EXISTS (
                        SELECT 1
                        FROM Session s
                        JOIN SubjectInstructorCrossRef si1 ON s.subjectInstructorCrossRefId = si1.id
                        JOIN SubjectInstructorCrossRef si2 ON (
                            SELECT subjectId FROM SubjectInstructorCrossRef 
                            WHERE id = NEW.subjectInstructorCrossRefId
                        ) = si2.subjectId
                        WHERE s.timeTableId = NEW.timeTableId
                        AND s.subjectInstructorCrossRefId != NEW.subjectInstructorCrossRefId
                        AND si1.subjectId = si2.subjectId
                    )
                    THEN RAISE(ABORT, 'This subject already exists with a different instructor in this timetable')
                END;
            END;
        """

        // Add this to your database creation callback
        val callback = object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                db.execSQL(ENFORCE_SINGLE_SUBJECT_INSTRUCTOR_TRIGGER)
            }
        }
    }
}