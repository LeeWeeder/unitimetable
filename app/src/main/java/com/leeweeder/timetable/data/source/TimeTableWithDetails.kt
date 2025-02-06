package com.leeweeder.timetable.data.source

import androidx.room.Embedded
import androidx.room.Relation
import com.leeweeder.timetable.data.source.session.Session
import com.leeweeder.timetable.data.source.subject.Subject
import com.leeweeder.timetable.data.source.subject.SubjectWithInstructor
import com.leeweeder.timetable.data.source.timetable.TimeTable

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