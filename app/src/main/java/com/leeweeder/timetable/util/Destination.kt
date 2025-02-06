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
        data class HomeScreen(val subjectIdToBeEdited: Int? = null) : Screen

        @Serializable
        data object SubjectsScreen : Screen
    }
}

