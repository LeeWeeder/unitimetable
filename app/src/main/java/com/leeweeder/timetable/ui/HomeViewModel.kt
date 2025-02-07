package com.leeweeder.timetable.ui

import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.leeweeder.timetable.domain.model.Instructor
import com.leeweeder.timetable.domain.model.Session
import com.leeweeder.timetable.domain.model.Subject
import com.leeweeder.timetable.domain.model.TimeTable
import com.leeweeder.timetable.domain.relation.SessionAndSubjectAndInstructor
import com.leeweeder.timetable.domain.relation.SubjectWithDetails
import com.leeweeder.timetable.domain.relation.TimeTableWithDetails
import com.leeweeder.timetable.domain.repository.DataStoreRepository
import com.leeweeder.timetable.domain.repository.InstructorRepository
import com.leeweeder.timetable.domain.repository.SessionRepository
import com.leeweeder.timetable.domain.repository.SubjectRepository
import com.leeweeder.timetable.domain.repository.TimeTableRepository
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
    timeTableRepository: TimeTableRepository,
    private val sessionRepository: SessionRepository,
    private val subjectRepository: SubjectRepository,
    private val instructorRepository: InstructorRepository,
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
                timeTableRepository.observeTimeTableWithDetails().onEach {
                    Log.d(TAG, "TimeTablesWithDetails emitted: $it")
                },
                instructorRepository.observeInstructors().onEach {
                    Log.d(TAG, "Instructors emitted: $it")
                }
            ) { timeTableWithDetails, instructors ->
                Log.d(
                    TAG,
                    "Combine block executing with mainTableId: ${timeTablePref.mainTimeTableId}"
                )

                val mainTableId = timeTablePref.mainTimeTableId

                if (mainTableId == -1) {
                    return@combine HomeDataState.Loading
                }

                fun findTimeTableWithDetailsById(id: Int): TimeTableWithDetails? {
                    return timeTableWithDetails
                        .find { it.timeTable.id == id }
                }

                val mainTimeTableWithDetails = findTimeTableWithDetailsById(mainTableId)
                    ?: return@combine HomeDataState.Error(IllegalStateException("Main timetable not found"))

                val mainTimeTable = mainTimeTableWithDetails.timeTable

                _uiState.update { state ->
                    val passedSelectedTimeTableId =
                        savedStateHandle.toRoute<Destination.Screen.HomeScreen>().selectedTimeTableId

                    Log.d(TAG, "Passed selected time table id: $passedSelectedTimeTableId")

                    state.copy(
                        selectedTimeTable = findTimeTableWithDetailsById(passedSelectedTimeTableId)?.timeTable
                            ?: mainTimeTable
                    )
                }

                Log.d(TAG, "Selected time table id: ${uiState.value.selectedTimeTable.id}")

                HomeDataState.Success(
                    mainTimeTableId = mainTimeTable.id,
                    instructors = instructors,
                    timeTableWithDetails = timeTableWithDetails
                )
            }
        }.catch {
            Log.e(TAG, "Error loading data", it)
            HomeDataState.Error(it)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), HomeDataState.Loading)

    fun onEvent(event: HomeEvent) {

        suspend fun upsertSubject(subject: Subject, instructor: Instructor?): Int {
            return subjectRepository.upsertSubject(subject, instructor)
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
                        val recentlyInsertedSubject = upsertSubject(event.subject, event.instructor)

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
                        val session = event.sessionId
                        val sessionId = session.id
                        val subjectId = session.subjectId
                        val activeSubjectId = uiState.value.activeSubjectIdForScheduling!!

                        if (subjectId == null || subjectId != activeSubjectId) {
                            sessionRepository.updateSession(id = sessionId, subjectId = activeSubjectId)
                        } else {
                            sessionRepository.updateSession(id = sessionId, label = null)
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
                        upsertSubject(event.newSubject, event.instructor)
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
                            subjectRepository.getSubjectWithDetailsById(event.subjectId)
                        )
                    )
                }
            }

            is DeleteSubject -> {
                viewModelScope.launch {
                    try {
                        val subjectToDelete = event.subjectToDelete
                        subjectRepository.deleteSubjectById(subjectToDelete.id)
                        _eventFlow.emit(FinishedDeletingSubject(subjectToDelete))
                    } catch (e: Exception) {
                        // TODO: Implement proper error handling
                        println(e)
                    }
                }
            }

            is ReinsertSubject -> {
                viewModelScope.launch {
                    upsertSubject(event.subject, instructor = null)
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
    data class FinishedDeletingSubject(val deletedSubject: Subject) :
        HomeUiEvent
}

sealed interface HomeEvent {
    data class SelectTimeTable(val newTimeTableId: Int) : HomeEvent
    data class SaveSubject(
        val subject: Subject,
        val instructor: Instructor
    ) : HomeEvent

    data class ReinsertSubject(
        val subject: Subject
    ) : HomeEvent

    data class SaveEditedSubject(
        val newSubject: Subject,
        val instructor: Instructor
    ) : HomeEvent

    data class SetSessionWithSubject(val sessionId: Session) :
        HomeEvent

    data object SetOnDefaultMode : HomeEvent
    data object SetOnEditMode : HomeEvent

    data class SetActiveSubjectIdForEditing(val id: Int) : HomeEvent

    data object ClearUiEvent : HomeEvent
    data class LoadToEditSubject(val subjectId: Int) : HomeEvent
    data class DeleteSubject(
        val subjectToDelete: Subject
    ) :
        HomeEvent
}

sealed interface HomeDataState {
    data class Success(
        val mainTimeTableId: Int,
        val instructors: List<Instructor> = emptyList(),
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

    if (firstSession.subjectWrapper != null && secondSession.subjectWrapper != null) {
        return firstSession.subjectWrapper.id == secondSession.subjectWrapper.id
    }

    if (firstSession.label != null && secondSession.label != null) {
        return firstSession.label == secondSession.label
    }

    if (firstSession.subjectWrapper == null && secondSession.subjectWrapper == null && firstSession.label == null && secondSession.label == null) {
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
                val subjectWrapper =
                    it.subjectWithInstructor!!.subject.let { sub ->
                        SubjectWrapper(
                            id = it.session.subjectId!!,
                            description = sub.description,
                            code = sub.code,
                            color = sub.color.toColor(),
                            instructor = it.subjectWithInstructor.instructor
                        )
                    }

                Schedule.subjectSchedule(
                    subjectWrapper = subjectWrapper,
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

    return if (first.subjectWrapper != null) {
        Schedule.subjectSchedule(first.subjectWrapper, periodSpan)
    } else {
        Schedule.emptySchedule(first.label, periodSpan)
    }
}

@ConsistentCopyVisibility
data class Schedule private constructor(
    val subjectWrapper: SubjectWrapper?,
    val periodSpan: Int,
    val label: String?
) {
    /** Subject */
    private constructor(
        subjectWrapper: SubjectWrapper,
        periodSpan: Int
    ) : this(
        subjectWrapper = subjectWrapper,
        periodSpan = periodSpan,
        label = null
    )

    /** Empty */
    private constructor(
        label: String?,
        periodSpan: Int
    ) : this(
        subjectWrapper = null,
        periodSpan = periodSpan,
        label = label
    )

    companion object {
        /**
         * Creates a subject [com.leeweeder.timetable.ui.Schedule].
         * */
        fun subjectSchedule(subjectWrapper: SubjectWrapper, periodSpan: Int) =
            Schedule(subjectWrapper = subjectWrapper, periodSpan = periodSpan)

        /**
         * Creates an empty [com.leeweeder.timetable.ui.Schedule].
         * */
        fun emptySchedule(label: String?, periodSpan: Int) =
            Schedule(label = label, periodSpan = periodSpan)
    }
}

data class SubjectWrapper(
    val id: Int,
    val description: String,
    val code: String,
    val color: Color,
    val instructor: Instructor?
) {
    override fun equals(other: Any?): Boolean {
        if (other !is SubjectWrapper) {
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