package com.leeweeder.timetable.domain.relation

import androidx.room.DatabaseView
import androidx.room.Embedded
import com.leeweeder.timetable.domain.model.Instructor
import com.leeweeder.timetable.domain.model.Subject
import com.leeweeder.timetable.util.Hue

@DatabaseView(
    viewName = "subject_instructor_view",
    value = """
        SELECT
            si.id,
            si.hue,
            s.id AS subject_id,
            s.description AS subject_description,
            s.code AS subject_code,
            s.dateAdded AS subject_dateAdded,
            i.id AS instructor_id,
            i.name AS instructor_name
        FROM subjectinstructorcrossref si
        JOIN subject s ON si.subjectId = s.id
        LEFT JOIN instructor i ON si.instructorId = i.id
"""
)
data class SubjectInstructorCrossRefWithDetails(
    val id: Int,
    val hue: Hue,

    @Embedded(prefix = "subject_")
    val subject: Subject,

    @Embedded(prefix = "instructor_")
    val instructor: Instructor?
)