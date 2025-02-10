package com.leeweeder.timetable.ui.instructor

import android.database.sqlite.SQLiteConstraintException
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.leeweeder.timetable.domain.model.Instructor
import com.leeweeder.timetable.domain.repository.InstructorRepository
import com.leeweeder.timetable.util.Destination
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UpsertInstructorDialogViewModel(
    private val instructorRepository: InstructorRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _uiState = mutableStateOf(UpsertInstructorDialogUiState())
    val uiState: State<UpsertInstructorDialogUiState> = _uiState

    private val _eventFlow = MutableStateFlow<UpsertInstructorDialogUiEvent?>(null)
    val eventFlow: StateFlow<UpsertInstructorDialogUiEvent?> = _eventFlow.asStateFlow()

    init {
        savedStateHandle.toRoute<Destination.Dialog.UpsertInstructorDialog>().let {
            _uiState.value = uiState.value.copy(
                id = if (it.id == 0) null else it.id,
                name = it.name
            )
        }
    }

    fun onEvent(event: UpsertInstructorDialogEvent) {
        when (event) {
            is UpsertInstructorDialogEvent.EditName -> {
                _uiState.value = uiState.value.copy(
                    name = event.value
                )
            }

            UpsertInstructorDialogEvent.StartCheckingForError -> {
                _uiState.value = uiState.value.copy(
                    shouldStartCheckingForError = true
                )
            }

            UpsertInstructorDialogEvent.ClearError -> {
                _uiState.value = uiState.value.copy(
                    shouldStartCheckingForError = false
                )
            }

            UpsertInstructorDialogEvent.Save -> {
                viewModelScope.launch {
                    try {
                        if (uiState.value.id == null) {
                            instructorRepository.insertInstructor(
                                Instructor(
                                    name = uiState.value.name.trim()
                                )
                            )
                        } else {
                            instructorRepository.updateInstructor(
                                Instructor(
                                    id = uiState.value.id!!,
                                    name = uiState.value.name.trim()
                                )
                            )
                        }
                        _eventFlow.emit(UpsertInstructorDialogUiEvent.DoneSavingInstructor)
                    } catch (_: SQLiteConstraintException) {
                        _uiState.value = uiState.value.copy(
                            conflictError = "Name already exists"
                        )
                    }
                }
            }
        }
    }
}

sealed interface UpsertInstructorDialogUiEvent {
    data object DoneSavingInstructor : UpsertInstructorDialogUiEvent
}

data class UpsertInstructorDialogUiState(
    val id: Int? = null,
    val name: String = "",
    val conflictError: String? = null,
    private val shouldStartCheckingForError: Boolean = false
) {
    val isError: Boolean
        get() = name.isBlank() && shouldStartCheckingForError || conflictError != null
}

sealed interface UpsertInstructorDialogEvent {
    data class EditName(val value: String) : UpsertInstructorDialogEvent
    data object StartCheckingForError : UpsertInstructorDialogEvent
    data object ClearError : UpsertInstructorDialogEvent
    data object Save : UpsertInstructorDialogEvent
}