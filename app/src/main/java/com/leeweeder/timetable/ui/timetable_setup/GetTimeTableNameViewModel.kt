package com.leeweeder.timetable.ui.timetable_setup

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.leeweeder.timetable.data.source.timetable.TimeTableDataSource
import com.leeweeder.timetable.util.Destination
import kotlinx.coroutines.launch

private const val TAG = "GetTimeTableNameViewModel"

class GetTimeTableNameViewModel(
    private val timeTableDataSource: TimeTableDataSource,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _timeTableNames = mutableStateOf(emptyList<String>())
    private val timeTableNames: State<List<String>> = _timeTableNames

    private val defaultTimeTableName = DefaultTimeTable.name

    private val _timeTableName = mutableStateOf(DefaultTimeTable.name)
    val timeTableName: State<String> = _timeTableName

    val isInitialization = savedStateHandle.toRoute<Destination.Dialog.GetTimeTableNameDialog>().isInitialization

    init {

        viewModelScope.launch {
            _timeTableNames.value = timeTableDataSource.getTimeTableNames()

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

/**
 * Count how many timetable has the name of [DefaultTimeTable].
 *
 * @param timeTableNames The names to be checked.
 *
 * @return The number of timetables that have the [DefaultTimeTable] name.
 * */
fun countTimeTableWithDefaultNames(timeTableNames: List<String>): Int {
    val defaultTimeTableName = DefaultTimeTable.name
    return timeTableNames.count { name ->
        name.startsWith(defaultTimeTableName) || name.matches(Regex("^$defaultTimeTableName\\(\\d+\\)$"))
    }
}