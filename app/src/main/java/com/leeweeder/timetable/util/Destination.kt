package com.leeweeder.timetable.util

import kotlinx.serialization.Serializable

sealed interface Destination {

    sealed interface Dialog : Destination {

        @Serializable
        data class TimeTableSetupDialog(
            val timeTableName: String,
            val isInitialization: Boolean,
            val selectedTimeTableId: Int
        ) : Dialog

        @Serializable
        data class GetTimeTableNameDialog(
            val isInitialization: Boolean = false,
            val selectedTimeTableId: Int
        ) : Dialog

        @Serializable
        data class ScheduleEntryDialog(
            val subjectInstructorId: Int?,
            val timeTableId: Int
        ) : Dialog

        @Serializable
        data class SubjectDialog(
            val id: Int,
            val description: String,
            val code: String
        ) : Dialog

        @Serializable
        data class UpsertInstructorDialog(
            val id: Int,
            val name: String
        ) : Dialog
    }

    sealed interface Screen : Destination {

        @Serializable
        data class HomeScreen(val subjectInstructorIdToBeScheduled: Int? = null, val selectedTimeTableId: Int) :
            Screen
    }
}

