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

package com.leeweeder.unitimetable.ui.schedule

import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.leeweeder.unitimetable.domain.model.Instructor
import com.leeweeder.unitimetable.domain.model.Session
import com.leeweeder.unitimetable.domain.model.Subject
import com.leeweeder.unitimetable.domain.model.SubjectInstructorCrossRef
import com.leeweeder.unitimetable.domain.repository.InstructorRepository
import com.leeweeder.unitimetable.domain.repository.SubjectInstructorRepository
import com.leeweeder.unitimetable.domain.repository.SubjectRepository
import com.leeweeder.unitimetable.ui.components.searchable_bottom_sheet.SearchableBottomSheetStateFactory
import com.leeweeder.unitimetable.ui.schedule.ScheduleEntryDialogUiEvent.*
import com.leeweeder.unitimetable.util.Destination
import com.leeweeder.unitimetable.util.Hue
import com.leeweeder.unitimetable.util.randomHue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ScheduleEntryDialogViewModel(
    private val subjectInstructorRepository: SubjectInstructorRepository,
    private val subjectRepository: SubjectRepository,
    private val instructorRepository: InstructorRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _uiState = mutableStateOf(ScheduleEntryDialogUiState())
    val uiState: State<ScheduleEntryDialogUiState> = _uiState

    private val _eventFlow = MutableStateFlow<ScheduleEntryDialogUiEvent?>(null)
    val eventFlow: StateFlow<ScheduleEntryDialogUiEvent?> = _eventFlow.asStateFlow()

    private val _selectedSubjectId = MutableStateFlow(0)
    private val selectedSubjectId: StateFlow<Int> = _selectedSubjectId.asStateFlow()

    private val _selectedInstructorId = MutableStateFlow(0)
    private val selectedInstructorId: StateFlow<Int> = _selectedInstructorId.asStateFlow()


    @OptIn(ExperimentalCoroutinesApi::class)
    val dataState = combine(
        selectedSubjectId,
        selectedInstructorId
    ) { subjectId, instructorId -> Pair(subjectId, instructorId) }
        .flatMapLatest { (subjectId, instructorId) ->
            combine(
                subjectRepository.observeSubject(subjectId),
                instructorRepository.observeInstructor(instructorId)
            ) { subject, instructor ->
                ScheduleEntryDialogDataState.Success(subject, instructor)
            }
        }
        .catch { ScheduleEntryDialogDataState.Error(it) }
        .stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000),
            ScheduleEntryDialogDataState.Loading
        )

    private val searchableBottomSheetStateFactory =
        SearchableBottomSheetStateFactory(viewModelScope)

    val subjectBottomSheetState =
        searchableBottomSheetStateFactory.create(
            subjectRepository.observeSubjects()
        ) { subject, searchQuery ->
            subject.code.lowercase()
                .contains(searchQuery.lowercase()) || subject.description.lowercase()
                .contains(searchQuery.lowercase())

        }

    val instructorBottomSheetState =
        searchableBottomSheetStateFactory.create(instructorRepository.observeInstructors()) { instructor, searchQuery ->
            instructor.name.lowercase().contains(searchQuery.lowercase())
        }

    init {
        val subjectInstructorId =
            savedStateHandle.toRoute<Destination.Dialog.ScheduleEntryDialog>().subjectInstructorId

        viewModelScope.launch {
            if (subjectInstructorId != null) {
                subjectInstructorRepository.getSubjectInstructorById(subjectInstructorId)
                    ?.let { subjectInstructorWithId ->
                        _uiState.value = uiState.value.copy(
                            selectedHue = subjectInstructorWithId.hue,
                            id = subjectInstructorWithId.id
                        )

                        viewModelScope.launch {
                            _selectedSubjectId.emit(subjectInstructorWithId.subject.id)
                            _selectedInstructorId.emit(subjectInstructorWithId.instructor?.id ?: 0)
                        }
                    }
            } else {
                _uiState.value = uiState.value.copy(
                    selectedHue = randomHue(),
                    id = null
                )
            }
        }
    }

    fun onEvent(event: ScheduleEntryDialogEvent) {
        when (event) {
            is ScheduleEntryDialogEvent.SetSelectedSubject -> {
                viewModelScope.launch {
                    _selectedSubjectId.emit(event.subjectId)
                    Log.d("ScheduleDialogViewModel", "emitted, event: ${event.subjectId}")
                }
            }

            is ScheduleEntryDialogEvent.SetSelectedInstructor -> {
                viewModelScope.launch {
                    _selectedInstructorId.emit(event.instructorId)
                }
            }

            is ScheduleEntryDialogEvent.SetSelectedHue -> {
                _uiState.value = uiState.value.copy(
                    selectedHue = event.value
                )
            }

            ScheduleEntryDialogEvent.Save -> {
                viewModelScope.launch {
                    try {
                        // For preventing error if the user deleted a subject or instructor within this schedule entry,
                        // try fetching first if the that schedule entry is already in the database.
                        val subjectInstructorId =
                            if (uiState.value.id == null || subjectInstructorRepository.getSubjectInstructorById(
                                    uiState.value.id!!
                                ) == null
                            ) {
                                subjectInstructorRepository.insertSubjectInstructor(
                                    SubjectInstructorCrossRef(
                                        hue = uiState.value.selectedHue,
                                        subjectId = selectedSubjectId.value,
                                        instructorId = selectedInstructorId.value
                                    )
                                )
                            } else {
                                subjectInstructorRepository.updateSubjectInstructor(
                                    SubjectInstructorCrossRef(
                                        id = uiState.value.id!!,
                                        hue = uiState.value.selectedHue,
                                        subjectId = selectedSubjectId.value,
                                        instructorId = selectedInstructorId.value
                                    )
                                )
                                uiState.value.id!!
                            }

                        _eventFlow.emit(DoneSaving(subjectInstructorId))
                    } catch (_: SQLiteConstraintException) {
                        _eventFlow.emit(ShowSnackbar("Failed to save schedule entry. This subject and instructor combination already exists."))
                    }
                }
            }

            ScheduleEntryDialogEvent.ClearUiEventEntry -> {
                viewModelScope.launch {
                    _eventFlow.emit(null)
                }
            }

            ScheduleEntryDialogEvent.DeleteScheduleEntry -> {
                viewModelScope.launch {
                    try {
                        uiState.value.id?.let {
                            val subjectInstructorCrossRef =
                                subjectInstructorRepository.getSubjectInstructorCrossRefById(it)

                            // Delete the SubjectInstructorCrossRef
                            val affectedSessions =
                                subjectInstructorRepository.deleteSubjectInstructorCrossRefById(it)

                            _eventFlow.emit(
                                SuccessfulDeletion(
                                    subjectInstructorCrossRef,
                                    affectedSessions
                                )
                            )
                        }

                    } catch (e: Exception) {
                        _eventFlow.emit(ShowSnackbar("Error deleting schedule entry: ${e.localizedMessage}"))
                    }
                }
            }
        }
    }
}

sealed interface ScheduleEntryDialogUiEvent {
    data class ShowSnackbar(val message: String) : ScheduleEntryDialogUiEvent
    data class DoneSaving(val subjectInstructorId: Int) : ScheduleEntryDialogUiEvent
    data class SuccessfulDeletion(
        val subjectInstructorCrossRef: SubjectInstructorCrossRef,
        val affectedSession: List<Session>
    ) : ScheduleEntryDialogUiEvent
}

sealed interface ScheduleEntryDialogEvent {
    data class SetSelectedSubject(val subjectId: Int) : ScheduleEntryDialogEvent
    data class SetSelectedInstructor(val instructorId: Int) : ScheduleEntryDialogEvent
    data class SetSelectedHue(val value: Hue) : ScheduleEntryDialogEvent
    data object Save : ScheduleEntryDialogEvent
    data object ClearUiEventEntry : ScheduleEntryDialogEvent
    data object DeleteScheduleEntry : ScheduleEntryDialogEvent
}

sealed interface ScheduleEntryDialogDataState {
    data class Success(val subject: Subject?, val instructor: Instructor?) :
        ScheduleEntryDialogDataState

    data object Loading : ScheduleEntryDialogDataState
    data class Error(val throwable: Throwable) : ScheduleEntryDialogDataState
}

data class ScheduleEntryDialogUiState(
    val selectedHue: Hue = Hue.UNSPECIFIED,
    val id: Int? = null
)