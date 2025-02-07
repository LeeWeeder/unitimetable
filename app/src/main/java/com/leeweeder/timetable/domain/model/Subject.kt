package com.leeweeder.timetable.domain.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

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
        fun createSubjectForInsertion(
            description: String,
            code: String,
            color: Color,
            instructorId: Int?
        ): Subject {
            return Subject(
                description = description,
                code = code,
                instructorId = instructorId,
                color = color.toArgb()
            )
        }

        fun createSubjectForUpdate(
            id: Int,
            description: String,
            code: String,
            color: Color,
            instructorId: Int?,
        ): Subject {
            return Subject(
                id = id,
                description = description,
                code = code,
                color = color.toArgb(),
                instructorId = instructorId
            )
        }
    }
}