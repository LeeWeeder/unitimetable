package com.leeweeder.timetable.ui

import android.database.SQLException
import androidx.annotation.VisibleForTesting
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leeweeder.timetable.data.DataStoreRepository
import com.leeweeder.timetable.data.source.SessionAndSubjectAndInstructor
import com.leeweeder.timetable.data.source.instructor.Instructor
import com.leeweeder.timetable.data.source.instructor.InstructorDataSource
import com.leeweeder.timetable.data.source.session.Session
import com.leeweeder.timetable.data.source.session.SessionDataSource
import com.leeweeder.timetable.data.source.session.SessionType
import com.leeweeder.timetable.data.source.session.toEmptySession
import com.leeweeder.timetable.data.source.session.toSubjectSession
import com.leeweeder.timetable.data.source.subject.SubjectDataSource
import com.leeweeder.timetable.data.source.subject.SubjectWithInstructor
import com.leeweeder.timetable.data.source.subject.SubjectWithSessionCount
import com.leeweeder.timetable.data.source.timetable.TimeTable
import com.leeweeder.timetable.data.source.timetable.TimeTableDataSource
import com.leeweeder.timetable.ui.HomeEvent.*
import com.leeweeder.timetable.ui.HomeUiEvent.*
import com.leeweeder.timetable.ui.timetable_setup.DefaultTimeTable
import com.leeweeder.timetable.ui.util.getDays
import com.leeweeder.timetable.ui.util.getTimes
import com.leeweeder.timetable.util.toColor
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flattenConcat
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

class HomeViewModel(
    private val timeTableDataSource: TimeTableDataSource,
    private val sessionDataSource: SessionDataSource,
    private val subjectDataSource: SubjectDataSource,
    private val instructorDataSource: InstructorDataSource,
    dataStoreRepository: DataStoreRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    private val _eventFlow = MutableStateFlow<HomeUiEvent?>(null)
    val eventFlow: StateFlow<HomeUiEvent?> = _eventFlow.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val dataState = combine(
        timeTableDataSource.observeTimeTables(),
        dataStoreRepository.timeTablePrefFlow.map { it.mainTimeTableId }
    ) { timeTables, currentTimeTableId ->
        combine(
            flowOf(timeTables),
            timeTableDataSource.observeTimeTableWithSessionsWithSubjectAndInstructorOfId(
                currentTimeTableId
            ),
            subjectDataSource.observeFiveRecentlyAddedSubjectsWithSession()
        ) { timeTables, timeTableWithDetails, subjectsWithSessionCount ->
            val sessionsWithSubjectAndInstructor =
                timeTableWithDetails.sessionsWithSubjectAndInstructor

            val mainTimeTable = timeTables.find { it.id == currentTimeTableId }!!

            val instructors = instructorDataSource.getInstructors()

            _uiState.update { state ->
                state.copy(
                    selectedTimeTable = mainTimeTable,
                    timeTables = timeTables,
                    subjectsWithSessionCount = subjectsWithSessionCount
                )
            }

            HomeDataState.Success(
                mainTimeTable = mainTimeTable,
                dayScheduleMap = sessionsWithSubjectAndInstructor.toMappedSchedules(),
                instructors = instructors,
                sessionsWithSubjectAndInstructor = sessionsWithSubjectAndInstructor
            )
        }
    }.flattenConcat().catch {
        HomeDataState.Error(it)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), HomeDataState.Loading)

    fun onEvent(event: HomeEvent) {

        suspend fun upsertSubject(subject: com.leeweeder.timetable.data.source.subject.Subject): com.leeweeder.timetable.data.source.subject.Subject {
            return subjectDataSource.upsertSubject(subject).let { subjectId ->
                subjectDataSource.getSubjectById(subjectId)
            } ?: throw SQLException("Failed to retrieve subject after upsert")
        }

        suspend fun upsertSubjectWithInstructor(
            subject: com.leeweeder.timetable.data.source.subject.Subject,
            instructor: Instructor
        ): com.leeweeder.timetable.data.source.subject.Subject {
            val instructorId = if (instructorDataSource.getInstructorById(instructor.id) == null) {
                instructorDataSource.upsertInstructor(instructor)
            } else {
                instructor.id
            }

            val subjectToUpsert = subject.copy(instructorId = instructorId)

            return upsertSubject(subjectToUpsert)
        }

        when (event) {
            is SelectTimeTable -> {
                _uiState.update { state ->
                    state.copy(selectedTimeTable = state.timeTables.find { it.id == event.newTimeTableId }!!)
                }
            }

            is SaveSubject -> {
                try {
                    viewModelScope.launch {
                        val recentlyInsertedSubject =
                            upsertSubjectWithInstructor(event.newSubject, event.instructor)

                        onEvent(SetOnEditMode)
                        onEvent(SetCurrentSubject(recentlyInsertedSubject))

                        _eventFlow.emit(DoneAddingSubject)
                    }
                } catch (e: Exception) {
                    // TODO: Implement proper error handling
                    println(e.message)
                }
            }

            is SetSessionWithSubject -> {
                try {
                    viewModelScope.launch {
                        val session = event.session
                        val subjectId = session.subjectId
                        val activeSubjectId = uiState.value.activeSubject!!.id

                        if (session.subjectId == null || subjectId != activeSubjectId) {
                            sessionDataSource.updateSession(session.toSubjectSession(activeSubjectId))
                        } else {
                            sessionDataSource.updateSession(session.toEmptySession())
                        }
                    }
                } catch (e: Exception) {
                    // TODO: Implement proper error handling
                    println(e.message)
                }
            }

            SetOnDefaultMode -> {
                _uiState.update { state ->
                    state.copy(isOnEditMode = false, activeSubject = null)
                }
            }

            is SetOnEditMode -> {
                _uiState.update { state ->
                    state.copy(isOnEditMode = true)
                }
            }

            is SetCurrentSubject -> {
                _uiState.update { state ->
                    state.copy(activeSubject = event.subject)
                }
            }

            is SaveEditedSubject -> {
                try {
                    viewModelScope.launch {
                        upsertSubjectWithInstructor(event.newSubject, event.instructor)
                        _eventFlow.emit(DoneEditingSubject)
                    }
                } catch (e: Exception) {
                    // TODO: Implement proper error handling
                    println(e.message)
                }
            }

            ClearUiEvent -> {
                viewModelScope.launch {
                    _eventFlow.emit(null)
                }
            }

            is LoadToEditSubject -> {
                viewModelScope.launch {
                    _eventFlow.emit(
                        FinishedLoadingToBeEditedSubject(
                            subjectDataSource.getSubjectWithInstructor(event.subjectId)
                        )
                    )
                }
            }

            is DeleteSubject -> {
                viewModelScope.launch {
                    val subjectToDelete = event.subjectToDelete
                    subjectDataSource.deleteSubject(subjectToDelete)
                    _eventFlow.emit(FinishedDeletingSubject(subjectToDelete))
                }
            }

            is ReinsertSubject -> {
                viewModelScope.launch {
                    upsertSubject(event.subject)
                }
            }
        }
    }
}

sealed interface HomeUiEvent {
    data object DoneEditingSubject : HomeUiEvent
    data class FinishedLoadingToBeEditedSubject(
        val subjectWithInstructor: SubjectWithInstructor?
    ) : HomeUiEvent

    data object DoneAddingSubject : HomeUiEvent
    data class FinishedDeletingSubject(val deletedSubject: com.leeweeder.timetable.data.source.subject.Subject) :
        HomeUiEvent
}

sealed interface HomeEvent {
    data class SelectTimeTable(val newTimeTableId: Int) : HomeEvent
    data class SaveSubject(
        val newSubject: com.leeweeder.timetable.data.source.subject.Subject,
        val instructor: Instructor
    ) : HomeEvent

    data class ReinsertSubject(
        val subject: com.leeweeder.timetable.data.source.subject.Subject
    ) : HomeEvent

    data class SaveEditedSubject(
        val newSubject: com.leeweeder.timetable.data.source.subject.Subject,
        val instructor: Instructor
    ) : HomeEvent

    data class SetSessionWithSubject(val session: Session) :
        HomeEvent

    data object SetOnDefaultMode : HomeEvent
    data object SetOnEditMode : HomeEvent

    data class SetCurrentSubject(val subject: com.leeweeder.timetable.data.source.subject.Subject) :
        HomeEvent

    data object ClearUiEvent : HomeEvent
    data class LoadToEditSubject(val subjectId: Int) : HomeEvent
    data class DeleteSubject(val subjectToDelete: com.leeweeder.timetable.data.source.subject.Subject) :
        HomeEvent
}

sealed interface HomeDataState {
    data class Success(
        val mainTimeTable: TimeTable,
        val dayScheduleMap: Map<DayOfWeek, List<Schedule>>,
        val instructors: List<Instructor> = emptyList(),
        val sessionsWithSubjectAndInstructor: List<SessionAndSubjectAndInstructor> = emptyList()
    ) : HomeDataState

    data class Error(val throwable: Throwable) : HomeDataState
    data object Loading : HomeDataState
}

data class HomeUiState(
    val selectedTimeTable: TimeTable = DefaultTimeTable,
    val timeTables: List<TimeTable> = emptyList(),
    val subjectsWithSessionCount: List<SubjectWithSessionCount> = emptyList(),
    val isOnEditMode: Boolean = false,
    val dayOfWeekNow: DayOfWeek = LocalDate.now().dayOfWeek,
    val activeSubject: com.leeweeder.timetable.data.source.subject.Subject? = null
) {
    val startTimes: List<LocalTime>
        get() {
            val timeTable = timeTables.find { selectedTimeTable.id == it.id }

            if (timeTable == null)
                return emptyList()

            return getTimes(timeTable.startTime, timeTable.endTime)
        }

    val days: List<DayOfWeek>
        get() = getDays(selectedTimeTable.startingDay, selectedTimeTable.numberOfDays)
}

@VisibleForTesting
fun isSameSchedule(pair: Pair<Schedule, Schedule>): Boolean {
    val firstSession = pair.first
    val secondSession = pair.second

    val firstSessionType = firstSession.type
    val secondSessionType = secondSession.type

    if (firstSessionType != secondSessionType) {
        return false
    }

    return when (firstSessionType) {
        SessionType.Subject -> {
            firstSession.subject!!.id == secondSession.subject!!.id
        }

        SessionType.Vacant -> true

        SessionType.Break -> {
            firstSession.breakDescription == secondSession.breakDescription
        }

        SessionType.Empty -> false
    }
}


/*
* This will group sessions as one for the following case:
*
*   The succeeding session of the same type means:
*       - Subject:
*           if the same subject, set as one, if null, different schedule.
*       - Vacant:
*           succeeding session as vacant is one schedule
*       - Break:
*           if the same description, set as one schedule
*
*   To set the schedule as one, indicate the periodNumbers it is belonged to
* */
fun List<SessionAndSubjectAndInstructor>.toMappedSchedules(): Map<DayOfWeek, List<Schedule>> {
    val groupedSessions = this.groupBy { it.session.dayOfWeek }

    // For each items in the list, check if the current item and the next item is the same, else, merge them.

    return groupedSessions.mapValues { (_, sessions) ->
        // for each sessions, convert all of them to schedules
        val schedules = sessions.map {
            val periodSpan = 1

            when (it.session.type) {
                SessionType.Subject -> {
                    val subject =
                        it.subjectWithInstructor!!.subject.let { sub ->
                            Subject(
                                id = it.session.subjectId!!,
                                description = sub.description,
                                code = sub.code,
                                color = sub.color.toColor(),
                                instructor = it.subjectWithInstructor.instructor
                            )
                        }

                    Schedule.subjectSchedule(
                        subject = subject,
                        periodSpan = periodSpan
                    )
                }

                SessionType.Vacant -> {
                    Schedule.vacantSchedule(periodSpan)
                }

                SessionType.Break -> {
                    Schedule.breakSchedule(
                        breakDescription = it.session.breakDescription,
                        periodSpan = periodSpan
                    )
                }

                SessionType.Empty -> Schedule.emptySchedule()
            }
        }

        mergeSchedules(schedules)
    }
}

fun mergeSchedules(schedules: List<Schedule>): List<Schedule> {
    if (schedules.size <= 1) return schedules

    val result = mutableListOf<Schedule>()
    var currentGroup = mutableListOf(schedules[0])

    for (i in 1 until schedules.size) {
        val current = schedules[i]
        val previous = currentGroup.last()

        if (isSameSchedule(previous to current)) {
            currentGroup.add(current)
        } else {
            result.add(mergeSameSchedules(currentGroup))
            currentGroup = mutableListOf(current)
        }
    }

    if (currentGroup.isNotEmpty()) {
        result.add(mergeSameSchedules(currentGroup))
    }

    return result
}

fun mergeSameSchedules(schedules: List<Schedule>): Schedule {
    val periodSpan = schedules.run {
        var periodSpan = 0
        this.forEach {
            periodSpan += it.periodSpan
        }
        periodSpan
    }

    val first = schedules.first()

    return when (first.type) {
        SessionType.Subject -> Schedule.subjectSchedule(
            subject = first.subject!!,
            periodSpan = periodSpan
        )

        SessionType.Vacant -> Schedule.vacantSchedule(periodSpan)

        SessionType.Break -> Schedule.breakSchedule(
            breakDescription = first.breakDescription,
            periodSpan = periodSpan
        )

        SessionType.Empty -> Schedule.emptySchedule()
    }
}

@ConsistentCopyVisibility
data class Schedule private constructor(
    val subject: Subject?,
    val type: SessionType,
    val periodSpan: Int,
    val breakDescription: String?
) {
    /** Subject */
    private constructor(
        subject: Subject,
        periodSpan: Int
    ) : this(
        subject = subject,
        type = SessionType.Subject,
        periodSpan = periodSpan,
        breakDescription = null
    )

    /** Vacant */
    private constructor(
        periodSpan: Int
    ) : this(
        subject = null,
        type = SessionType.Vacant,
        periodSpan = periodSpan,
        breakDescription = null
    )

    /** Empty */
    private constructor() : this(
        subject = null,
        type = SessionType.Empty,
        periodSpan = 1,
        breakDescription = null
    )

    /** Break */
    private constructor(
        breakDescription: String?,
        periodSpan: Int
    ) : this(
        subject = null,
        type = SessionType.Break,
        periodSpan = periodSpan,
        breakDescription = breakDescription
    )

    companion object {
        /**
         * Creates a [com.leeweeder.timetable.ui.Schedule] with [SessionType.Subject] type.
         * */
        fun subjectSchedule(subject: Subject, periodSpan: Int) =
            Schedule(subject = subject, periodSpan = periodSpan)


        /**
         * Creates a [com.leeweeder.timetable.ui.Schedule] with [SessionType.Vacant] type.
         * */
        fun vacantSchedule(periodSpan: Int) =
            Schedule(periodSpan = periodSpan)

        /**
         * Creates a [com.leeweeder.timetable.ui.Schedule] with [SessionType.Break] type.
         * */
        fun breakSchedule(breakDescription: String?, periodSpan: Int) =
            Schedule(breakDescription = breakDescription, periodSpan = periodSpan)

        /**
         * Creates a [com.leeweeder.timetable.ui.Schedule] with [SessionType.Subject] type.
         * */
        fun emptySchedule() = Schedule()
    }
}

data class Subject(
    val id: Int,
    val description: String,
    val code: String,
    val color: Color,
    val instructor: Instructor?
) {
    override fun equals(other: Any?): Boolean {
        if (other !is Subject) {
            return false
        }

        if (this.id == other.id) {
            return true
        }

        return false
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + id
        return result
    }
}