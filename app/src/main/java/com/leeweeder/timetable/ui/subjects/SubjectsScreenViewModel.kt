package com.leeweeder.timetable.ui.subjects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leeweeder.timetable.domain.relation.SubjectWithDetails
import com.leeweeder.timetable.domain.repository.SubjectRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SubjectsScreenViewModel(
    private val subjectRepository: SubjectRepository
) : ViewModel() {

    val subjectsWithDetails = subjectRepository.observeSubjectWithDetails()
        .map {
            SubjectsScreenUiState.Success(it)
        }.catch {
            SubjectsScreenUiState.Error(it)
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000L),
            SubjectsScreenUiState.Loading
        )

    fun onEvent(event: SubjectsScreenEvent) {
        when (event) {
            is SubjectsScreenEvent.DeleteSubject -> {
                viewModelScope.launch {
                    try {
                        subjectRepository.deleteSubjectById(event.id)
                    } catch (e: Exception) {
                        // TODO: Implement proper error handling
                        println(e)
                    }
                }
            }
        }
    }

}

sealed interface SubjectsScreenUiState {
    data class Success(val subjectsWithDetails: List<SubjectWithDetails>) : SubjectsScreenUiState
    data class Error(val throwable: Throwable) : SubjectsScreenUiState
    data object Loading : SubjectsScreenUiState
}

sealed interface SubjectsScreenEvent {
    data class DeleteSubject(val id: Int) :
        SubjectsScreenEvent
}