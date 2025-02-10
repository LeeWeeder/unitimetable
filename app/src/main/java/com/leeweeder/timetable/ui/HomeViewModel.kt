package com.leeweeder.timetable.ui

import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.leeweeder.timetable.domain.model.Instructor
import com.leeweeder.timetable.domain.model.Session
import com.leeweeder.timetable.domain.model.Subject
import com.leeweeder.timetable.domain.model.TimeTable
import com.leeweeder.timetable.domain.relation.SessionWithDetails
import com.leeweeder.timetable.domain.relation.TimeTableWithSession
import com.leeweeder.timetable.domain.repository.DataStoreRepository
import com.leeweeder.timetable.domain.repository.SessionRepository
import com.leeweeder.timetable.domain.repository.SubjectInstructorRepository
import com.leeweeder.timetable.domain.repository.TimeTableRepository
import com.leeweeder.timetable.ui.HomeEvent.*
import com.leeweeder.timetable.ui.components.selection_and_addition_bottom_sheet.SelectionAndAdditionBottomSheetStateFactory
import com.leeweeder.timetable.ui.timetable_setup.DefaultTimeTable
import com.leeweeder.timetable.ui.util.getDays
import com.leeweeder.timetable.ui.util.getTimes
import com.leeweeder.timetable.util.Destination
import com.leeweeder.timetable.util.Hue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalTime

private const val TAG = "HomeViewModel"

class HomeViewModel(
    timeTableRepository: TimeTableRepository,
    private val sessionRepository: SessionRepository,
    private val subjectInstructorRepository: SubjectInstructorRepository,
    dataStoreRepository: DataStoreRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val homeDataState = dataStoreRepository.timeTablePrefFlow
        .flatMapLatest { timeTablePref ->
            timeTableRepository.observeTimeTableWithDetails().map { timeTableWithDetails ->
                val mainTableId = timeTablePref.mainTimeTableId

                if (mainTableId == -1) {
                    return@map HomeDataState.Loading
                }

                fun findTimeTableWithDetailsById(id: Int): TimeTableWithSession? {
                    return timeTableWithDetails
                        .find { it.timeTable.id == id }
                }

                val mainTimeTableWithDetails = findTimeTableWithDetailsById(mainTableId)
                    ?: return@map HomeDataState.Error(IllegalStateException("Main timetable not found"))

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
                    timeTableWithDetails = timeTableWithDetails
                )
            }
        }.catch {
            Log.e(TAG, "Error loading data", it)
            HomeDataState.Error(it)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), HomeDataState.Loading)

    private val selectionAndAdditionBottomSheetStateFactory =
        SelectionAndAdditionBottomSheetStateFactory(viewModelScope)

    val scheduleEntryBottomSheetState =
        selectionAndAdditionBottomSheetStateFactory
            .create(
                subjectInstructorRepository.observeSubjectInstructors()
            ) { subjectInstructor, searchQuery ->
                val searchQuery = searchQuery.lowercase()
                subjectInstructor.instructor.name.lowercase().contains(searchQuery) ||
                        subjectInstructor.subject.code.lowercase().contains(searchQuery) ||
                        subjectInstructor.subject.description.lowercase().contains(searchQuery)
            }

    fun onEvent(event: HomeEvent) {
        when (event) {
            is SelectTimeTable -> {
                if (homeDataState.value is HomeDataState.Success) {
                    _uiState.update { state ->
                        state.copy(
                            selectedTimeTable = (homeDataState.value as HomeDataState.Success)
                                .timeTableWithDetails
                                .map { it.timeTable }
                                .find { it.id == event.newTimeTableId }!!
                        )
                    }
                }
            }

            is SetSessionWithActiveSubjectInstructor -> {
                try {
                    viewModelScope.launch {
                        val session = event.sessionId
                        val sessionId = session.id
                        val subjectInstructorId = session.subjectInstructorCrossRefId
                        val activeSubjectInstructorCrossRefId =
                            uiState.value.activeSubjectInstructorIdForScheduling!!

                        if (subjectInstructorId == null || subjectInstructorId != activeSubjectInstructorCrossRefId) {
                            sessionRepository.updateSession(
                                id = sessionId,
                                crossRefId = activeSubjectInstructorCrossRefId
                            )
                        } else {
                            sessionRepository.updateSession(id = sessionId, label = null)
                        }
                    }
                } catch (e: Exception) {
                    // TODO: Implement proper error handling
                    println(e.message)
                }
            }

            SetToDefaultMode -> {
                _uiState.update { state ->
                    state.copy(
                        isOnEditMode = false,
                        activeSubjectInstructorIdForScheduling = null
                    )
                }
            }

            is SetToEditMode -> {
                _uiState.update { state ->
                    state.copy(
                        activeSubjectInstructorIdForScheduling = event.id,
                        isOnEditMode = true
                    )
                }
            }
        }
    }
}

sealed interface HomeEvent {
    data class SelectTimeTable(val newTimeTableId: Int) : HomeEvent

    data class SetSessionWithActiveSubjectInstructor(val sessionId: Session) :
        HomeEvent

    data object SetToDefaultMode : HomeEvent
    data class SetToEditMode(val id: Int) : HomeEvent
}

sealed interface HomeDataState {
    data class Success(
        val mainTimeTableId: Int,
        val timeTableWithDetails: List<TimeTableWithSession> = emptyList()
    ) : HomeDataState {

        fun getDayScheduleMap(timeTableId: Int): Map<DayOfWeek, List<Schedule>> {
            return getSessionsWithSubjectInstructor(timeTableId).toMappedSchedules()
        }

        fun getSessionsWithSubjectInstructor(timeTableId: Int): List<SessionWithDetails> {
            return timeTableWithDetails
                .find { it.timeTable.id == timeTableId }
                ?.sessions
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
    val activeSubjectInstructorIdForScheduling: Int? = null
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

    if (firstSession.subjectInstructor != null && secondSession.subjectInstructor != null) {
        return firstSession.subjectInstructor.id == secondSession.subjectInstructor.id
    }

    if (firstSession.label != null && secondSession.label != null) {
        return firstSession.label == secondSession.label
    }

    if (firstSession.subjectInstructor == null && secondSession.subjectInstructor == null && firstSession.label == null && secondSession.label == null) {
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
fun List<SessionWithDetails>.toMappedSchedules(): Map<DayOfWeek, List<Schedule>> {
    val groupedSessions = this.groupBy { it.session.dayOfWeek }

    // For each items in the list, check if the current item and the next item is the same, else, merge them.

    return groupedSessions.mapValues { (_, sessions) ->
        // for each sessions, convert all of them to schedules
        val schedules = sessions.map {
            val periodSpan = 1

            if (it.session.isSubject) {
                val subjectInstructor =
                    SubjectInstructor(
                        id = it.session.subjectInstructorCrossRefId!!,
                        subject = it.subjectWithInstructor!!.subject,
                        instructor = it.subjectWithInstructor.instructor,
                        hue = it.subjectWithInstructor.hue
                    )

                Schedule.subject(
                    subjectInstructor = subjectInstructor,
                    periodSpan = periodSpan
                )
            } else {
                Schedule.empty(
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

    return if (first.subjectInstructor != null) {
        Schedule.subject(first.subjectInstructor, periodSpan)
    } else {
        Schedule.empty(first.label, periodSpan)
    }
}

@ConsistentCopyVisibility
data class Schedule private constructor(
    val subjectInstructor: SubjectInstructor?,
    val label: String?,
    val periodSpan: Int
) {
    companion object {
        fun subject(subjectInstructor: SubjectInstructor, periodSpan: Int): Schedule {
            return Schedule(
                subjectInstructor = subjectInstructor,
                periodSpan = periodSpan,
                label = null
            )
        }

        fun empty(label: String?, periodSpan: Int): Schedule {
            return Schedule(
                subjectInstructor = null,
                periodSpan = periodSpan,
                label = label
            )
        }
    }
}

data class SubjectInstructor(
    val id: Int,
    val hue: Hue,
    val subject: Subject?,
    val instructor: Instructor?
) {
    override fun equals(other: Any?): Boolean {
        if (other !is SubjectInstructor) {
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
        result = 31 * result + (subject?.hashCode() ?: 0)
        result = 31 * result + (instructor?.hashCode() ?: 0)
        return result
    }


}