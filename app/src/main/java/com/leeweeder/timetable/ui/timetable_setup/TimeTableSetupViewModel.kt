package com.leeweeder.timetable.ui.timetable_setup

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.leeweeder.timetable.data.DataStoreRepository
import com.leeweeder.timetable.data.source.session.Session
import com.leeweeder.timetable.data.source.session.SessionDataSource
import com.leeweeder.timetable.data.source.timetable.TimeTable
import com.leeweeder.timetable.data.source.timetable.TimeTableDataSource
import com.leeweeder.timetable.ui.util.getDays
import com.leeweeder.timetable.ui.util.getTimes
import com.leeweeder.timetable.util.Destination
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalTime

class TimeTableSetupViewModel(
    private val timeTableDataSource: TimeTableDataSource,
    private val sessionDataSource: SessionDataSource,
    private val dataStoreRepository: DataStoreRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _eventFlow = MutableStateFlow<TimeTableSetUpUiEvent?>(null)
    val eventFlow = _eventFlow.asStateFlow()

    private val _uiState = mutableStateOf(
        TimeTableSetupUiState(
            timeTable = DefaultTimeTable.copy(name = savedStateHandle.toRoute<Destination.Dialog.TimeTableSetupDialog>().timeTableName)
        )
    )
    val uiState: State<TimeTableSetupUiState> = _uiState

    fun onEvent(event: TimeTableSetupEvent) {
        when (event) {
            is TimeTableSetupEvent.ChangeTimeTableName -> {
                _uiState.value = uiState.value.copy(
                    timeTable = uiState.value.timeTable.copy(name = event.name)
                )
            }

            is TimeTableSetupEvent.UpdateNumberOfDays -> {
                val uiState = uiState.value
                val timeTable = uiState.timeTable
                val newNumberOfDays = event.newNumberOfDays

                _uiState.value = uiState.copy(
                    timeTable = timeTable.copy(numberOfDays = newNumberOfDays),
                    days = getDays(timeTable.startingDay, newNumberOfDays)
                )
            }

            is TimeTableSetupEvent.UpdateStartingDay -> {
                val uiState = uiState.value
                val timeTable = uiState.timeTable
                val newStartingDay = event.newStartingDay

                _uiState.value = uiState.copy(
                    timeTable = timeTable.copy(startingDay = newStartingDay),
                    days = getDays(newStartingDay, timeTable.numberOfDays)
                )
            }

            is TimeTableSetupEvent.UpdateTime -> {
                val uiState = uiState.value
                val timeTable = uiState.timeTable
                val newTime = LocalTime.of(event.newHour, 0)

                _uiState.value = when (event.part) {
                    TimeTableSetupEvent.UpdateTime.Part.Start -> {
                        uiState.copy(
                            // Update state
                            timeTable = timeTable.copy(startTime = newTime),
                            // Update schedule
                            periodStartTimes = getTimes(newTime, timeTable.endTime)
                        )
                    }

                    TimeTableSetupEvent.UpdateTime.Part.End -> {
                        uiState.copy(
                            // Update state
                            timeTable = timeTable.copy(endTime = newTime),
                            // Update schedule
                            periodStartTimes = getTimes(timeTable.startTime, newTime)
                        )
                    }
                }
            }

            TimeTableSetupEvent.Save -> {
                viewModelScope.launch {
                    try {

                        val uiState = uiState.value
                        // Insert timetable
                        val timeTableId =
                            timeTableDataSource.insertTimeTable(uiState.timeTable)

                        val sessions = uiState.periodStartTimes
                            .flatMap { startTime ->
                                uiState.days.map { day ->
                                    Session.emptySession(
                                        timeTableId = timeTableId,
                                        dayOfWeek = day,
                                        startTime = startTime
                                    )
                                }
                            }

                        sessionDataSource.insertSessions(sessions)

                        if (savedStateHandle.toRoute<Destination.Dialog.TimeTableSetupDialog>().isInitialization) {
                            dataStoreRepository.setMainTimeTableId(timeTableId)
                        }

                        _eventFlow.emit(TimeTableSetUpUiEvent.FinishedSaving(timeTableId))
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
    data class FinishedSaving(val timeTableId: Int) : TimeTableSetUpUiEvent
}

val DefaultTimeTable = TimeTable(
    name = "Timetable",
    numberOfDays = 5,
    startingDay = DayOfWeek.MONDAY,
    startTime = LocalTime.of(8, 0),
    endTime = LocalTime.of(17, 0)
)

data class TimeTableSetupUiState(
    val timeTable: TimeTable = DefaultTimeTable,
    val periodStartTimes: List<LocalTime> = getTimes(timeTable.startTime, timeTable.endTime),
    val days: List<DayOfWeek> = getDays(timeTable.startingDay, timeTable.numberOfDays)
)