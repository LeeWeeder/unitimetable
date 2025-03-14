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

package com.leeweeder.unitimetable.feature_widget.ui

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leeweeder.unitimetable.domain.model.Timetable
import com.leeweeder.unitimetable.domain.repository.TimetableRepository
import com.leeweeder.unitimetable.feature_widget.model.DisplayOption
import com.leeweeder.unitimetable.ui.components.searchable_bottom_sheet.SearchableBottomSheetStateFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class WidgetConfigurationScreenViewModel(
    timetableRepository: TimetableRepository
) : ViewModel() {
    private val _selectedTimetable = mutableStateOf<Timetable?>(null)
    val selectedTimetable: State<Timetable?> = _selectedTimetable

    private val _displayOptions = mutableStateOf(DisplayOption.DEFAULT)
    val displayOptions: State<Set<DisplayOption>> = _displayOptions

    private val timetablesFlow: Flow<List<Timetable>> = timetableRepository.observeTimetables()

    val bottomSheetState = SearchableBottomSheetStateFactory(viewModelScope).create(
        timetablesFlow
    ) { timetable, searchQuery ->
        timetable.name.lowercase().contains(searchQuery.lowercase())
    }

    fun selectTimetable(id: Int?) {
        viewModelScope.launch {
            // Wait for first emission of timetables
            val timetables: List<Timetable> = timetablesFlow.first()
            if (timetables.isEmpty()) return@launch

            _selectedTimetable.value = when (id) {
                null -> timetables.first()
                else -> timetables.find { it.id == id } ?: timetables.first()
            }
        }
    }

    fun toggleDisplayOption(option: DisplayOption) {
        if (_displayOptions.value.contains(option)) {
            // Only remove if it's not the last selected option
            if (_displayOptions.value.size > 1) {
                _displayOptions.value = _displayOptions.value - option
            }
        } else {
            _displayOptions.value = _displayOptions.value + option
        }
    }

    fun setDisplayOptions(options: Set<DisplayOption>) {
        // Ensure we always have at least one option
        _displayOptions.value = if (options.isEmpty()) {
            DisplayOption.DEFAULT
        } else {
            options
        }
    }
}