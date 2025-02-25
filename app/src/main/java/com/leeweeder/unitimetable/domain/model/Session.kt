package com.leeweeder.unitimetable.domain.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.DayOfWeek
import java.time.LocalTime

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Timetable::class,
            parentColumns = ["id"],
            childColumns = ["timetableId"],
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
        Index("timetableId"),
        Index("subjectInstructorCrossRefId"),
        Index("timetableId", "dayOfWeek", "startTime", unique = true),
        Index("timetableId", "subjectInstructorCrossRefId")
    ]
)

data class Session(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val timetableId: Int,
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
            crossRefId: Int,
            dayOfWeek: DayOfWeek,
            startTime: LocalTime
        ) = Session(
            timetableId = timeTableId,
            subjectInstructorCrossRefId = crossRefId,
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
            timetableId = timeTableId,
            dayOfWeek = dayOfWeek,
            startTime = startTime,
            subjectInstructorCrossRefId = null,
            label = label,
        )
    }
}

fun Session.toScheduledSession(subjectInstructorCrossRefId: Int): Session {
    return Session(
        timetableId = timetableId,
        subjectInstructorCrossRefId = subjectInstructorCrossRefId,
        dayOfWeek = dayOfWeek,
        startTime = startTime,
        id = id,
        label = null
    )
}

fun Session.toEmptySession(label: String? = null): Session {
    return Session(
        timetableId = timetableId,
        dayOfWeek = dayOfWeek,
        startTime = startTime,
        id = id,
        subjectInstructorCrossRefId = null,
        label = label
    )
}
