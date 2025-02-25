package com.leeweeder.unitimetable.util

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import com.leeweeder.unitimetable.domain.model.SerializableTimetable
import kotlinx.serialization.Serializable

sealed interface Destination {

    sealed interface Dialog : Destination {

        @Serializable
        data class TimeTableSetupDialog(
            val timetable: SerializableTimetable,
            val selectedTimeTableId: Int
        ) : Dialog {

            companion object {
                val typeMap = typeMapBuilder<SerializableTimetable>()

                fun from(savedStateHandle: SavedStateHandle): TimeTableSetupDialog {
                    return savedStateHandle.toRoute<TimeTableSetupDialog>(typeMap)
                }
            }
        }

        @Serializable
        data class TimetableNameDialog(
            val isInitialization: Boolean = false,
            val selectedTimeTableId: Int,
            val timetable: TimetableIdAndName? = null
        ) : Dialog {
            companion object {
                val typeMap = typeMapBuilder<TimetableIdAndName?>()

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
        data class HomeScreen(
            val subjectInstructorIdToBeScheduled: Int? = null,
            val selectedTimeTableId: Int
        ) :
            Screen
    }
}

@Serializable
data class TimetableIdAndName(
    val id: Int,
    val name: String
)


