package com.leeweeder.timetable.domain.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.leeweeder.timetable.util.Hue

@Entity(
    indices = [
        Index("subjectId", "instructorId", unique = true),
        Index("subjectId"),
        Index("instructorId"),
    ],
    foreignKeys = [
        ForeignKey(
            entity = Subject::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Instructor::class,
            parentColumns = ["id"],
            childColumns = ["instructorId"],
            onDelete = ForeignKey.SET_NULL,
            onUpdate = ForeignKey.CASCADE,
        )
    ]
)
data class SubjectInstructorCrossRef(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val hue: Hue,
    val subjectId: Int,
    val instructorId: Int?
)
