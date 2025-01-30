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
    val type: SessionType,
    val breakDescription: String?
) {
    init {
        when (type) {
            SessionType.Subject -> {
                require(subjectId != null) {
                    "Session is of Subject type yet having null subjectId. Use Vacant or Break instead or provide subject."
                }
            }

            SessionType.Vacant -> {
                require(subjectId == null && breakDescription == null) {
                    "Session is of Vacant type yet having non-null subjectId or breakDescription. Use Subject or Break instead or remove subject and description."
                }
            }

            SessionType.Break -> {
                require(subjectId == null) {
                    "Session is of Break type yet having non-null subjectId. Use Subject or Vacant instead or remove subject."
                }
            }

            SessionType.Empty -> {
                require(subjectId == null && breakDescription == null) {
                    "Session is of Empty type yet having non-null subjectId or breakDescription. Use Subject or Break instead or remove subject and description."
                }
            }
        }
    }

    /**
     * Constructor for a new session with type [SessionType.Subject]
     * */
    private constructor(
        timeTableId: Int,
        subjectId: Int,
        dayOfWeek: DayOfWeek,
        startTime: LocalTime
    ) : this(
        timeTableId = timeTableId,
        subjectId = subjectId,
        dayOfWeek = dayOfWeek,
        startTime = startTime,
        type = SessionType.Subject,
        breakDescription = null
    )

    /**
     * Constructor for a new session with type [SessionType.Vacant] or [SessionType.Empty]
     * */
    private constructor(
        timeTableId: Int,
        dayOfWeek: DayOfWeek,
        startTime: LocalTime,
        type: SessionType
    ) : this(
        timeTableId = timeTableId,
        subjectId = null,
        dayOfWeek = dayOfWeek,
        startTime = startTime,
        type = type,
        breakDescription = null
    )

    /**
     * Constructor for a new session with type [SessionType.Break]
     * */

    private constructor(
        timeTableId: Int,
        dayOfWeek: DayOfWeek,
        startTime: LocalTime,
        breakDescription: String?
    ) : this(
        timeTableId = timeTableId,
        subjectId = null,
        dayOfWeek = dayOfWeek,
        startTime = startTime,
        type = SessionType.Break,
        breakDescription = breakDescription
    )

    companion object {
        /**
         * Create a session of [SessionType.Subject]
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
            startTime = startTime
        )

        /**
         * Create a session of [SessionType.Empty]
         * */
        fun emptySession(
            timeTableId: Int,
            dayOfWeek: DayOfWeek,
            startTime: LocalTime
        ) = Session(
            timeTableId = timeTableId,
            dayOfWeek = dayOfWeek,
            startTime = startTime,
            type = SessionType.Empty
        )

        /**
         * Create a session of [SessionType.Vacant]
         * */
        fun vacantSession(
            timeTableId: Int,
            dayOfWeek: DayOfWeek,
            startTime: LocalTime
        ) = Session(
            timeTableId = timeTableId,
            dayOfWeek = dayOfWeek,
            startTime = startTime,
            type = SessionType.Vacant
        )

        /**
         * Create a session of [SessionType.Break]
         * */
        fun breakSession(
            timeTableId: Int,
            dayOfWeek: DayOfWeek,
            startTime: LocalTime,
            breakDescription: String?
        ) = Session(
            timeTableId = timeTableId,
            dayOfWeek = dayOfWeek,
            startTime = startTime,
            breakDescription = breakDescription
        )
    }
}

enum class SessionType {
    Subject,
    Vacant,
    Break,
    Empty
}
