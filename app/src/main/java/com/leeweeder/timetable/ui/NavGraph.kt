package com.leeweeder.timetable.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.toRoute
import com.leeweeder.timetable.NonExistingMainTimeTableId
import com.leeweeder.timetable.domain.model.Session
import com.leeweeder.timetable.domain.model.SubjectInstructorCrossRef
import com.leeweeder.timetable.ui.instructor.UpsertInstructorDialog
import com.leeweeder.timetable.ui.schedule.ScheduleEntryDialog
import com.leeweeder.timetable.ui.subject.SubjectDialog
import com.leeweeder.timetable.ui.timetable_setup.GetTimeTableNameDialog
import com.leeweeder.timetable.ui.timetable_setup.TimeTableSetupDialog
import com.leeweeder.timetable.util.Destination

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: Destination,
    mainTimeTableId: Int,
    modifier: Modifier,
    // This is for showing a snackbar and enabling undo operation
    onSuccessfulScheduleEntryDeletion: (subjectInstructorCrossRef: SubjectInstructorCrossRef, affectedSessions: List<Session>) -> Unit
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
        navController = navController, startDestination = startDestination, modifier = modifier
    ) {

        composable<Destination.Screen.HomeScreen> {
            HomeScreen(selectedTimeTableId = it.toRoute<Destination.Screen.HomeScreen>().selectedTimeTableId,
                onNavigateToGetNewTimeTableNameDialog = { isInitialization, selectedTimeTableId ->
                    navController.navigate(
                        Destination.Dialog.GetTimeTableNameDialog(
                            isInitialization, selectedTimeTableId
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
                })
        }


        dialog<Destination.Dialog.TimeTableSetupDialog> {
            TimeTableSetupDialog(onDismissRequest = {
                navigateUp()
            }, onNavigateToHomeScreen = {
                navigateAndPreventGoingBack(Destination.Screen.HomeScreen(selectedTimeTableId = it))
            })
        }

        dialog<Destination.Dialog.GetTimeTableNameDialog> {
            GetTimeTableNameDialog(onDismissRequest = {
                navigateUp()
            }, onNavigateToTimeTableSetupDialog = { timeTableName, isInitialization ->
                navController.navigate(
                    Destination.Dialog.TimeTableSetupDialog(
                        timeTableName = timeTableName,
                        isInitialization = isInitialization,
                        selectedTimeTableId = it.toRoute<Destination.Dialog.GetTimeTableNameDialog>().selectedTimeTableId
                    )
                )
            }, isCancelButtonEnabled = mainTimeTableId != NonExistingMainTimeTableId
            )
        }

        dialog<Destination.Dialog.ScheduleEntryDialog> { backStackEntry ->
            ScheduleEntryDialog(onNavigateBack = {
                navigateUp()
            }, onNavigateToSubjectDialog = {
                navController.navigate(
                    Destination.Dialog.SubjectDialog(
                        id = it?.id ?: 0,
                        description = it?.description ?: "",
                        code = it?.code ?: ""
                    )
                )
            }, onNavigateToUpsertInstructorDialog = {
                navController.navigate(
                    Destination.Dialog.UpsertInstructorDialog(
                        id = it?.id ?: 0, name = it?.name ?: ""
                    )
                )
            }, onNavigateToHomeScreen = {
                navigateAndPreventGoingBack(
                    Destination.Screen.HomeScreen(
                        subjectInstructorIdToBeScheduled = it,
                        selectedTimeTableId = backStackEntry.toRoute<Destination.Dialog.ScheduleEntryDialog>().timeTableId
                    )
                )
            }, onSuccessfulScheduleEntryDeletion = onSuccessfulScheduleEntryDeletion)
        }

        dialog<Destination.Dialog.SubjectDialog> {
            SubjectDialog(onDismissRequest = {
                navigateUp()
            })
        }

        dialog<Destination.Dialog.UpsertInstructorDialog> {
            UpsertInstructorDialog(onDismissRequest = {
                navigateUp()
            })
        }
    }
}