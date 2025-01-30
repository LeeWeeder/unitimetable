package com.leeweeder.timetable.ui.timetable_setup

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leeweeder.timetable.data.source.timetable.TimeTableDataSource
import kotlinx.coroutines.launch

class GetTimeTableNameViewModel(
    private val timeTableDataSource: TimeTableDataSource
) : ViewModel() {
    private val _timeTableNames = mutableStateOf(emptyList<String>())
    private val timeTableNames: State<List<String>> = _timeTableNames

    private val defaultTimeTableName = DefaultTimeTable.name

    private val _timeTableName = mutableStateOf(DefaultTimeTable.name)
    val timeTableName: State<String> = _timeTableName

    init {
        viewModelScope.launch {
            _timeTableNames.value = timeTableDataSource.getTimeTableNames()

            val timeTableCountWithDefaultTimeTableName =
                timeTableNames.value.count {
                    it.startsWith(defaultTimeTableName) || it.matches(
                        Regex("^${defaultTimeTableName} \\(\\d+\\)$")
                    )
                }

            _timeTableName.value =
                if (timeTableCountWithDefaultTimeTableName == 0)
                    defaultTimeTableName
                else "$defaultTimeTableName (${timeTableCountWithDefaultTimeTableName + 1})"
        }
    }
}