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

package com.leeweeder.unitimetable.ui.timetable_setup

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leeweeder.unitimetable.domain.repository.TimetableRepository
import com.leeweeder.unitimetable.util.Destination
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val TAG = "GetTimeTableNameViewModel"

class TimeTableNameViewModel(
    private val timeTableRepository: TimetableRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _timeTableNames = mutableStateOf(emptyList<String>())
    private val timeTableNames: State<List<String>> = _timeTableNames

    private val defaultTimeTableName = DefaultTimetable.name

    private val _timeTableName = mutableStateOf(DefaultTimetable.name)
    val timeTableName: State<String> = _timeTableName

    private val _isSuccess = MutableStateFlow(false)
    val isSuccess: StateFlow<Boolean> = _isSuccess.asStateFlow()

    private val route = Destination.Dialog.TimetableNameDialog.from(savedStateHandle)
    private val timetable = route.timetable

    val isRename: Boolean
        get() = timetable != null

    val isInitialization: Boolean
        get() = route.isInitialization.also {
            Log.d(TAG, "Initialization: $it")
        }

    init {
        // If the timetable is not null, this means the purpose of opening this dialog is to rename a timetable with given id and initial name
        if (isRename) {
            // The timetable should not be null since it's checked in isRename (I could be wrong)
            _timeTableName.value = timetable!!.name
        } else {
            // If the timetable is null, then it's for new timetable
            viewModelScope.launch {
                _timeTableNames.value = timeTableRepository.getTimeTableNames()

                Log.d(TAG, "Time table names: ${timeTableNames.value}")

                val timeTableCountWithDefaultTimeTableName =
                    timeTableNames.value.count {
                        it.startsWith(defaultTimeTableName) || it.matches(
                            Regex("^${defaultTimeTableName} \\(\\d+\\)$")
                        )
                    }

                Log.d(
                    TAG,
                    "Time table name count with DefaultTimeTable name (Timetable): $timeTableCountWithDefaultTimeTableName"
                )

                _timeTableName.value =
                    if (timeTableCountWithDefaultTimeTableName == 0)
                        defaultTimeTableName
                    else "$defaultTimeTableName (${timeTableCountWithDefaultTimeTableName + 1})"

                Log.d(TAG, "Generated time table name: ${timeTableName.value}")
            }
        }
    }

    fun saveTimeTableName(newName: String) {
        route.timetable?.let {
            viewModelScope.launch {
                try {
                    timeTableRepository.updateTimetableName(
                        route.timetable.id,
                        newName
                    )
                    _isSuccess.emit(true)
                    Log.d(this@TimeTableNameViewModel::class.simpleName, "This is ran")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to update time table name", e)
                }
            }
        }
    }
}

/**
 * Count how many timetable has the name of [DefaultTimetable].
 *
 * @param timeTableNames The names to be checked.
 *
 * @return The number of timetables that have the [DefaultTimetable] name.
 * */
fun countTimeTableWithDefaultNames(timeTableNames: List<String>): Int {
    val defaultTimeTableName = DefaultTimetable.name
    return timeTableNames.count { name ->
        name.startsWith(defaultTimeTableName) || name.matches(Regex("^$defaultTimeTableName\\(\\d+\\)$"))
    }
}