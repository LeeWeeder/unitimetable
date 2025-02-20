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

class InstructorDialogViewModel(
    private val instructorRepository: InstructorRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _uiState = mutableStateOf(InstructorDialogUiState())
    val uiState: State<InstructorDialogUiState> = _uiState

    private val _eventFlow = MutableStateFlow<InstructorDialogUiEvent?>(null)
    val eventFlow: StateFlow<InstructorDialogUiEvent?> = _eventFlow.asStateFlow()

    init {
        savedStateHandle.toRoute<Destination.Dialog.InstructorDialog>().let {
            _uiState.value = uiState.value.copy(
                id = if (it.id == 0) null else it.id,
                name = it.name
            )
        }
    }

    fun onEvent(event: InstructorDialogEvent) {
        when (event) {
            is InstructorDialogEvent.EditName -> {
                _uiState.value = uiState.value.copy(
                    name = event.value
                )
            }

            InstructorDialogEvent.StartCheckingForError -> {
                _uiState.value = uiState.value.copy(
                    shouldStartCheckingForError = true
                )
            }

            InstructorDialogEvent.ClearError -> {
                _uiState.value = uiState.value.copy(
                    shouldStartCheckingForError = false
                )
            }

            InstructorDialogEvent.Save -> {
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
                        _eventFlow.emit(InstructorDialogUiEvent.DoneSavingInstructor)
                    } catch (_: SQLiteConstraintException) {
                        _uiState.value = uiState.value.copy(
                            conflictError = "Name already exists"
                        )
                    }
                }
            }

            InstructorDialogEvent.DeleteInstructor -> {
                viewModelScope.launch {
                    uiState.value.id?.let {
                        try {
                            // TODO: Do the same for schedule entry and subject, fetch first the to be deleted item
                            val deletedSubject = instructorRepository.getInstructorById(it)
                            deletedSubject?.let {
                                _eventFlow.emit(
                                    InstructorDialogUiEvent.InstructorDeleted(
                                        deletedSubject,
                                        instructorRepository.deleteInstructorById(it.id)
                                    )
                                )
                            }
                        } catch (e: Exception) {
                            _eventFlow.emit(
                                InstructorDialogUiEvent.Error(
                                    e.message ?: "Unknown error"
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

sealed interface InstructorDialogUiEvent {
    data object DoneSavingInstructor : InstructorDialogUiEvent
    data class InstructorDeleted(val instructor: Instructor, val crossRefIds: List<Int>) :
        InstructorDialogUiEvent

    data class Error(val message: String) : InstructorDialogUiEvent
}

data class InstructorDialogUiState(
    val id: Int? = null,
    val name: String = "",
    val conflictError: String? = null,
    private val shouldStartCheckingForError: Boolean = false
) {
    val isError: Boolean
        get() = name.isBlank() && shouldStartCheckingForError || conflictError != null
}

sealed interface InstructorDialogEvent {
    data class EditName(val value: String) : InstructorDialogEvent
    data object StartCheckingForError : InstructorDialogEvent
    data object ClearError : InstructorDialogEvent
    data object Save : InstructorDialogEvent
    data object DeleteInstructor : InstructorDialogEvent
}