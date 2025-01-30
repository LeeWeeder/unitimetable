package com.leeweeder.timetable.data.source.subject

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.leeweeder.timetable.data.source.instructor.Instructor

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
    val color: Long
)