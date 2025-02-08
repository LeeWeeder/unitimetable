package com.leeweeder.timetable.domain.relation

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.leeweeder.timetable.domain.model.Instructor
import com.leeweeder.timetable.domain.model.Subject
import com.leeweeder.timetable.domain.model.SubjectInstructorCrossRef

data class SubjectWithInstructor(
    @Embedded val subject: Subject,
    @Relation(
        entity = Instructor::class,
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = SubjectInstructorCrossRef::class,
            parentColumn = "subjectId",
            entityColumn = "instructorId"
        )
    )
    val instructor: Instructor?,
)