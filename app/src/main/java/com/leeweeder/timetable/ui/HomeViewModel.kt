package com.leeweeder.timetable.ui

import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.leeweeder.timetable.data.DataStoreRepository
import com.leeweeder.timetable.data.source.SessionAndSubjectAndInstructor
import com.leeweeder.timetable.data.source.TimeTableWithDetails
import com.leeweeder.timetable.data.source.instructor.Instructor
import com.leeweeder.timetable.data.source.instructor.InstructorDataSource
import com.leeweeder.timetable.data.source.session.Session
import com.leeweeder.timetable.data.source.session.SessionDataSource
import com.leeweeder.timetable.data.source.session.toEmptySession
import com.leeweeder.timetable.data.source.session.toSubjectSession
import com.leeweeder.timetable.data.source.subject.SubjectDataSource
import com.leeweeder.timetable.data.source.subject.SubjectWithDetails
import com.leeweeder.timetable.data.source.timetable.TimeTable
import com.leeweeder.timetable.data.source.timetable.TimeTableDataSource
import com.leeweeder.timetable.ui.HomeEvent.*
import com.leeweeder.timetable.ui.HomeUiEvent.*
import com.leeweeder.timetable.ui.timetable_setup.DefaultTimeTable
import com.leeweeder.timetable.ui.util.getDays
import com.leeweeder.timetable.ui.util.getTimes
import com.leeweeder.timetable.util.Destination
import com.leeweeder.timetable.util.toColor
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalTime

private const val TAG = "HomeViewModel"

class HomeViewModel(
    timeTableDataSource: TimeTableDataSource,
    private val sessionDataSource: SessionDataSource,
    private val subjectDataSource: SubjectDataSource,
    private val instructorDataSource: InstructorDataSource,
    dataStoreRepository: DataStoreRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    private val _eventFlow = MutableStateFlow<HomeUiEvent?>(null)
    val eventFlow: StateFlow<HomeUiEvent?> = _eventFlow.asStateFlow()

    init {
        val toBeEditedSubjectId =
            savedStateHandle.toRoute<Destination.Screen.HomeScreen>().subjectIdToBeEdited

        if (toBeEditedSubjectId != null) {
            onEvent(LoadToEditSubject(toBeEditedSubjectId))
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val dataState = dataStoreRepository.timeTablePrefFlow
        .flatMapLatest { timeTablePref ->
            combine(
                timeTableDataSource.observeTimeTablesWithDetails().onEach {
                    Log.d(TAG, "TimeTablesWithDetails emitted: $it")
                },
                subjectDataSource.observeFiveRecentlyAddedSubjects().onEach {
                    Log.d(TAG, "FiveRecentlyAddedSubjects emitted: $it")
                },
                instructorDataSource.observeInstructors().onEach {
                    Log.d(TAG, "Instructors emitted: $it")
                }
            ) { timeTableWithDetails, fiveRecentlyAddedSubjects, instructors ->
                Log.d(
                    TAG,
                    "Combine block executing with mainTableId: ${timeTablePref.mainTimeTableId}"
                )

                val mainTableId = timeTablePref.mainTimeTableId

                if (mainTableId == -1) {
                    return@combine HomeDataState.Loading
                }

                val mainTimeTableWithDetails = timeTableWithDetails
                    .find { it.timeTable.id == mainTableId }
                    ?: return@combine HomeDataState.Error(IllegalStateException("Main timetable not found"))

                val mainTimeTable = mainTimeTableWithDetails.timeTable

                _uiState.update { state ->
                    state.copy(
                        selectedTimeTable = mainTimeTable
                    )
                }

                HomeDataState.Success(
                    mainTimeTableId = mainTimeTable.id,
                    instructors = instructors,
                    fiveRecentlyAddedSubjects = fiveRecentlyAddedSubjects,
                    timeTableWithDetails = timeTableWithDetails
                )
            }
        }.catch {
            Log.e(TAG, "Error loading data", it)
            HomeDataState.Error(it)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), HomeDataState.Loading)

    fun onEvent(event: HomeEvent) {

        suspend fun upsertSubject(subject: com.leeweeder.timetable.data.source.subject.Subject): Int {
            return subjectDataSource.upsertSubject(subject)
        }

        suspend fun upsertSubjectWithInstructor(
            subject: com.leeweeder.timetable.data.source.subject.Subject,
            instructor: Instructor
        ): Int {
            val instructorId =
                if (instructorDataSource.getInstructorById(instructor.id) == null) {
                    instructorDataSource.upsertInstructor(instructor)
                } else {
                    instructor.id
                }

            val subjectToUpsert = subject.copy(instructorId = instructorId)

            return upsertSubject(subjectToUpsert)
        }

        when (event) {
            is SelectTimeTable -> {
                if (dataState.value is HomeDataState.Success) {
                    _uiState.update { state ->
                        state.copy(
                            selectedTimeTable = (dataState.value as HomeDataState.Success)
                                .timeTableWithDetails
                                .map { it.timeTable }
                                .find { it.id == event.newTimeTableId }!!
                        )
                    }
                }
            }

            is SaveSubject -> {
                try {
                    viewModelScope.launch {
                        val recentlyInsertedSubject =
                            upsertSubjectWithInstructor(event.newSubject, event.instructor)

                        onEvent(SetOnEditMode)
                        onEvent(SetActiveSubjectIdForEditing(recentlyInsertedSubject))

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
                        val activeSubjectId = uiState.value.activeSubjectIdForScheduling!!

                        if (session.subjectId == null || subjectId != activeSubjectId) {
                            sessionDataSource.updateSession(
                                session.toSubjectSession(
                                    activeSubjectId
                                )
                            )
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
                    state.copy(isOnEditMode = false, activeSubjectIdForScheduling = null)
                }
            }

            is SetOnEditMode -> {
                _uiState.update { state ->
                    state.copy(isOnEditMode = true)
                }
            }

            is SetActiveSubjectIdForEditing -> {
                _uiState.update { state ->
                    state.copy(activeSubjectIdForScheduling = event.id)
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
                            subjectDataSource.getSubjectWithDetails(event.subjectId)
                        )
                    )
                }
            }

            is DeleteSubject -> {
                viewModelScope.launch {
                    try {
                        val subjectToDelete = event.subjectToDelete
                        subjectDataSource.deleteSubject(subjectToDelete)
                        sessionDataSource.updateSessions(event.sessions.map { it.toEmptySession() })
                        // TODO: Extract this to a function or a repository/use case
                        _eventFlow.emit(FinishedDeletingSubject(subjectToDelete))
                    } catch (e: Exception) {
                        // TODO: Implement proper error handling
                        println(e)
                    }
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
        val subjectWithDetails: SubjectWithDetails?
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

    data class SetActiveSubjectIdForEditing(val id: Int) : HomeEvent

    data object ClearUiEvent : HomeEvent
    data class LoadToEditSubject(val subjectId: Int) : HomeEvent
    data class DeleteSubject(
        val subjectToDelete: com.leeweeder.timetable.data.source.subject.Subject,
        val sessions: List<Session>
    ) :
        HomeEvent
}

sealed interface HomeDataState {
    data class Success(
        val mainTimeTableId: Int,
        val instructors: List<Instructor> = emptyList(),
        val fiveRecentlyAddedSubjects: List<com.leeweeder.timetable.data.source.subject.Subject> = emptyList(),
        val timeTableWithDetails: List<TimeTableWithDetails> = emptyList(),
    ) : HomeDataState {

        fun getDayScheduleMap(timeTableId: Int): Map<DayOfWeek, List<Schedule>> {
            return getSessionsWithSubjectInstructor(timeTableId).toMappedSchedules()
        }

        fun getSessionsWithSubjectInstructor(timeTableId: Int): List<SessionAndSubjectAndInstructor> {
            return timeTableWithDetails
                .find { it.timeTable.id == timeTableId }
                ?.sessionsWithSubjectAndInstructor
                ?: emptyList()
        }

        val mainTimeTable: TimeTable
            get() = timeTables.find { it.id == mainTimeTableId }!!

        val timeTables: List<TimeTable>
            get() = timeTableWithDetails.map { it.timeTable }
    }

    data class Error(val throwable: Throwable) : HomeDataState
    data object Loading : HomeDataState
}

data class HomeUiState(
    val selectedTimeTable: TimeTable = DefaultTimeTable,
    val isOnEditMode: Boolean = false,
    val activeSubjectIdForScheduling: Int? = null
) {
    val startTimes: List<LocalTime>
        get() = getTimes(selectedTimeTable.startTime, selectedTimeTable.endTime)

    val days: List<DayOfWeek>
        get() = getDays(selectedTimeTable.startingDay, selectedTimeTable.numberOfDays)
}

@VisibleForTesting
fun isSameSchedule(pair: Pair<Schedule, Schedule>): Boolean {
    val firstSession = pair.first
    val secondSession = pair.second

    if (firstSession.subject != null && secondSession.subject != null) {
        return firstSession.subject.id == secondSession.subject.id
    }

    if (firstSession.label != null && secondSession.label != null) {
        return firstSession.label == secondSession.label
    }

    if (firstSession.subject == null && secondSession.subject == null && firstSession.label == null && secondSession.label == null) {
        return false
    }

    return false
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

            if (it.session.isSubject) {
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
            } else {
                Schedule.emptySchedule(
                    label = it.session.label,
                    periodSpan = periodSpan
                )
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

    return if (first.subject != null) {
        Schedule.subjectSchedule(first.subject, periodSpan)
    } else {
        Schedule.emptySchedule(first.label, periodSpan)
    }
}

@ConsistentCopyVisibility
data class Schedule private constructor(
    val subject: Subject?,
    val periodSpan: Int,
    val label: String?
) {
    /** Subject */
    private constructor(
        subject: Subject,
        periodSpan: Int
    ) : this(
        subject = subject,
        periodSpan = periodSpan,
        label = null
    )

    /** Empty */
    private constructor(
        label: String?,
        periodSpan: Int
    ) : this(
        subject = null,
        periodSpan = periodSpan,
        label = label
    )

    companion object {
        /**
         * Creates a subject [com.leeweeder.timetable.ui.Schedule].
         * */
        fun subjectSchedule(subject: Subject, periodSpan: Int) =
            Schedule(subject = subject, periodSpan = periodSpan)

        /**
         * Creates an empty [com.leeweeder.timetable.ui.Schedule].
         * */
        fun emptySchedule(label: String?, periodSpan: Int) =
            Schedule(label = label, periodSpan = periodSpan)
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