package com.leeweeder.timetable.feature_widget.ui

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leeweeder.timetable.domain.model.TimeTable
import com.leeweeder.timetable.domain.repository.DataStoreRepository
import com.leeweeder.timetable.domain.repository.TimeTableRepository
import com.leeweeder.timetable.feature_widget.domain.WidgetPreferenceDataStoreRepository
import com.leeweeder.timetable.ui.components.selection_and_addition_bottom_sheet.SearchableBottomSheetStateFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class WidgetConfigurationScreenViewModel(
    private val dataStoreRepository: DataStoreRepository,
    private val widgetPreferenceDataStoreRepository: WidgetPreferenceDataStoreRepository,
    private val timeTableRepository: TimeTableRepository
) : ViewModel() {
    private val _selectedTimeTable = mutableStateOf<TimeTable?>(null)
    val selectedTimeTable: State<TimeTable?> = _selectedTimeTable

    private val _isSuccess = MutableStateFlow(false)
    val isSuccess: StateFlow<Boolean> = _isSuccess.asStateFlow()

    init {
        viewModelScope.launch {
            val mainTimetableId = dataStoreRepository.timeTablePrefFlow.first().mainTimeTableId

            _selectedTimeTable.value = timeTableRepository.getTimetableById(mainTimetableId)
        }
    }

    fun onEvent(event: WidgetConfigurationScreenEvent) {
        when (event) {
            is WidgetConfigurationScreenEvent.Save -> {
                selectedTimeTable.value?.id?.let {
                    viewModelScope.launch {
                        widgetPreferenceDataStoreRepository.saveWidgetPreferences(event.widgetId, it)
                        _isSuccess.emit(true)
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
    data class Save(val widgetId: Int) : WidgetConfigurationScreenEvent
}