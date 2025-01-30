package com.leeweeder.timetable.data.source

import androidx.room.Embedded
import androidx.room.Relation
import com.leeweeder.timetable.data.source.instructor.Instructor
import com.leeweeder.timetable.data.source.session.Session
import com.leeweeder.timetable.data.source.subject.Subject
import com.leeweeder.timetable.data.source.timetable.TimeTable
import java.time.DayOfWeek

data class TimeTableWithSessionsWithSubjectAndInstructor(
    @Embedded val timeTable: TimeTable,
    @Relation(
        entity = Session::class,
        parentColumn = "id",
        entityColumn = "timeTableId"
    )
    val sessionsWithSubjectAndInstructor: List<SessionAndSubjectAndInstructor>
)

data class SessionAndSubjectAndInstructor(
    @Embedded val session: Session,
    @Relation(
        entity = Subject::class,
        parentColumn = "subjectId",
        entityColumn = "id"
    )
    val subjectWithInstructor: SubjectWithInstructor?,
)

data class SubjectWithInstructor(
    @Embedded val subject: Subject,
    @Relation(
        parentColumn = "instructorId",
        entityColumn = "id"
    )
    val instructor: Instructor?
)