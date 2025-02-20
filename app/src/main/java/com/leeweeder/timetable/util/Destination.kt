package com.leeweeder.timetable.util

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import kotlin.reflect.typeOf

sealed interface Destination {

    sealed interface Dialog : Destination {

        @Serializable
        data class TimeTableSetupDialog(
            val timeTableName: String,
            val isInitialization: Boolean,
            val selectedTimeTableId: Int
        ) : Dialog

        @Serializable
        data class TimetableNameDialog(
            val isInitialization: Boolean = false,
            val selectedTimeTableId: Int,
            val timetable: Timetable? = null
        ) : Dialog {
            companion object {
                val typeMap = mapOf(typeOf<Timetable?>() to serializableType<Timetable?>(true))

                fun from(savedStateHandle: SavedStateHandle): TimetableNameDialog {
                    return savedStateHandle.toRoute<TimetableNameDialog>(typeMap)
                }
            }
        }

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
        data class InstructorDialog(
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

@Serializable
data class Timetable(
    val id: Int,
    val name: String
)


