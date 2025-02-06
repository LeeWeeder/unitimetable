package com.leeweeder.timetable.data.source.session

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.leeweeder.timetable.data.source.subject.Subject
import com.leeweeder.timetable.data.source.timetable.TimeTable
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
            entity = Subject::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("timeTableId"),
        Index("subjectId"),
        Index("timeTableId", "dayOfWeek", "startTime", unique = true)
    ]
)

data class Session(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val timeTableId: Int,
    val subjectId: Int?,
    val dayOfWeek: DayOfWeek,
    val startTime: LocalTime,
    val label: String?
) {
    init {
        require(!(subjectId != null && label != null)) {
            "Cannot have both a subjectId and label at the same time"
        }
    }

    val isSubject: Boolean
        get() = subjectId != null

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
            subjectId = subjectId,
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
            subjectId = null,
            label = label,
        )
    }
}

fun Session.toSubjectSession(subjectId: Int): Session {
    return Session(
        timeTableId = timeTableId,
        subjectId = subjectId,
        dayOfWeek = dayOfWeek,
        startTime = startTime,
        id = id,
        label = null
    )
}

fun Session.toEmptySession(): Session {
    return Session(
        timeTableId = timeTableId,
        dayOfWeek = dayOfWeek,
        startTime = startTime,
        id = id,
        subjectId = null,
        label = null
    )
}
