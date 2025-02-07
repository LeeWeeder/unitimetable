package com.leeweeder.timetable

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leeweeder.timetable.domain.repository.DataStoreRepository
import com.leeweeder.timetable.util.Destination
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

const val NonExistingMainTimeTableId = -1

class MainActivityViewModel(
    dataStoreRepository: DataStoreRepository
) : ViewModel() {

    val uiState = dataStoreRepository.timeTablePrefFlow.map {
        if (it.mainTimeTableId == NonExistingMainTimeTableId) {
            MainActivityUiState(
                isLoading = false,
                startDestination = Destination.Dialog.GetTimeTableNameDialog(
                    isInitialization = true,
                    selectedTimeTableId = -1
                )
            )
        } else {
            MainActivityUiState(isLoading = false, mainTimeTableId = it.mainTimeTableId)
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000L),
        MainActivityUiState(isLoading = true)
    )
}

data class MainActivityUiState(
    val isLoading: Boolean,
    val mainTimeTableId: Int = NonExistingMainTimeTableId,
    val startDestination: Destination = Destination.Screen.HomeScreen(selectedTimeTableId = mainTimeTableId)
)