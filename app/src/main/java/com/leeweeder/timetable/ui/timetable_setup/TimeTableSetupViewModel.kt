package com.leeweeder.timetable.ui.timetable_setup

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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalTime

class TimeTableSetupViewModel(
    private val timeTableDataSource: TimeTableDataSource,
    private val sessionDataSource: SessionDataSource,
    private val dataStoreRepository: DataStoreRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private val _uiState = mutableStateOf(
        TimeTableSetupUiState(
            timeTable = DefaultTimeTable.copy(name = savedStateHandle.toRoute<Destination.Dialog.TimeTableSetupDialog>().timeTableName)
        )
    )
    val uiState = _uiState

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
                println("Time table saving")
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

                        // Insert sessions
                        println("Inserting sessions:")
                        sessions.forEach {
                            println("\t :$it")
                        }

                        sessionDataSource.insertSessions(sessions)

                        println("Finished inserting sessions")

                        dataStoreRepository.setMainTimeTableId(timeTableId)

                        println("MainTimeTableId is $timeTableId")
                    } catch (e: Exception) {
                        // TODO: Implement error handling
                        println("There is an error inserting: ${e.message}")
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

sealed interface UiEvent {
    data object FinishedSaving : UiEvent
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