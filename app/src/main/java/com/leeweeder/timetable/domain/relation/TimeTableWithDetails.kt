package com.leeweeder.timetable.domain.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.leeweeder.timetable.domain.model.Instructor
import com.leeweeder.timetable.domain.model.Session
import com.leeweeder.timetable.domain.model.Subject
import com.leeweeder.timetable.domain.model.TimeTable

data class TimeTableWithDetails(
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