package com.leeweeder.unitimetable.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.toRoute
import com.leeweeder.unitimetable.NonExistingMainTimeTableId
import com.leeweeder.unitimetable.domain.model.Instructor
import com.leeweeder.unitimetable.domain.model.Session
import com.leeweeder.unitimetable.domain.model.Subject
import com.leeweeder.unitimetable.domain.model.SubjectInstructorCrossRef
import com.leeweeder.unitimetable.domain.relation.TimetableWithSession
import com.leeweeder.unitimetable.ui.instructor.InstructorDialog
import com.leeweeder.unitimetable.ui.schedule.ScheduleEntryDialog
import com.leeweeder.unitimetable.ui.subject.SubjectDialog
import com.leeweeder.unitimetable.ui.timetable_setup.DefaultTimetable
import com.leeweeder.unitimetable.ui.timetable_setup.TimeTableNameDialog
import com.leeweeder.unitimetable.ui.timetable_setup.TimeTableSetupDialog
import com.leeweeder.unitimetable.util.Destination
import kotlinx.coroutines.launch

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: Destination,
    mainTimeTableId: Int,
    snackbarHostState: SnackbarHostState,
    // This is for showing a snackbar and enabling undo operation
    onSuccessfulScheduleEntryDeletion: (subjectInstructorCrossRef: SubjectInstructorCrossRef, affectedSessions: List<Session>) -> Unit,
    onSuccessfulSubjectDeletion: (Subject, List<Session>, List<SubjectInstructorCrossRef>) -> Unit,
    onSuccessfulInstructorDeletion: (Instructor, List<Int>) -> Unit,
    onSuccessfulTimeTableDeletion: (TimetableWithSession) -> Unit
) {

    fun navigateUp() {
        navController.navigateUp()
    }

    fun navigateAndPreventGoingBack(destination: Destination) {
        navController.navigate(destination) {
            popUpTo(destination) {
                inclusive = true
            }
        }
    }

    NavHost(
        navController = navController, startDestination = startDestination
    ) {

        composable<Destination.Screen.HomeScreen> {
            HomeScreen(
                selectedTimeTableId = it.toRoute<Destination.Screen.HomeScreen>().selectedTimeTableId,
                onNavigateToTimeTableNameDialog = { isInitialization, selectedTimeTableId, timetable ->
                    navController.navigate(
                        Destination.Dialog.TimetableNameDialog(
                            isInitialization, selectedTimeTableId, timetable
                        )
                    )
                },
                onNavigateToScheduleEntryDialog = { subjectInstructorIdToBeScheduled, selectedTimeTableId ->
                    navController.navigate(
                        Destination.Dialog.ScheduleEntryDialog(
                            subjectInstructorIdToBeScheduled,
                            selectedTimeTableId
                        )
                    )
                },
                onNavigateToEditTimetableLayoutScreen = {
                    navController.navigate(
                        Destination.Dialog.TimeTableSetupDialog(
                            it.serialize(),
                            it.id
                        )
                    )
                },
                onDeleteTimetableSuccessful = onSuccessfulTimeTableDeletion
            )
        }


        composable<Destination.Dialog.TimeTableSetupDialog>(typeMap = Destination.Dialog.TimeTableSetupDialog.typeMap) {
            TimeTableSetupDialog(onDismissRequest = {
                navigateUp()
            }, onNavigateToHomeScreen = {
                navigateAndPreventGoingBack(Destination.Screen.HomeScreen(selectedTimeTableId = it))
            })
        }

        dialog<Destination.Dialog.TimetableNameDialog>(
            typeMap = Destination.Dialog.TimetableNameDialog.typeMap
        ) {
            TimeTableNameDialog(onDismissRequest = {
                navigateUp()
            }, onNavigateToTimeTableSetupDialog = { timetableName, isInitialization ->
                navController.navigate(
                    Destination.Dialog.TimeTableSetupDialog(
                        timetable = DefaultTimetable.copy(name = timetableName).serialize(),
                        selectedTimeTableId = it.toRoute<Destination.Dialog.TimetableNameDialog>().selectedTimeTableId
                    )
                )
            }, isCancelButtonEnabled = mainTimeTableId != NonExistingMainTimeTableId
            )
        }

        dialog<Destination.Dialog.ScheduleEntryDialog> { backStackEntry ->
            ScheduleEntryDialog(
                onNavigateBack = {
                    navigateUp()
                },
                onNavigateToSubjectDialog = {
                    navController.navigate(
                        Destination.Dialog.SubjectDialog(
                            id = it?.id ?: 0,
                            description = it?.description ?: "",
                            code = it?.code ?: ""
                        )
                    )
                },
                onNavigateToInstructorDialog = {
                    navController.navigate(
                        Destination.Dialog.InstructorDialog(
                            id = it?.id ?: 0, name = it?.name ?: ""
                        )
                    )
                },
                onNavigateToHomeScreen = {
                    navigateAndPreventGoingBack(
                        Destination.Screen.HomeScreen(
                            subjectInstructorIdToBeScheduled = it,
                            selectedTimeTableId = backStackEntry.toRoute<Destination.Dialog.ScheduleEntryDialog>().timeTableId
                        )
                    )
                },
                onSuccessfulScheduleEntryDeletion = onSuccessfulScheduleEntryDeletion,
                snackbarHostState = snackbarHostState
            )
        }

        dialog<Destination.Dialog.SubjectDialog> {
            SubjectDialog(onDismissRequest = {
                navigateUp()
            }, onDeleteSuccessful = onSuccessfulSubjectDeletion)
        }

        dialog<Destination.Dialog.InstructorDialog> {
            val scope = rememberCoroutineScope()
            InstructorDialog(onDismissRequest = {
                navigateUp()
            }, onDeleteSuccessful = onSuccessfulInstructorDeletion, onDeletionError = {
                scope.launch {
                    snackbarHostState.showSnackbar(it)
                }
            })
        }
    }
}