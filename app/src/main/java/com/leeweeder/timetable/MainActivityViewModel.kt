package com.leeweeder.timetable

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leeweeder.timetable.domain.model.Session
import com.leeweeder.timetable.domain.model.SubjectInstructorCrossRef
import com.leeweeder.timetable.domain.model.toScheduledSession
import com.leeweeder.timetable.domain.repository.DataStoreRepository
import com.leeweeder.timetable.domain.repository.SessionRepository
import com.leeweeder.timetable.domain.repository.SubjectInstructorRepository
import com.leeweeder.timetable.util.Destination
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

const val NonExistingMainTimeTableId = -1

class MainActivityViewModel(
    dataStoreRepository: DataStoreRepository,
    private val subjectInstructorRepository: SubjectInstructorRepository,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    val uiState = dataStoreRepository.timeTablePrefFlow.map {
        if (it.mainTimeTableId == NonExistingMainTimeTableId) {
            MainActivityUiState(
                isLoading = false,
                startDestination = Destination.Dialog.GetTimeTableNameDialog(
                    isInitialization = true,
                    selectedTimeTableId = -1
                )
            )
        } else {
            MainActivityUiState(isLoading = false, mainTimeTableId = it.mainTimeTableId)
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000L),
        MainActivityUiState(isLoading = true)
    )

    fun onEvent(event: MainActivityEvent) {
        when (event) {
            is MainActivityEvent.UndoScheduleEntryDeletion -> {
                viewModelScope.launch {
                    val subjectInstructorCrossRef = event.subjectInstructorCrossRef
                    subjectInstructorRepository.insertSubjectInstructor(subjectInstructorCrossRef)
                    sessionRepository.updateSessions(event.affectedSessions.map { it.toScheduledSession(subjectInstructorCrossRef.id) })
                }
            }
        }
    }
}

data class MainActivityUiState(
    val isLoading: Boolean,
    val mainTimeTableId: Int = NonExistingMainTimeTableId,
    val startDestination: Destination = Destination.Screen.HomeScreen(selectedTimeTableId = mainTimeTableId)
)

sealed interface MainActivityEvent {
    data class UndoScheduleEntryDeletion(
        val subjectInstructorCrossRef: SubjectInstructorCrossRef,
        val affectedSessions: List<Session>
    ) : MainActivityEvent
}