package com.leeweeder.timetable.ui

import com.leeweeder.timetable.data.source.SessionAndSubjectAndInstructor
import com.leeweeder.timetable.data.source.SubjectWithInstructor
import com.leeweeder.timetable.data.source.session.Session
import com.leeweeder.timetable.data.source.session.SessionType
import com.leeweeder.timetable.data.source.subject.Subject
import org.junit.Assert
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalTime

class HomeViewModelTest {


    @Test
    fun `should return three schedules from 5 schedules with 2 pair of sessions of one having the same subject and the other is vacant`() {
        val default = SessionAndSubjectAndInstructor(
            session = Session.subjectSession(
                timeTableId = 1,
                dayOfWeek = DayOfWeek.SATURDAY,
                startTime = LocalTime.of(8, 0),
                subjectId = 1
            ),
            subjectWithInstructor = SubjectWithInstructor(
                subject = Subject(
                    description = "Math",
                    id = 1,
                    code = "Test",
                    instructorId = null,
                    color = 0xFFFFFFFF
                ),
                instructor = null
            )
        )

        val sessions = listOf(
            default,
            default.copy(
                session = default.session.copy(
                    subjectId = 2,
                    startTime = default.session.startTime.plusHours(1L)
                )
            ),
            default.copy(
                session = default.session.copy(
                    startTime = default.session.startTime.plusHours(
                        2L
                    ), subjectId = 2
                )
            ),
            default.copy(
                session = default.session.copy(
                    subjectId = null,
                    startTime = default.session.startTime.plusHours(
                        3L
                    ),
                    type = SessionType.Vacant
                ),
                subjectWithInstructor = null
            ),
            default.copy(
                session = default.session.copy(
                    subjectId = null,
                    startTime = default.session.startTime.plusHours(
                        4L
                    ),
                    type = SessionType.Vacant
                ),
                subjectWithInstructor = null
            ),
        )

        println("\nInput sessions:")
        sessions.forEachIndexed { index, session ->
            println("Session $index: type=${session.session.type}, subjectId=${session.session.subjectId}, period=${session.session.startTime}")
        }

        val result = sessions.toMappedSchedules().entries.first().value

        result.forEachIndexed { index, schedule ->
            println("Schedule $index: type=${schedule.type}, subject=${schedule.subject?.id}, periods=${schedule.periodSpan}")
        }
        Assert.assertEquals(3, result.size)
    }

    @Test
    fun `should return one schedule for one session of type subject with subject`() {
        val default = SessionAndSubjectAndInstructor(
            session = Session.subjectSession(
                timeTableId = 1,
                dayOfWeek = DayOfWeek.SATURDAY,
                startTime = LocalTime.of(8, 0),
                subjectId = 1
            ),
            subjectWithInstructor = SubjectWithInstructor(
                subject = Subject(
                    description = "Math",
                    id = 1,
                    code = "Test",
                    instructorId = null,
                    color = 0xFFFFFFFF
                ),
                instructor = null
            )
        )

        val sessions = listOf(
            default
        )

        val result = sessions.toMappedSchedules().entries.first().value.size

        Assert.assertEquals(1, result)
    }

    @Test
    fun `should return one schedule for one session of type subject without subject`() {
        val default = SessionAndSubjectAndInstructor(
            session = Session.emptySession(
                timeTableId = 1,
                dayOfWeek = DayOfWeek.SATURDAY,
                startTime = LocalTime.of(8, 0)
            ),
            subjectWithInstructor = null
        )

        val sessions = listOf(
            default
        )

        val result = sessions.toMappedSchedules().entries.first().value.size

        Assert.assertEquals(1, result)
    }

    @Test
    fun `should return one schedule for one session of type vacant`() {
        val default = SessionAndSubjectAndInstructor(
            session = Session.vacantSession(
                timeTableId = 1,
                dayOfWeek = DayOfWeek.SATURDAY,
                startTime = LocalTime.of(8, 0)
            ),
            subjectWithInstructor = null
        )

        val sessions = listOf(
            default
        )

        val result = sessions.toMappedSchedules().entries.first().value.size

        Assert.assertEquals(1, result)
    }

    @Test
    fun `should return one schedule for one session of type break without break description`() {
        val default = SessionAndSubjectAndInstructor(
            session = Session.breakSession(
                timeTableId = 1,
                dayOfWeek = DayOfWeek.SATURDAY,
                startTime = LocalTime.of(8, 0),
                breakDescription = null
            ),
            subjectWithInstructor = null
        )

        val sessions = listOf(
            default
        )

        val result = sessions.toMappedSchedules().entries.first().value.size

        Assert.assertEquals(1, result)
    }

    @Test
    fun `should return one schedule for one session of type break with break description`() {
        val default = SessionAndSubjectAndInstructor(
            session = Session.breakSession(
                timeTableId = 1,
                dayOfWeek = DayOfWeek.SATURDAY,
                startTime = LocalTime.of(8, 0),
                breakDescription = "This is a break description"
            ),
            subjectWithInstructor = null
        )

        val sessions = listOf(
            default
        )

        val result = sessions.toMappedSchedules().entries.first().value.size

        Assert.assertEquals(1, result)
    }

    @Test
    fun `should return one schedule for two sessions of the same subject type`() {
        val default = SessionAndSubjectAndInstructor(
            session = Session.subjectSession(
                timeTableId = 1,
                dayOfWeek = DayOfWeek.SATURDAY,
                startTime = LocalTime.of(8, 0),
                subjectId = 1
            ),
            subjectWithInstructor = SubjectWithInstructor(
                subject = Subject(
                    description = "Math",
                    id = 1,
                    code = "Test",
                    instructorId = null,
                    color = 0xFFFFFFFF
                ),
                instructor = null
            )
        )

        val sessions = listOf(
            default,
            default.copy(
                session = default.session.copy(
                    startTime = default.session.startTime.plusHours(
                        1L
                    )
                )
            )
        )

        val result = sessions.toMappedSchedules().entries.first().value.size

        Assert.assertEquals(1, result)
    }

    @Test
    fun `should return two schedules for three sessions for same subject type with one different subject`() {
        val default = SessionAndSubjectAndInstructor(
            session = Session.subjectSession(
                timeTableId = 1,
                dayOfWeek = DayOfWeek.SATURDAY,
                startTime = LocalTime.of(8, 0),
                subjectId = 1
            ),
            subjectWithInstructor = SubjectWithInstructor(
                subject = Subject(
                    description = "Math",
                    id = 1,
                    code = "Test",
                    instructorId = null,
                    color = 0xFFFFFFFF
                ),
                instructor = null
            )
        )

        val sessions = listOf(
            default,
            default.copy(
                session = default.session.copy(
                    startTime = default.session.startTime.plusHours(
                        1L
                    )
                )
            ),
            default.copy(
                session = default.session.copy(
                    startTime = default.session.startTime.plusHours(
                        2L
                    ), subjectId = 2
                )
            )
        )

        val result = sessions.toMappedSchedules().entries.first().value.size

        Assert.assertEquals(2, result)
    }

    @Test
    fun `should return one schedule for three sessions of the same subject type`() {
        val default = SessionAndSubjectAndInstructor(
            session = Session.subjectSession(
                timeTableId = 1,
                dayOfWeek = DayOfWeek.SATURDAY,
                startTime = LocalTime.of(8, 0),
                subjectId = 1
            ),
            subjectWithInstructor = SubjectWithInstructor(
                subject = Subject(
                    description = "Math",
                    id = 1,
                    code = "Test",
                    instructorId = null,
                    color = 0xFFFFFFFF
                ),
                instructor = null
            )
        )

        val sessions = listOf(
            default,
            default.copy(
                session = default.session.copy(
                    startTime = default.session.startTime.plusHours(
                        1L
                    )
                )
            ),
            default.copy(
                session = default.session.copy(
                    startTime = default.session.startTime.plusHours(
                        2L
                    )
                )
            ),
        )

        val result = sessions.toMappedSchedules().entries.first().value.size

        Assert.assertEquals(1, result)
    }
}