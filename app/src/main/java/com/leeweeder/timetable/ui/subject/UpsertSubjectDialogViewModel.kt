package com.leeweeder.timetable.ui.subject

import android.database.sqlite.SQLiteConstraintException
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.leeweeder.timetable.domain.model.Subject
import com.leeweeder.timetable.domain.repository.SubjectRepository
import com.leeweeder.timetable.util.Destination
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UpsertSubjectDialogViewModel(
    savedStateHandle: SavedStateHandle,
    private val subjectRepository: SubjectRepository
) : ViewModel() {
    private val _uiState = mutableStateOf(UpsertSubjectDialogUiState())
    val uiState: State<UpsertSubjectDialogUiState> = _uiState

    private val _eventFlow = MutableStateFlow<UpsertSubjectDialogUiEvent?>(null)
    val eventFlow: StateFlow<UpsertSubjectDialogUiEvent?> = _eventFlow.asStateFlow()

    init {
        savedStateHandle.toRoute<Destination.Dialog.UpsertSubjectDialog>().let {
            _uiState.value = uiState.value.copy(
                description = it.description,
                code = it.code,
                id = if (it.id == 0) null else it.id
            )
        }
    }

    fun onEvent(event: UpsertSubjectDialogEvent) {
        when (event) {
            is UpsertSubjectDialogEvent.EditCode -> {
                _uiState.value = uiState.value.copy(
                    code = event.value.uppercase()
                )
            }

            is UpsertSubjectDialogEvent.EditDescription -> {
                _uiState.value = uiState.value.copy(
                    description = event.value
                )
            }

            UpsertSubjectDialogEvent.Save -> {
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
                        _eventFlow.emit(UpsertSubjectDialogUiEvent.DoneSaving)
                    } catch (_: SQLiteConstraintException) {
                        _uiState.value = uiState.value.copy(
                            conflictError = "A subject with the same code and description already exists."
                        )
                    }
                }
            }

            UpsertSubjectDialogEvent.StartCodeErrorChecking -> {
                _uiState.value = uiState.value.copy(
                    shouldStartCheckingForCodeError = true
                )
            }

            UpsertSubjectDialogEvent.StartDescriptionErrorChecking -> {
                _uiState.value = uiState.value.copy(
                    shouldStartCheckingForDescriptionError = true
                )
            }

            is UpsertSubjectDialogEvent.ClearError -> {
                _uiState.value = uiState.value.copy(
                    conflictError = if (event.conflict) null else uiState.value.conflictError,
                    forceCodeError = if (event.code) false else uiState.value.forceCodeError,
                    forceDescriptionError = if (event.description) false else uiState.value.forceDescriptionError
                )
            }

            UpsertSubjectDialogEvent.ForceCodeError -> {
                _uiState.value = uiState.value.copy(
                    forceCodeError = true
                )
            }

            UpsertSubjectDialogEvent.ForceDescriptionError -> {
                _uiState.value = uiState.value.copy(
                    forceDescriptionError = true
                )
            }
        }
    }
}

sealed interface UpsertSubjectDialogUiEvent {
    data object DoneSaving : UpsertSubjectDialogUiEvent
}

sealed interface UpsertSubjectDialogEvent {
    data class EditDescription(val value: String) : UpsertSubjectDialogEvent
    data object StartDescriptionErrorChecking : UpsertSubjectDialogEvent
    data class EditCode(val value: String) : UpsertSubjectDialogEvent
    data object StartCodeErrorChecking : UpsertSubjectDialogEvent
    data object Save : UpsertSubjectDialogEvent

    /** If true, the specified parameter will be cleared */
    data class ClearError(val code: Boolean, val description: Boolean, val conflict: Boolean) :
        UpsertSubjectDialogEvent

    data object ForceCodeError : UpsertSubjectDialogEvent
    data object ForceDescriptionError : UpsertSubjectDialogEvent
}

data class UpsertSubjectDialogUiState(
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