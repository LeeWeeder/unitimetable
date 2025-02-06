package com.leeweeder.timetable.data.source.subject

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.leeweeder.timetable.data.source.instructor.Instructor
import com.leeweeder.timetable.data.source.session.Session

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Instructor::class,
            parentColumns = ["id"],
            childColumns = ["instructorId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("instructorId")
    ]
)
data class Subject(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val description: String,
    val code: String,
    val instructorId: Int?,
    val color: Int,
    val dateAdded: Long = System.currentTimeMillis()
) {
    companion object {
        fun createSubjectForInsertion(description: String, code: String, color: Color): Subject {
            return Subject(
                description = description,
                code = code,
                instructorId = null,
                color = color.toArgb()
            )
        }

        fun createSubjectForUpdate(
            id: Int,
            description: String,
            code: String,
            color: Color
        ): Subject {
            return Subject(
                id = id,
                description = description,
                code = code,
                color = color.toArgb(),
                instructorId = null
            )
        }
    }
}

fun Subject.withInstructorId(instructorId: Int) = this.copy(instructorId = instructorId)

data class SubjectWithSessionCount(
    @Embedded
    val subject: Subject,
    val sessionCount: Int
)

data class SubjectWithInstructor(
    @Embedded val subject: Subject,
    @Relation(
        parentColumn = "instructorId",
        entityColumn = "id"
    )
    val instructor: Instructor?
)

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