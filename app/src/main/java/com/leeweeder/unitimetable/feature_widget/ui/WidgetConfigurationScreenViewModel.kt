package com.leeweeder.unitimetable.feature_widget.ui

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leeweeder.unitimetable.domain.model.TimeTable
import com.leeweeder.unitimetable.domain.relation.TimeTableWithSession
import com.leeweeder.unitimetable.domain.repository.DataStoreRepository
import com.leeweeder.unitimetable.domain.repository.TimeTableRepository
import com.leeweeder.unitimetable.ui.components.searchable_bottom_sheet.SearchableBottomSheetStateFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class WidgetConfigurationScreenViewModel(
    private val dataStoreRepository: DataStoreRepository,
    private val timeTableRepository: TimeTableRepository
) : ViewModel() {
    private val _selectedTimeTable = mutableStateOf<TimeTable?>(null)
    val selectedTimeTable: State<TimeTable?> = _selectedTimeTable

    private val _success = MutableStateFlow<TimeTableWithSession?>(null)
    val success: StateFlow<TimeTableWithSession?> = _success.asStateFlow()

    init {
        viewModelScope.launch {
            // TODO: Fetch the current selected timetable id, default to mainTimeTable if first time
            val mainTimetableId = dataStoreRepository.timeTablePrefFlow.first().mainTimeTableId

            _selectedTimeTable.value = timeTableRepository.getTimetableById(mainTimetableId)
        }
    }

    fun onEvent(event: WidgetConfigurationScreenEvent) {
        when (event) {
            is WidgetConfigurationScreenEvent.Save -> {
                selectedTimeTable.value?.id?.let {
                    viewModelScope.launch {
                        val timeTableWithSession = timeTableRepository.getTimeTableWithDetails(
                            selectedTimeTable.value?.id ?: 0
                        )

                        if (timeTableWithSession == null) {
                            return@launch
                        }

                        timeTableWithSession.let {
                            _success.emit(it)
                        }
                    }
                }
            }

            is WidgetConfigurationScreenEvent.SelectTimeTable -> {
                _selectedTimeTable.value = event.value
            }
        }
    }

    val bottomSheetState = SearchableBottomSheetStateFactory(viewModelScope).create(
        timeTableRepository.observeTimeTables()
    ) { timeTable, searchQuery ->
        timeTable.name.lowercase().contains(searchQuery.lowercase())
    }
}

sealed interface WidgetConfigurationScreenEvent {
    data class SelectTimeTable(val value: TimeTable) : WidgetConfigurationScreenEvent
    data object Save : WidgetConfigurationScreenEvent
}