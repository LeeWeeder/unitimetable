package com.leeweeder.timetable.domain.relation

import androidx.room.DatabaseView
import androidx.room.Relation
import com.leeweeder.timetable.domain.model.Instructor
import com.leeweeder.timetable.domain.model.Subject

@DatabaseView(
    viewName = "subject_instructor_view",
    value = "SELECT " +
            "si.id AS id, " +
            "si.subjectId, " +
            "si.instructorId, " +
            "s.*, " +
            "i.* " +
            "FROM subjectinstructorcrossref si " +
            "JOIN Subject s ON si.subjectId = s.id " +
            "JOIN Instructor i ON si.instructorId = i.id"
)
data class SubjectInstructorWithId(
    val id: Int,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
    )
    val subject: Subject,

    @Relation(
        parentColumn = "id",
        entityColumn = "id"
    )
    val instructor: Instructor
)
