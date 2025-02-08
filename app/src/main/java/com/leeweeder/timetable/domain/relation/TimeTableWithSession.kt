package com.leeweeder.timetable.domain.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.leeweeder.timetable.domain.model.Session
import com.leeweeder.timetable.domain.model.TimeTable

data class TimeTableWithSession(
    @Embedded val timeTable: TimeTable,
    @Relation(
        entity = Session::class,
        parentColumn = "id",
        entityColumn = "timeTableId"
    )
    val sessions: List<SessionWithDetails>
)

data class SessionWithDetails(
    @Embedded val session: Session,
    @Relation(
        parentColumn = "subjectInstructorCrossRefId",
        entityColumn = "id"
    )
    val subjectWithInstructor: SubjectInstructorWithId?
)