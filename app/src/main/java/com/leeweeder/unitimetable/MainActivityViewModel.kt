package com.leeweeder.unitimetable

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leeweeder.unitimetable.MainActivityUiEvent.*
import com.leeweeder.unitimetable.domain.model.Instructor
import com.leeweeder.unitimetable.domain.model.Session
import com.leeweeder.unitimetable.domain.model.Subject
import com.leeweeder.unitimetable.domain.model.SubjectInstructorCrossRef
import com.leeweeder.unitimetable.domain.relation.TimetableWithSession
import com.leeweeder.unitimetable.domain.repository.InstructorRepository
import com.leeweeder.unitimetable.domain.repository.SessionRepository
import com.leeweeder.unitimetable.domain.repository.SubjectInstructorRepository
import com.leeweeder.unitimetable.domain.repository.SubjectRepository
import com.leeweeder.unitimetable.domain.repository.TimetableRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainActivityViewModel(
    private val subjectInstructorRepository: SubjectInstructorRepository,
    private val sessionRepository: SessionRepository,
    private val subjectRepository: SubjectRepository,
    private val instructorRepository: InstructorRepository,
    private val timetableRepository: TimetableRepository
) : ViewModel() {

    private val _eventFlow = MutableStateFlow<MainActivityUiEvent?>(null)
    val eventFlow: StateFlow<MainActivityUiEvent?> = _eventFlow.asStateFlow()

    private val _isLoading = mutableStateOf(true)
    val isLoading: State<Boolean> = _isLoading

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

                    is UndoEvent.UndoTimeTableDeletion -> {
                        viewModelScope.launch {
                            try {
                                timetableRepository.insertTimetable(
                                    event.timetableWithDetails.timetable,
                                    event.timetableWithDetails.sessions.map { it.session }
                                )
                                _eventFlow.emit(ShowSnackbar.Success)
                            } catch (e: Exception) {
                                _eventFlow.emit(ShowSnackbar.Fail)
                                Log.e("MainActivityViewModel", "Undo timetable deletion failed", e)
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

            MainActivityEvent.DoneLoading -> {
                _isLoading.value = false
            }
        }
    }
}

sealed interface MainActivityEvent {
    data class Undo(val event: UndoEvent) : MainActivityEvent
    data object DoneLoading : MainActivityEvent
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

    data class UndoTimeTableDeletion(
        val timetableWithDetails: TimetableWithSession
    ) : UndoEvent
}

sealed interface MainActivityUiEvent {
    sealed class ShowSnackbar(val message: String) : MainActivityUiEvent {
        data object Success : ShowSnackbar("Undo successful")
        data object Fail : ShowSnackbar("Undo operation failed")
    }
}