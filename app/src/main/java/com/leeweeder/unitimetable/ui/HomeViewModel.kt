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

package com.leeweeder.unitimetable.ui

import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.leeweeder.unitimetable.domain.model.Instructor
import com.leeweeder.unitimetable.domain.model.Session
import com.leeweeder.unitimetable.domain.model.Subject
import com.leeweeder.unitimetable.domain.model.Timetable
import com.leeweeder.unitimetable.domain.relation.SessionWithDetails
import com.leeweeder.unitimetable.domain.relation.TimetableWithSession
import com.leeweeder.unitimetable.domain.repository.DataStoreRepository
import com.leeweeder.unitimetable.domain.repository.SessionRepository
import com.leeweeder.unitimetable.domain.repository.SubjectInstructorRepository
import com.leeweeder.unitimetable.domain.repository.TimetableRepository
import com.leeweeder.unitimetable.ui.HomeEvent.*
import com.leeweeder.unitimetable.ui.HomeUiEvent.*
import com.leeweeder.unitimetable.ui.components.searchable_bottom_sheet.SearchableBottomSheetStateFactory
import com.leeweeder.unitimetable.util.Destination
import com.leeweeder.unitimetable.util.Hue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek

private const val TAG = "HomeViewModel"

class HomeViewModel(
    private val timeTableRepository: TimetableRepository,
    private val sessionRepository: SessionRepository,
    subjectInstructorRepository: SubjectInstructorRepository,
    private val dataStoreRepository: DataStoreRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    private val _eventFlow = MutableStateFlow<HomeUiEvent?>(null)
    val eventFlow = _eventFlow.asStateFlow()

    private var getSelectedTimetableIdJob: Job? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    val homeDataState: StateFlow<HomeDataState> = timeTableRepository.observeTimetablesWithDetails()
        .map { timetablesWithDetails ->
            HomeDataState.Success(timetablesWithDetails).also {
                _eventFlow.emit(DoneLoading(shouldInitialize = timetablesWithDetails.isEmpty()))
            }
        }
        .catch {
            Log.e(TAG, "Error loading data", it)
            HomeDataState.Error(it)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), HomeDataState.Loading)

    private val searchableBottomSheetStateFactory =
        SearchableBottomSheetStateFactory(viewModelScope)

    val scheduleEntryBottomSheetState =
        searchableBottomSheetStateFactory
            .create(
                subjectInstructorRepository.observeSubjectInstructors()
            ) { subjectInstructor, searchQuery ->
                val searchQuery = searchQuery.lowercase()
                (subjectInstructor.instructor != null && subjectInstructor.instructor.name.lowercase()
                    .contains(searchQuery)) ||
                        subjectInstructor.subject.code.lowercase().contains(searchQuery) ||
                        subjectInstructor.subject.description.lowercase()
                            .contains(searchQuery)
            }

    init {
        // Identify what is the selectedTimetableId
        getSelectedTimetableId()

        // Check if there is a subjectInstructorId passed on navigation to this screen (not null)
        // If so, set the mode to edit with the passed id
        savedStateHandle.toRoute<Destination.Screen.HomeScreen>().subjectInstructorIdToBeScheduled?.let {
            onEvent(SetToEditMode(it))
        }
    }

    fun onEvent(event: HomeEvent) {
        when (event) {
            is SelectTimeTable -> {
                viewModelScope.launch {
                    dataStoreRepository.setSelectedTimetable(event.newSelectedTimetableId)
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

            is DeleteTimeTable -> {
                if (homeDataState.value is HomeDataState.Success) {
                    val timeTableWithDetails =
                        (homeDataState.value as HomeDataState.Success).timeTableWithDetails.find { it.timetable.id == event.timeTableId }!!
                    viewModelScope.launch {
                        timeTableRepository.deleteTimeTableById(event.timeTableId)
                        _eventFlow.emit(SuccessTimetableDeletion(timeTableWithDetails))
                    }
                }
            }

            ClearUiEvent -> {
                viewModelScope.launch {
                    _eventFlow.emit(null)
                }
            }
        }
    }

    private fun getSelectedTimetableId() {
        getSelectedTimetableIdJob?.cancel()
        getSelectedTimetableIdJob = dataStoreRepository.selectedTimetableIdFlow
            .onEach { selectedTimetableId ->
                val timetables = timeTableRepository.observeTimetables().first()
                if (timetables.find { it.id == selectedTimetableId } != null) {
                    _uiState.update { state ->
                        state.copy(selectedTimetableId = selectedTimetableId)
                    }
                } else {
                    if (timetables.isNotEmpty()) {
                        _uiState.update { state ->
                            state.copy(selectedTimetableId = timetables.first().id)
                        }
                    }
                }
            }
            .launchIn(viewModelScope)
    }
}

sealed interface HomeEvent {
    data class SelectTimeTable(val newSelectedTimetableId: Int) : HomeEvent

    data class SetSessionWithActiveSubjectInstructor(val sessionId: Session) :
        HomeEvent

    data object SetToDefaultMode : HomeEvent
    data class SetToEditMode(val id: Int) : HomeEvent
    data class DeleteTimeTable(val timeTableId: Int) : HomeEvent
    data object ClearUiEvent : HomeEvent
}

sealed interface HomeUiEvent {
    data class SuccessTimetableDeletion(val deletedTimetableWithDetails: TimetableWithSession) :
        HomeUiEvent

    data class DoneLoading(val shouldInitialize: Boolean = false /* The selected timetable is the default value */) :
        HomeUiEvent
}

sealed interface HomeDataState {
    data class Success(
        val timeTableWithDetails: List<TimetableWithSession> = emptyList()
    ) : HomeDataState {

        fun getSelectedTimetableSessionWithDetails(id: Int): List<SessionWithDetails> {
            return timeTableWithDetails.find { it.timetable.id == id }?.sessions
                ?: timeTableWithDetails.firstOrNull()?.sessions ?: emptyList()
        }

        fun getSelectedTimetableGroupedSchedules(id: Int): List<List<Schedule>> {
            return getSelectedTimetable(id)?.let {
                getSelectedTimetableSessionWithDetails(id).toGroupedSchedules(it.days)
            } ?: emptyList()
        }

        val timetables: List<Timetable>
            get() = timeTableWithDetails.map { it.timetable }

        /** Returns the timetable of the given the id, the first item if none match  and null if empty */
        fun getSelectedTimetable(id: Int): Timetable? {
            return timetables.find { it.id == id } ?: timetables.firstOrNull()
        }
    }

    data class Error(val throwable: Throwable) : HomeDataState
    data object Loading : HomeDataState
}

data class HomeUiState(
    val isOnEditMode: Boolean = false,
    val selectedTimetableId: Int = -1,
    val activeSubjectInstructorIdForScheduling: Int? = null
)

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
/**
 * Get the schedules for each day by checking the equality of the given days' index and the returned
 * list.
 *
 * @return The schedules for each day of the week, sorted by [days]
 * */
fun List<SessionWithDetails>.toGroupedSchedules(
    /** Make sure to sort the days based on the timetable settings */
    days: List<DayOfWeek>
): List<List<Schedule>> {
    val groupedSessions = this.groupBy { it.session.dayOfWeek }

    // For each items in the list, check if the current item and the next item is the same, else, merge them.
    return days.map { day ->
        val sessions = groupedSessions[day] ?: emptyList()

        // for each sessions, convert all of them to schedules
        val schedules = sessions.map { sessionWithDetails ->
            val periodSpan = 1

            if (sessionWithDetails.session.isSubject) {
                val subjectInstructor = sessionWithDetails.subjectWithInstructor?.let {
                    SubjectInstructor(
                        id = sessionWithDetails.session.subjectInstructorCrossRefId!!,
                        subject = sessionWithDetails.subjectWithInstructor.subject,
                        instructor = sessionWithDetails.subjectWithInstructor.instructor,
                        hue = sessionWithDetails.subjectWithInstructor.hue
                    )
                }
                    ?: throw IllegalArgumentException("Subject instructor is null for a subject session: $sessionWithDetails")

                Schedule.subject(
                    subjectInstructor = subjectInstructor,
                    periodSpan = periodSpan
                )
            } else {
                Schedule.empty(
                    label = sessionWithDetails.session.label,
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
    val subject: Subject,
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
        result = 31 * result + (subject.hashCode())
        result = 31 * result + (instructor?.hashCode() ?: 0)
        return result
    }
}