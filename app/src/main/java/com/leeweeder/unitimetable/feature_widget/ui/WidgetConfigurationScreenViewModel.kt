package com.leeweeder.unitimetable.feature_widget.ui

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leeweeder.unitimetable.domain.model.Timetable
import com.leeweeder.unitimetable.domain.repository.DataStoreRepository
import com.leeweeder.unitimetable.domain.repository.TimetableRepository
import com.leeweeder.unitimetable.ui.components.searchable_bottom_sheet.SearchableBottomSheetStateFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class WidgetConfigurationScreenViewModel(
    private val dataStoreRepository: DataStoreRepository,
    private val timeTableRepository: TimetableRepository
) : ViewModel() {
    private val _selectedTimetable = mutableStateOf<Timetable?>(null)
    val selectedTimetable: State<Timetable?> = _selectedTimetable

    private val _timetableId = MutableStateFlow<Int?>(null)
    val timetableId: StateFlow<Int?> = _timetableId.asStateFlow()

    init {
        viewModelScope.launch {
            // TODO: Fetch the current selected timetable id, default to mainTimeTable if first time
            val mainTimetableId = dataStoreRepository.timeTablePrefFlow.first().mainTimeTableId

            _selectedTimetable.value = timeTableRepository.getTimetableById(mainTimetableId)
        }
    }

    fun onEvent(event: WidgetConfigurationScreenEvent) {
        when (event) {
            is WidgetConfigurationScreenEvent.Save -> {
                selectedTimetable.value?.id?.let {
                    viewModelScope.launch {
                        _timetableId.emit(it)
                    }
                }
            }

            is WidgetConfigurationScreenEvent.SelectTimeTable -> {
                _selectedTimetable.value = event.value
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
    data class SelectTimeTable(val value: Timetable) : WidgetConfigurationScreenEvent
    data object Save : WidgetConfigurationScreenEvent
}