package com.leeweeder.timetable.ui.schedule

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.leeweeder.timetable.domain.model.Instructor
import com.leeweeder.timetable.domain.model.Subject
import com.leeweeder.timetable.domain.repository.SubjectInstructorRepository
import com.leeweeder.timetable.domain.repository.SubjectRepository
import com.leeweeder.timetable.util.Destination
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class UpsertScheduleDialogViewModel(
    private val subjectInstructorRepository: SubjectInstructorRepository,
    private val subjectRepository: SubjectRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _uiState = mutableStateOf(UpsertScheduleDialogUiState())
    val uiState: State<UpsertScheduleDialogUiState> = _uiState

    val subjectSearchFieldState = TextFieldState()

    private val subjectBottomSheetDataState = subjectRepository.observeSubjects()
        .map { SubjectBottomSheetDataState.Success(it) }
        .catch { SubjectBottomSheetDataState.Error(it) }
        .stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5_000L),
            SubjectBottomSheetDataState.Loading
        )

    var subjectOptions: List<Subject> by mutableStateOf(emptyList())
        private set

    @OptIn(FlowPreview::class)
    suspend fun runSubjectSearch() {
        combine(
            subjectBottomSheetDataState,
            snapshotFlow { subjectSearchFieldState.text }.debounce(200)
        ) { dataState, searchFieldState ->
            when (dataState) {
                is SubjectBottomSheetDataState.Success -> {
                    val searchQuery = searchFieldState.toString().lowercase().trim()
                    if (searchQuery.isBlank()) {
                        dataState.subjects
                    } else {
                        dataState.subjects.filter { subject ->
                            subject.code.lowercase()
                                .contains(searchQuery) || subject.description.lowercase()
                                .contains(searchQuery)
                        }
                    }
                }

                else -> emptyList()
            }
        }.collectLatest { filteredList ->
            subjectOptions = filteredList
        }
    }

    init {
        val subjectInstructorId =
            savedStateHandle.toRoute<Destination.Dialog.UpsertScheduleDialog>().subjectInstructorId

        viewModelScope.launch {
            if (subjectInstructorId != null) {
                val subjectInstructorWithId =
                    subjectInstructorRepository.getSubjectInstructorWithId(subjectInstructorId)

                _uiState.value = uiState.value.copy(
                    selectedSubject = subjectInstructorWithId.subject,
                    selectedInstructor = subjectInstructorWithId.instructor,
                    mode = UpsertScheduleDialogMode.Update
                )
            } else {
                _uiState.value = uiState.value.copy(
                    mode = UpsertScheduleDialogMode.Insert
                )
            }
        }
    }
}

data class UpsertScheduleDialogUiState(
    val selectedSubject: Subject? = null,
    val selectedInstructor: Instructor? = null,
    val mode: UpsertScheduleDialogMode = UpsertScheduleDialogMode.Insert
)

sealed interface SubjectBottomSheetDataState {
    data class Success(val subjects: List<Subject>) : SubjectBottomSheetDataState
    data class Error(val throwable: Throwable) : SubjectBottomSheetDataState
    data object Loading : SubjectBottomSheetDataState
}