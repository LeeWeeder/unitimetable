package com.leeweeder.timetable

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.leeweeder.timetable.ui.HomeScreen
import com.leeweeder.timetable.ui.instructor.UpsertInstructorDialog
import com.leeweeder.timetable.ui.schedule.ScheduleEntryDialog
import com.leeweeder.timetable.ui.subject.UpsertSubjectDialog
import com.leeweeder.timetable.ui.theme.AppTheme
import com.leeweeder.timetable.ui.timetable_setup.GetTimeTableNameDialog
import com.leeweeder.timetable.ui.timetable_setup.TimeTableSetupDialog
import com.leeweeder.timetable.util.Destination
import org.koin.androidx.viewmodel.ext.android.viewModel


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel by viewModel<MainActivityViewModel>()

        installSplashScreen().setKeepOnScreenCondition {
            viewModel.uiState.value.isLoading
        }

        enableEdgeToEdge(
            navigationBarStyle = SystemBarStyle.auto(
                lightScrim = Color.TRANSPARENT,
                darkScrim = Color.TRANSPARENT
            )
        )

        setContent {
            AppTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                if (!uiState.isLoading) {
                    val navController = rememberNavController()
                    MainNavHost(
                        navController = navController,
                        startDestination = uiState.startDestination,
                        mainTimeTableId = uiState.mainTimeTableId
                    )
                }
            }
        }
    }
}

@Composable
private fun MainNavHost(
    navController: NavHostController,
    startDestination: Destination,
    mainTimeTableId: Int
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
        navController = navController,
        startDestination = startDestination
    ) {

        homeScreen(
            onNavigateToGetNewTimeTableNameDialog = { isInitialization, selectedTimeTableId ->
                navController.navigate(
                    Destination.Dialog.GetTimeTableNameDialog(
                        isInitialization,
                        selectedTimeTableId
                    )
                )
            },
            onNavigateToScheduleEntryDialog = {
                navController.navigate(it)
            }
        )

        timeTableSetupDialog(
            onNavigateUp = {
                navigateUp()
            },
            onNavigateToHomeScreen = {
                navigateAndPreventGoingBack(Destination.Screen.HomeScreen(selectedTimeTableId = it))
            }
        )

        getTimeTableNameDialog(
            onNavigateUp = {
                navigateUp()
            },
            onNavigateToTimeTableSetupDialog = {
                navController.navigate(it)
            },
            isCancelable = mainTimeTableId != NonExistingMainTimeTableId
        )

        dialog<Destination.Dialog.ScheduleEntryDialog> { backStackEntry ->
            ScheduleEntryDialog(
                onNavigateBack = {
                    navigateUp()
                }, onNavigateToUpsertSubjectDialog = {
                    navController.navigate(
                        Destination.Dialog.UpsertSubjectDialog(
                            id = it?.id ?: 0,
                            description = it?.description ?: "",
                            code = it?.code ?: ""
                        )
                    )
                }, onNavigateToUpsertInstructorDialog = {
                    navController.navigate(
                        Destination.Dialog.UpsertInstructorDialog(
                            id = it?.id ?: 0,
                            name = it?.name ?: ""
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
                }
            )
        }

        dialog<Destination.Dialog.UpsertSubjectDialog> {
            UpsertSubjectDialog(onDismissRequest = {
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

private fun NavGraphBuilder.homeScreen(
    onNavigateToGetNewTimeTableNameDialog: (isInitialization: Boolean, selectedTimeTableId: Int) -> Unit,
    onNavigateToScheduleEntryDialog: (Destination.Dialog.ScheduleEntryDialog) -> Unit
) {
    composable<Destination.Screen.HomeScreen> {
        HomeScreen(
            selectedTimeTableId = it.toRoute<Destination.Screen.HomeScreen>().selectedTimeTableId,
            onNavigateToGetNewTimeTableNameDialog = { isInitialization, selectedTimeTableId ->
                onNavigateToGetNewTimeTableNameDialog(isInitialization, selectedTimeTableId)
            },
            onNavigateToScheduleEntryDialog = { subjectInstructorIdToBeScheduled, selectedTimeTableId ->
                onNavigateToScheduleEntryDialog(
                    Destination.Dialog.ScheduleEntryDialog(
                        subjectInstructorIdToBeScheduled,
                        selectedTimeTableId
                    )
                )
            }
        )
    }
}

private fun NavGraphBuilder.getTimeTableNameDialog(
    onNavigateUp: () -> Unit,
    onNavigateToTimeTableSetupDialog: (Destination.Dialog.TimeTableSetupDialog) -> Unit,
    isCancelable: Boolean
) {
    dialog<Destination.Dialog.GetTimeTableNameDialog> {
        GetTimeTableNameDialog(
            onDismissRequest = onNavigateUp,
            onNavigateToTimeTableSetupDialog = { timeTableName, isInitialization ->
                onNavigateToTimeTableSetupDialog(
                    Destination.Dialog.TimeTableSetupDialog(
                        timeTableName = timeTableName,
                        isInitialization = isInitialization,
                        selectedTimeTableId = it.toRoute<Destination.Dialog.GetTimeTableNameDialog>().selectedTimeTableId
                    )
                )
            },
            isCancelButtonEnabled = isCancelable
        )
    }
}

private fun NavGraphBuilder.timeTableSetupDialog(
    onNavigateUp: () -> Unit,
    onNavigateToHomeScreen: (selectedTimeTableId: Int) -> Unit
) {
    dialog<Destination.Dialog.TimeTableSetupDialog> {
        TimeTableSetupDialog(
            onDismissRequest = onNavigateUp,
            onNavigateToHomeScreen = onNavigateToHomeScreen
        )
    }
}