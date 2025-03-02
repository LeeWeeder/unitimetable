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

package com.leeweeder.unitimetable.domain.relation

import androidx.room.DatabaseView
import androidx.room.Embedded
import com.leeweeder.unitimetable.domain.model.Instructor
import com.leeweeder.unitimetable.domain.model.Subject
import com.leeweeder.unitimetable.util.Hue

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