package com.leeweeder.timetable

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leeweeder.timetable.MainActivityUiEvent.*
import com.leeweeder.timetable.domain.model.Instructor
import com.leeweeder.timetable.domain.model.Session
import com.leeweeder.timetable.domain.model.Subject
import com.leeweeder.timetable.domain.model.SubjectInstructorCrossRef
import com.leeweeder.timetable.domain.repository.DataStoreRepository
import com.leeweeder.timetable.domain.repository.InstructorRepository
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
    private val subjectRepository: SubjectRepository,
    private val instructorRepository: InstructorRepository
) : ViewModel() {

    val uiState = dataStoreRepository.timeTablePrefFlow.map {
        if (it.mainTimeTableId == NonExistingMainTimeTableId) {
            MainActivityUiState(
                isLoading = false,
                startDestination = Destination.Dialog.TimetableNameDialog(
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
                                _eventFlow.emit(ShowSnackbar.Success)
                            } catch (_: Exception) {
                                _eventFlow.emit(ShowSnackbar.Fail)
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
                                _eventFlow.emit(ShowSnackbar.Success)
                            } catch (_: Exception) {
                                _eventFlow.emit(ShowSnackbar.Fail)
                            }
                        }
                    }

                    is UndoEvent.UndoInstructorDeletion -> {
                        viewModelScope.launch {
                            try {
                                instructorRepository.insertInstructor(event.instructor)

                                subjectInstructorRepository.updateSubjectInstructorCrossRefs(
                                    subjectInstructorRepository.getNullInstructorCrossRefIds(event.affectedSubjectInstructorCrossRefIds)
                                        .map { it.copy(instructorId = event.instructor.id) }
                                )

                                _eventFlow.emit(ShowSnackbar.Success)
                            } catch (_: Exception) {
                                _eventFlow.emit(ShowSnackbar.Fail)
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

    data class UndoInstructorDeletion(
        val instructor: Instructor,
        val affectedSubjectInstructorCrossRefIds: List<Int>,
    ) : UndoEvent
}

sealed interface MainActivityUiEvent {
    sealed class ShowSnackbar(val message: String) : MainActivityUiEvent {
        data object Success : ShowSnackbar("Undo successful")
        data object Fail : ShowSnackbar("Undo operation failed")
    }
}