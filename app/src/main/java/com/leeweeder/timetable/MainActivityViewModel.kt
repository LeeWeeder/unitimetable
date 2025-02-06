package com.leeweeder.timetable

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leeweeder.timetable.data.DataStoreRepository
import com.leeweeder.timetable.util.Destination
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class MainActivityViewModel(
    dataStoreRepository: DataStoreRepository
) : ViewModel() {

    val uiState = dataStoreRepository.timeTablePrefFlow.map {
        if (it.mainTimeTableId == -1) {
            println("Main table id got from main activity is null")
            MainActivityUiState(isLoading = false, Destination.Dialog.GetTimeTableNameDialog)
        } else {
            println("Main table id got from main activity is ${it.mainTimeTableId}")
            MainActivityUiState(isLoading = false)
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000L),
        MainActivityUiState(isLoading = true)
    )
}

data class MainActivityUiState(
    val isLoading: Boolean,
    val startDestination: Destination = Destination.Screen.HomeScreen()
)