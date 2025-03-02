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

package com.leeweeder.unitimetable.data.data_source

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RenameColumn
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.AutoMigrationSpec
import androidx.sqlite.db.SupportSQLiteDatabase
import com.leeweeder.unitimetable.data.data_source.dao.InstructorDao
import com.leeweeder.unitimetable.data.data_source.dao.SessionDao
import com.leeweeder.unitimetable.data.data_source.dao.SubjectDao
import com.leeweeder.unitimetable.data.data_source.dao.SubjectInstructorCrossRefDao
import com.leeweeder.unitimetable.data.data_source.dao.TimetableDao
import com.leeweeder.unitimetable.domain.model.Instructor
import com.leeweeder.unitimetable.domain.model.Session
import com.leeweeder.unitimetable.domain.model.Subject
import com.leeweeder.unitimetable.domain.model.SubjectInstructorCrossRef
import com.leeweeder.unitimetable.domain.model.Timetable
import com.leeweeder.unitimetable.domain.relation.SubjectInstructorCrossRefWithDetails

@Database(
    entities = [
        Timetable::class,
        Session::class,
        Subject::class,
        Instructor::class,
        SubjectInstructorCrossRef::class
    ],
    views = [
        SubjectInstructorCrossRefWithDetails::class
    ],
    version = 2,
    autoMigrations = [
        AutoMigration(from = 1, to = 2, spec = AutoMigrationSpec1To2::class)
    ]
)
@TypeConverters(
    Converters::class
)
abstract class AppDatabase : RoomDatabase() {
    abstract val timeTableDao: TimetableDao
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

@RenameColumn.Entries(
    RenameColumn(
        tableName = "Session",
        fromColumnName = "timeTableId",
        toColumnName = "timetableId"
    )
)
class AutoMigrationSpec1To2 : AutoMigrationSpec