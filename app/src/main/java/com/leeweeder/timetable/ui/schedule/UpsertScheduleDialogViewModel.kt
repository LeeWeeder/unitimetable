package com.leeweeder.timetable.ui.schedule

import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.leeweeder.timetable.domain.model.Instructor
import com.leeweeder.timetable.domain.model.Subject
import com.leeweeder.timetable.domain.model.SubjectInstructorCrossRef
import com.leeweeder.timetable.domain.repository.InstructorRepository
import com.leeweeder.timetable.domain.repository.SubjectInstructorRepository
import com.leeweeder.timetable.domain.repository.SubjectRepository
import com.leeweeder.timetable.ui.components.selection_and_addition_bottom_sheet.SearchableBottomSheetStateFactory
import com.leeweeder.timetable.ui.schedule.UpsertScheduleDialogUiEvent.*
import com.leeweeder.timetable.util.Destination
import com.leeweeder.timetable.util.Hue
import com.leeweeder.timetable.util.randomHue
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

class UpsertScheduleDialogViewModel(
    private val subjectInstructorRepository: SubjectInstructorRepository,
    private val subjectRepository: SubjectRepository,
    private val instructorRepository: InstructorRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _uiState = mutableStateOf(UpsertScheduleDialogUiState())
    val uiState: State<UpsertScheduleDialogUiState> = _uiState

    private val _eventFlow = MutableStateFlow<UpsertScheduleDialogUiEvent?>(null)
    val eventFlow: StateFlow<UpsertScheduleDialogUiEvent?> = _eventFlow.asStateFlow()

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
                UpsertScheduleDialogDataState.Success(subject, instructor)
            }
        }
        .catch { UpsertScheduleDialogDataState.Error(it) }
        .stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000),
            UpsertScheduleDialogDataState.Loading
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
            savedStateHandle.toRoute<Destination.Dialog.UpsertScheduleDialog>().subjectInstructorId

        viewModelScope.launch {
            if (subjectInstructorId != null) {
                val subjectInstructorWithId =
                    subjectInstructorRepository.getSubjectInstructorWithId(subjectInstructorId)

                _uiState.value = uiState.value.copy(
                    selectedHue = subjectInstructorWithId.hue,
                    id = subjectInstructorWithId.id
                )

                viewModelScope.launch {
                    _selectedSubjectId.emit(subjectInstructorWithId.subject.id)
                    _selectedInstructorId.emit(subjectInstructorWithId.instructor.id)
                }
            } else {
                _uiState.value = uiState.value.copy(
                    selectedHue = randomHue(),
                    id = null
                )
            }
        }
    }

    fun onEvent(event: UpsertScheduleDialogEvent) {
        when (event) {
            is UpsertScheduleDialogEvent.SetSelectedSubject -> {
                viewModelScope.launch {
                    _selectedSubjectId.emit(event.subjectId)
                    Log.d("UpsertScheduleDialogViewModel", "emitted, event: ${event.subjectId}")
                }
            }

            is UpsertScheduleDialogEvent.SetSelectedInstructor -> {
                viewModelScope.launch {
                    _selectedInstructorId.emit(event.instructorId)
                }
            }

            is UpsertScheduleDialogEvent.SetSelectedHue -> {
                _uiState.value = uiState.value.copy(
                    selectedHue = event.value
                )
            }

            UpsertScheduleDialogEvent.Save -> {
                viewModelScope.launch {
                    try {
                        if (uiState.value.id == null) {
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
                        }

                        _eventFlow.emit(DoneSaving)
                    } catch (_: SQLiteConstraintException) {
                        _eventFlow.emit(ShowSnackbar("Failed to save schedule entry. This subject and instructor combination already exists."))
                    }
                }
            }

            UpsertScheduleDialogEvent.ClearUiEvent -> {
                viewModelScope.launch {
                    _eventFlow.emit(null)
                }
            }
        }
    }
}

sealed interface UpsertScheduleDialogUiEvent {
    data class ShowSnackbar(val message: String) : UpsertScheduleDialogUiEvent
    data object DoneSaving : UpsertScheduleDialogUiEvent
}

sealed interface UpsertScheduleDialogEvent {
    data class SetSelectedSubject(val subjectId: Int) : UpsertScheduleDialogEvent
    data class SetSelectedInstructor(val instructorId: Int) : UpsertScheduleDialogEvent
    data class SetSelectedHue(val value: Hue) : UpsertScheduleDialogEvent
    data object Save : UpsertScheduleDialogEvent
    data object ClearUiEvent : UpsertScheduleDialogEvent
}

sealed interface UpsertScheduleDialogDataState {
    data class Success(val subject: Subject?, val instructor: Instructor?) :
        UpsertScheduleDialogDataState

    data object Loading : UpsertScheduleDialogDataState
    data class Error(val throwable: Throwable) : UpsertScheduleDialogDataState
}

data class UpsertScheduleDialogUiState(
    val selectedHue: Hue = Hue.UNSPECIFIED,
    val id: Int? = null
)