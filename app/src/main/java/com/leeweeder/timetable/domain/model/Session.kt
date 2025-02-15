package com.leeweeder.timetable.domain.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.DayOfWeek
import java.time.LocalTime

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = TimeTable::class,
            parentColumns = ["id"],
            childColumns = ["timeTableId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = SubjectInstructorCrossRef::class,
            parentColumns = ["id"],
            childColumns = ["subjectInstructorCrossRefId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("timeTableId"),
        Index("subjectInstructorCrossRefId"),
        Index("timeTableId", "dayOfWeek", "startTime", unique = true),
        Index("timeTableId", "subjectInstructorCrossRefId")
    ]
)

data class Session(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val timeTableId: Int,
    val subjectInstructorCrossRefId: Int?,
    val dayOfWeek: DayOfWeek,
    val startTime: LocalTime,
    val label: String?
) {
    init {
        require(!(subjectInstructorCrossRefId != null && label != null)) {
            "Cannot have both a subjectId and label at the same time"
        }
    }

    val isSubject: Boolean
        get() = subjectInstructorCrossRefId != null

    companion object {
        /**
         * Create a subject session
         * */
        fun subjectSession(
            timeTableId: Int,
            subjectId: Int,
            dayOfWeek: DayOfWeek,
            startTime: LocalTime
        ) = Session(
            timeTableId = timeTableId,
            subjectInstructorCrossRefId = subjectId,
            dayOfWeek = dayOfWeek,
            startTime = startTime,
            label = null
        )

        /**
         * Create an empty session
         * */
        fun emptySession(
            timeTableId: Int,
            dayOfWeek: DayOfWeek,
            startTime: LocalTime,
            label: String? = null
        ) = Session(
            timeTableId = timeTableId,
            dayOfWeek = dayOfWeek,
            startTime = startTime,
            subjectInstructorCrossRefId = null,
            label = label,
        )
    }
}

fun Session.toScheduledSession(subjectInstructorCrossRefId: Int): Session {
    return Session(
        timeTableId = timeTableId,
        subjectInstructorCrossRefId = subjectInstructorCrossRefId,
        dayOfWeek = dayOfWeek,
        startTime = startTime,
        id = id,
        label = null
    )
}

fun Session.toEmptySession(label: String? = null): Session {
    return Session(
        timeTableId = timeTableId,
        dayOfWeek = dayOfWeek,
        startTime = startTime,
        id = id,
        subjectInstructorCrossRefId = null,
        label = label
    )
}
