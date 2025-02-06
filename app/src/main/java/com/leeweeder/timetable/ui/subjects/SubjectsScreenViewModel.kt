package com.leeweeder.timetable.ui.subjects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leeweeder.timetable.data.source.session.Session
import com.leeweeder.timetable.data.source.session.SessionDataSource
import com.leeweeder.timetable.data.source.session.toEmptySession
import com.leeweeder.timetable.data.source.subject.Subject
import com.leeweeder.timetable.data.source.subject.SubjectDataSource
import com.leeweeder.timetable.data.source.subject.SubjectWithDetails
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SubjectsScreenViewModel(
    private val subjectDataSource: SubjectDataSource,
    private val sessionDataSource: SessionDataSource
) : ViewModel() {

    val subjectsWithDetails = subjectDataSource.observeSubjectsWithDetails()
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
                        subjectDataSource.deleteSubject(event.subject)
                        sessionDataSource.updateSessions(event.sessions.map { it.toEmptySession() })
                        // TODO: Extract this to a function or a repository/use case
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
    data class DeleteSubject(val subject: Subject, val sessions: List<Session>) :
        SubjectsScreenEvent
}