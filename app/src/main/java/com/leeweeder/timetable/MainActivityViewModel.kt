package com.leeweeder.timetable

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leeweeder.timetable.MainActivityUiEvent.*
import com.leeweeder.timetable.domain.model.Session
import com.leeweeder.timetable.domain.model.Subject
import com.leeweeder.timetable.domain.model.SubjectInstructorCrossRef
import com.leeweeder.timetable.domain.repository.DataStoreRepository
import com.leeweeder.timetable.domain.repository.SessionRepository
import com.leeweeder.timetable.domain.repository.SubjectInstructorRepository
import com.leeweeder.timetable.domain.repository.SubjectRepository
import com.leeweeder.timetable.util.Destination
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

const val NonExistingMainTimeTableId = -1

class MainActivityViewModel(
    dataStoreRepository: DataStoreRepository,
    private val subjectInstructorRepository: SubjectInstructorRepository,
    private val sessionRepository: SessionRepository,
    private val subjectRepository: SubjectRepository
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

    private val _eventFlow = MutableStateFlow<MainActivityUiEvent?>(null)
    val eventFlow: StateFlow<MainActivityUiEvent?> = _eventFlow.asStateFlow()

    fun onEvent(event: MainActivityEvent) {
        when (event) {
            is MainActivityEvent.Undo -> {
                val event = event.event
                when (event) {
                    is UndoEvent.UndoScheduleEntryDeletion -> {
                        viewModelScope.launch {
                            try {
                                subjectInstructorRepository.insertSubjectInstructor(event.subjectInstructorCrossRef)
                                sessionRepository.updateSessions(event.affectedSessions)
                                _eventFlow.emit(ShowSnackbar("Undo successful"))
                            } catch (_: Exception) {
                                _eventFlow.emit(ShowSnackbar("Undo operation failed"))
                            }
                        }
                    }

                    is UndoEvent.UndoSubjectDeletion -> {
                        viewModelScope.launch {
                            try {
                                // Insert first the subject
                                subjectRepository.insertSubject(event.subject)
                                // Insert the cross ref
                                subjectInstructorRepository.insertSubjectInstructorCrossRefs(event.affectedSubjectInstructorCrossRefs)
                                // Update the sessions
                                sessionRepository.updateSessions(event.affectedSessions)
                                _eventFlow.emit(ShowSnackbar("Undo successful"))
                            } catch (_: Exception) {
                                _eventFlow.emit(ShowSnackbar("Undo operation failed"))
                            }
                        }
                    }
                }
            }

            MainActivityEvent.ClearEventFlow -> {
                viewModelScope.launch {
                    _eventFlow.emit(null)
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
    data class Undo(val event: UndoEvent) : MainActivityEvent
    data object ClearEventFlow : MainActivityEvent
}

sealed interface UndoEvent {
    data class UndoScheduleEntryDeletion(
        val subjectInstructorCrossRef: SubjectInstructorCrossRef,
        val affectedSessions: List<Session>
    ) : UndoEvent

    data class UndoSubjectDeletion(
        val subject: Subject,
        val affectedSessions: List<Session>,
        val affectedSubjectInstructorCrossRefs: List<SubjectInstructorCrossRef>
    ) : UndoEvent
}

sealed interface MainActivityUiEvent {
    data class ShowSnackbar(val message: String) : MainActivityUiEvent
}