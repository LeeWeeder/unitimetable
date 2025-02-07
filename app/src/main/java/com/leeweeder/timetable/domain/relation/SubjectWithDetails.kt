package com.leeweeder.timetable.domain.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.leeweeder.timetable.domain.model.Instructor
import com.leeweeder.timetable.domain.model.Session
import com.leeweeder.timetable.domain.model.Subject

data class SubjectWithDetails(
    @Embedded val subject: Subject,
    @Relation(
        parentColumn = "instructorId",
        entityColumn = "id"
    )
    val instructor: Instructor?,
    @Relation(
        entity = Session::class,
        parentColumn = "id",
        entityColumn = "subjectId"
    )
    val sessions: List<Session>
)