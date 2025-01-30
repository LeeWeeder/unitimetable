package com.leeweeder.timetable.util

import kotlinx.serialization.Serializable

sealed interface Destination {

    sealed interface Dialog : Destination {

        @Serializable
        data class TimeTableSetupDialog(val timeTableName: String) : Dialog

        @Serializable
        data object GetTimeTableNameDialog : Dialog
    }

    sealed interface Screen : Destination {

        @Serializable
        data object HomeScreen : Screen
    }
}

