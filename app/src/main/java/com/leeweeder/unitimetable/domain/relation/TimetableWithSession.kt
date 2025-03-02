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

import androidx.room.Embedded
import androidx.room.Relation
import com.leeweeder.unitimetable.domain.model.Instructor
import com.leeweeder.unitimetable.domain.model.SerializableTimetable
import com.leeweeder.unitimetable.domain.model.Session
import com.leeweeder.unitimetable.domain.model.Subject
import com.leeweeder.unitimetable.domain.model.Timetable
import com.leeweeder.unitimetable.util.Hue
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.DayOfWeek
import java.time.LocalTime

data class TimetableWithSession(
    @Embedded val timetable: Timetable,
    @Relation(
        entity = Session::class,
        parentColumn = "id",
        entityColumn = "timetableId"
    )
    val sessions: List<SessionWithDetails>
) {
    fun serialize(): SerializableTimeTableWithSession {
        return SerializableTimeTableWithSession(
            timetable = timetable.serialize(),
            sessions = sessions.map {
                SerializableSessionWithDetails(
                    session = SerializableSession(
                        id = it.session.id,
                        timetableId = it.session.timetableId,
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
        return this.serialize().toString()
    }

    companion object {
        fun fromJson(value: String): TimetableWithSession {
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
    val timetable: SerializableTimetable,
    val sessions: List<SerializableSessionWithDetails>
) {
    fun normalize(): TimetableWithSession {
        return TimetableWithSession(
            timetable = timetable.normalize(),
            sessions = sessions.map {
                SessionWithDetails(
                    session = Session(
                        id = it.session.id,
                        timetableId = it.session.timetableId,
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
data class SerializableSessionWithDetails(
    val session: SerializableSession,
    val subjectWithInstructor: SerializableSubjectInstructorCrossRefWithDetails?
)

@Serializable
data class SerializableSession(
    val id: Int = 0,
    val timetableId: Int,
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