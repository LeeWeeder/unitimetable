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
import com.leeweeder.unitimetable.domain.model.Timetable
import com.leeweeder.unitimetable.domain.repository.DataStoreRepository
import com.leeweeder.unitimetable.domain.repository.TimetableRepository
import com.leeweeder.unitimetable.ui.util.getDays
import com.leeweeder.unitimetable.ui.util.getTimes
import com.leeweeder.unitimetable.util.Destination
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalTime

class TimetableSetupViewModel(
    private val timetableRepository: TimetableRepository,
    private val dataStoreRepository: DataStoreRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _eventFlow = MutableStateFlow<TimeTableSetUpUiEvent?>(null)
    val eventFlow = _eventFlow.asStateFlow()

    val destination = Destination.Dialog.TimeTableSetupDialog.from(savedStateHandle)

    private val _uiState = mutableStateOf(
        TimeTableSetupUiState(
            timetable = destination.timetable.normalize()
        )
    )
    val uiState: State<TimeTableSetupUiState> = _uiState

    fun onEvent(event: TimeTableSetupEvent) {
        when (event) {
            is TimeTableSetupEvent.ChangeTimeTableName -> {
                _uiState.value = uiState.value.copy(
                    timetable = uiState.value.timetable.copy(name = event.name)
                )
            }

            is TimeTableSetupEvent.UpdateNumberOfDays -> {
                val uiState = uiState.value
                val timeTable = uiState.timetable
                val newNumberOfDays = event.newNumberOfDays

                _uiState.value = uiState.copy(
                    timetable = timeTable.copy(numberOfDays = newNumberOfDays),
                    days = getDays(timeTable.startingDay, newNumberOfDays)
                )
            }

            is TimeTableSetupEvent.UpdateStartingDay -> {
                val uiState = uiState.value
                val timeTable = uiState.timetable
                val newStartingDay = event.newStartingDay

                _uiState.value = uiState.copy(
                    timetable = timeTable.copy(startingDay = newStartingDay),
                    days = getDays(newStartingDay, timeTable.numberOfDays)
                )
            }

            is TimeTableSetupEvent.UpdateTime -> {
                val uiState = uiState.value
                val timeTable = uiState.timetable
                val newTime = LocalTime.of(event.newHour, 0)

                _uiState.value = when (event.part) {
                    TimeTableSetupEvent.UpdateTime.Part.Start -> {
                        uiState.copy(
                            // Update state
                            timetable = timeTable.copy(startTime = newTime),
                            // Update schedule
                            periodStartTimes = getTimes(newTime, timeTable.endTime)
                        )
                    }

                    TimeTableSetupEvent.UpdateTime.Part.End -> {
                        uiState.copy(
                            // Update state
                            timetable = timeTable.copy(endTime = newTime),
                            // Update schedule
                            periodStartTimes = getTimes(timeTable.startTime, newTime)
                        )
                    }
                }
            }

            TimeTableSetupEvent.Save -> {
                viewModelScope.launch {
                    try {
                        val timetable = uiState.value.timetable

                        // When timetable is not null, just update the timetable
                        val isTimetableAlreadyExisting =
                            timetableRepository.getTimetableById(timetable.id) != null

                        var timetableId = timetable.id

                        if (isTimetableAlreadyExisting) {
                            timetableRepository.editTimetableLayout(timetable)
                        } else {
                            // Insert timetable
                            timetableId = timetableRepository.insertTimetable(timetable)
                            dataStoreRepository.setSelectedTimetable(timetableId)
                        }

                        _eventFlow.emit(TimeTableSetUpUiEvent.FinishedSaving(timetableId))

                    } catch (e: Exception) {
                        // TODO: Implement error handling
                        Log.e("TimeTableSetupViewModel", "Error saving time table", e)
                    }
                }
            }
        }
    }
}

sealed interface TimeTableSetupEvent {
    data class ChangeTimeTableName(val name: String) : TimeTableSetupEvent

    data class UpdateNumberOfDays(val newNumberOfDays: Int) : TimeTableSetupEvent
    data class UpdateStartingDay(val newStartingDay: DayOfWeek) : TimeTableSetupEvent
    data class UpdateTime(val newHour: Int, val part: Part) : TimeTableSetupEvent {
        enum class Part {
            Start,
            End
        }
    }

    data object Save : TimeTableSetupEvent
}

sealed interface TimeTableSetUpUiEvent {
    data class FinishedSaving(val timetableId: Int) : TimeTableSetUpUiEvent
}

val DefaultTimetable = Timetable(
    name = "Timetable",
    numberOfDays = 5,
    startingDay = DayOfWeek.MONDAY,
    startTime = LocalTime.of(8, 0),
    endTime = LocalTime.of(17, 0)
)

data class TimeTableSetupUiState(
    val timetable: Timetable = DefaultTimetable,
    val periodStartTimes: List<LocalTime> = getTimes(timetable.startTime, timetable.endTime),
    val days: List<DayOfWeek> = getDays(timetable.startingDay, timetable.numberOfDays)
)