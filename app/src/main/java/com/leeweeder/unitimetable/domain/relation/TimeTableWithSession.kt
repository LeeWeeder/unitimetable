package com.leeweeder.unitimetable.domain.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.leeweeder.unitimetable.domain.model.Instructor
import com.leeweeder.unitimetable.domain.model.Session
import com.leeweeder.unitimetable.domain.model.Subject
import com.leeweeder.unitimetable.domain.model.TimeTable
import com.leeweeder.unitimetable.util.Hue
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.DayOfWeek
import java.time.LocalTime

data class TimeTableWithSession(
    @Embedded val timeTable: TimeTable,
    @Relation(
        entity = Session::class,
        parentColumn = "id",
        entityColumn = "timeTableId"
    )
    val sessions: List<SessionWithDetails>
) {
    private fun toSerializable(): SerializableTimeTableWithSession {
        return SerializableTimeTableWithSession(
            timeTable = SerializableTimeTable(
                id = timeTable.id,
                name = timeTable.name,
                numberOfDays = timeTable.numberOfDays,
                startingDay = timeTable.startingDay,
                startTimeHour = timeTable.startTime.hour,
                endTimeHour = timeTable.endTime.hour
            ),
            sessions = sessions.map {
                SerializableSessionWithDetails(
                    session = SerializableSession(
                        id = it.session.id,
                        timeTableId = it.session.timeTableId,
                        subjectInstructorCrossRefId = it.session.subjectInstructorCrossRefId,
                        dayOfWeek = it.session.dayOfWeek,
                        startTimeHour = it.session.startTime.hour,
                        label = it.session.label
                    ),
                    subjectWithInstructor = it.subjectWithInstructor?.let {
                        SerializableSubjectInstructorCrossRefWithDetails(
                            id = it.id,
                            hueValue = it.hue.value,
                            subject = SerializableSubject(
                                id = it.subject.id,
                                description = it.subject.description,
                                code = it.subject.code,
                                dateAdded = it.subject.dateAdded
                            ),
                            instructor = it.instructor?.let {
                                SerializableInstructor(
                                    id = it.id,
                                    name = it.name
                                )
                            }
                        )
                    }
                )
            }
        )
    }

    override fun toString(): String {
        return this.toSerializable().toString()
    }

    companion object {
        fun fromJson(value: String): TimeTableWithSession {
            return Json.decodeFromString<SerializableTimeTableWithSession>(value).normalize()
        }
    }
}

data class SessionWithDetails(
    @Embedded val session: Session,
    @Relation(
        parentColumn = "subjectInstructorCrossRefId",
        entityColumn = "id"
    )
    val subjectWithInstructor: SubjectInstructorCrossRefWithDetails?
)

@Serializable
data class SerializableTimeTableWithSession(
    val timeTable: SerializableTimeTable,
    val sessions: List<SerializableSessionWithDetails>
) {
    fun normalize(): TimeTableWithSession {
        return TimeTableWithSession(
            timeTable = TimeTable(
                id = timeTable.id,
                numberOfDays = timeTable.numberOfDays,
                startingDay = timeTable.startingDay,
                startTime = LocalTime.of(timeTable.startTimeHour, 0),
                endTime = LocalTime.of(timeTable.endTimeHour, 0)
            ),
            sessions = sessions.map {
                SessionWithDetails(
                    session = Session(
                        id = it.session.id,
                        timeTableId = it.session.timeTableId,
                        subjectInstructorCrossRefId = it.session.subjectInstructorCrossRefId,
                        dayOfWeek = it.session.dayOfWeek,
                        startTime = LocalTime.of(it.session.startTimeHour, 0),
                        label = it.session.label
                    ),
                    subjectWithInstructor = it.subjectWithInstructor?.let {
                        SubjectInstructorCrossRefWithDetails(
                            id = it.id,
                            hue = Hue(it.hueValue),
                            subject = Subject(
                                id = it.subject.id,
                                description = it.subject.description,
                                code = it.subject.code,
                                dateAdded = it.subject.dateAdded
                            ),
                            instructor = it.instructor?.let {
                                Instructor(
                                    id = it.id,
                                    name = it.name
                                )
                            }
                        )
                    }
                )
            }
        )
    }

    override fun toString(): String {
        return Json.encodeToString(this)
    }
}

@Serializable
data class SerializableTimeTable(
    val id: Int,
    val name: String,
    val numberOfDays: Int,
    val startingDay: DayOfWeek,
    val startTimeHour: Int,
    /** End time is exclusive. Meaning, end time of 5:00 PM, means the last period is 4:00-5:00 PM */
    val endTimeHour: Int
)

@Serializable
data class SerializableSessionWithDetails(
    val session: SerializableSession,
    val subjectWithInstructor: SerializableSubjectInstructorCrossRefWithDetails?
)

@Serializable
data class SerializableSession(
    val id: Int = 0,
    val timeTableId: Int,
    val subjectInstructorCrossRefId: Int?,
    val dayOfWeek: DayOfWeek,
    val startTimeHour: Int,
    val label: String?
)

@Serializable
data class SerializableSubjectInstructorCrossRefWithDetails(
    val id: Int,
    val hueValue: Int,
    val subject: SerializableSubject,
    val instructor: SerializableInstructor?
)

@Serializable
data class SerializableSubject(
    val id: Int,
    val description: String,
    val code: String,
    val dateAdded: Long = System.currentTimeMillis()
)

@Serializable
data class SerializableInstructor(
    val id: Int,
    val name: String
)