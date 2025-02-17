package com.leeweeder.timetable.ui.subject

import android.database.sqlite.SQLiteConstraintException
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.leeweeder.timetable.domain.model.Session
import com.leeweeder.timetable.domain.model.Subject
import com.leeweeder.timetable.domain.model.SubjectInstructorCrossRef
import com.leeweeder.timetable.domain.repository.SubjectRepository
import com.leeweeder.timetable.util.Destination
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SubjectDialogViewModel(
    savedStateHandle: SavedStateHandle,
    private val subjectRepository: SubjectRepository
) : ViewModel() {
    private val _uiState = mutableStateOf(SubjectDialogUiState())
    val uiState: State<SubjectDialogUiState> = _uiState

    private val _eventFlow = MutableStateFlow<SubjectDialogUiEvent?>(null)
    val eventFlow: StateFlow<SubjectDialogUiEvent?> = _eventFlow.asStateFlow()

    init {
        savedStateHandle.toRoute<Destination.Dialog.SubjectDialog>().let {
            _uiState.value = uiState.value.copy(
                description = it.description,
                code = it.code,
                id = if (it.id == 0) null else it.id
            )
        }
    }

    fun onEvent(event: SubjectDialogEvent) {
        when (event) {
            is SubjectDialogEvent.EditCode -> {
                _uiState.value = uiState.value.copy(
                    code = event.value.uppercase()
                )
            }

            is SubjectDialogEvent.EditDescription -> {
                _uiState.value = uiState.value.copy(
                    description = event.value
                )
            }

            SubjectDialogEvent.Save -> {
                viewModelScope.launch {
                    try {
                        if (uiState.value.id == null) {
                            subjectRepository.insertSubject(
                                Subject(
                                    description = uiState.value.description.trim(),
                                    code = uiState.value.code.uppercase().trim()
                                )
                            )
                        } else {
                            subjectRepository.updateSubject(
                                Subject(
                                    id = uiState.value.id!!,
                                    description = uiState.value.description.trim(),
                                    code = uiState.value.code.uppercase().trim()
                                )
                            )
                        }
                        _eventFlow.emit(SubjectDialogUiEvent.DoneSaving)
                    } catch (_: SQLiteConstraintException) {
                        _uiState.value = uiState.value.copy(
                            conflictError = "A subject with the same code and description already exists."
                        )
                    }
                }
            }

            SubjectDialogEvent.StartCodeErrorChecking -> {
                _uiState.value = uiState.value.copy(
                    shouldStartCheckingForCodeError = true
                )
            }

            SubjectDialogEvent.StartDescriptionErrorChecking -> {
                _uiState.value = uiState.value.copy(
                    shouldStartCheckingForDescriptionError = true
                )
            }

            is SubjectDialogEvent.ClearError -> {
                _uiState.value = uiState.value.copy(
                    conflictError = if (event.conflict) null else uiState.value.conflictError,
                    forceCodeError = if (event.code) false else uiState.value.forceCodeError,
                    forceDescriptionError = if (event.description) false else uiState.value.forceDescriptionError
                )
            }

            SubjectDialogEvent.ForceCodeError -> {
                _uiState.value = uiState.value.copy(
                    forceCodeError = true
                )
            }

            SubjectDialogEvent.ForceDescriptionError -> {
                _uiState.value = uiState.value.copy(
                    forceDescriptionError = true
                )
            }

            SubjectDialogEvent.DeleteSubject -> {
                viewModelScope.launch {
                    val deletedSubject =
                        Subject(uiState.value.id!!, uiState.value.description, uiState.value.code)
                    val (affectedSessions, affectedSubjectInstructorCrossRefs) = subjectRepository.deleteSubjectById(
                        uiState.value.id!!
                    )

                    _eventFlow.emit(
                        SubjectDialogUiEvent.DeletionSuccessful(
                            deletedSubject,
                            affectedSessions,
                            affectedSubjectInstructorCrossRefs
                        )
                    )
                }
            }
        }
    }
}

sealed interface SubjectDialogUiEvent {
    data object DoneSaving : SubjectDialogUiEvent
    data class DeletionSuccessful(
        val subject: Subject,
        val sessions: List<Session>,
        val subjectInstructorCrossRefs: List<SubjectInstructorCrossRef>
    ) :
        SubjectDialogUiEvent
}

sealed interface SubjectDialogEvent {
    data class EditDescription(val value: String) : SubjectDialogEvent
    data object StartDescriptionErrorChecking : SubjectDialogEvent
    data class EditCode(val value: String) : SubjectDialogEvent
    data object StartCodeErrorChecking : SubjectDialogEvent
    data object Save : SubjectDialogEvent

    /** If true, the specified parameter will be cleared */
    data class ClearError(val code: Boolean, val description: Boolean, val conflict: Boolean) :
        SubjectDialogEvent

    data object ForceCodeError : SubjectDialogEvent
    data object ForceDescriptionError : SubjectDialogEvent

    data object DeleteSubject : SubjectDialogEvent
}

data class SubjectDialogUiState(
    val description: String = "",
    val code: String = "",
    val id: Int? = null,
    val conflictError: String? = null,
    private val shouldStartCheckingForCodeError: Boolean = false,
    private val shouldStartCheckingForDescriptionError: Boolean = false,
    internal val forceCodeError: Boolean = false,
    internal val forceDescriptionError: Boolean = false
) {
    val isCodeError: Boolean get() = (shouldStartCheckingForCodeError && code.isBlank()) || (conflictError != null) || forceCodeError
    val isDescriptionError: Boolean get() = (shouldStartCheckingForDescriptionError && description.isBlank()) || (conflictError != null) || forceDescriptionError
}