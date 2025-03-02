/*
 * Copyright (C) 2025 Lyniel Jhay G. Maquilan (@LeeWeeder)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.leeweeder.unitimetable.util

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import com.leeweeder.unitimetable.domain.model.SerializableTimetable
import kotlinx.serialization.Serializable

sealed interface Destination {

    sealed interface Dialog : Destination {

        @Serializable
        data class TimeTableSetupDialog(
            val timetable: SerializableTimetable
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
            val subjectInstructorId: Int?
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
            val subjectInstructorIdToBeScheduled: Int? = null
        ) :
            Screen
    }
}

@Serializable
data class TimetableIdAndName(
    val id: Int,
    val name: String
)


