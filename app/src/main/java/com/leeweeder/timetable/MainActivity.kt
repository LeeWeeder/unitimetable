package com.leeweeder.timetable

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
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
import com.leeweeder.timetable.ui.subjects.SubjectsScreen
import com.leeweeder.timetable.ui.theme.TimeTableTheme
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

        enableEdgeToEdge()

        setContent {
            TimeTableTheme {
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
            onNavigateToSubjectsScreen = {
                navController.navigate(it)
            },
            onNavigateToGetNewTimeTableNameDialog = { isInitialization, selectedTimeTableId ->
                navController.navigate(
                    Destination.Dialog.GetTimeTableNameDialog(
                        isInitialization,
                        selectedTimeTableId
                    )
                )
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

        subjectsScreen(
            onNavigateBack = {
                navController.popBackStack()
            },
            onNavigateToHomeScreenForSubjectEdit = {
                navigateAndPreventGoingBack(it)
            }
        )
    }
}

private fun NavGraphBuilder.homeScreen(
    onNavigateToSubjectsScreen: (Destination.Screen.SubjectsScreen) -> Unit,
    onNavigateToGetNewTimeTableNameDialog: (isInitialization: Boolean, selectedTimeTableId: Int) -> Unit
) {
    composable<Destination.Screen.HomeScreen> {
        HomeScreen(
            selectedTimeTableId = it.toRoute<Destination.Screen.HomeScreen>().selectedTimeTableId,
            onNavigateToSubjectsScreen = {
                onNavigateToSubjectsScreen(Destination.Screen.SubjectsScreen(it))
            },
            onNavigateToGetNewTimeTableNameDialog = { isInitialization, selectedTimeTableId ->
                onNavigateToGetNewTimeTableNameDialog(isInitialization, selectedTimeTableId)
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

private fun NavGraphBuilder.subjectsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToHomeScreenForSubjectEdit: (Destination.Screen.HomeScreen) -> Unit
) {
    composable<Destination.Screen.SubjectsScreen> { backStackEntry ->
        SubjectsScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToHomeScreenForSubjectEdit = {
                val selectedTimeTableId =
                    backStackEntry.toRoute<Destination.Screen.SubjectsScreen>().timeTableId

                Log.d("subjectsScreen", "Selected time table id: $selectedTimeTableId")

                onNavigateToHomeScreenForSubjectEdit(
                    Destination.Screen.HomeScreen(
                        subjectIdToBeEdited = it,
                        selectedTimeTableId = selectedTimeTableId
                    )
                )
            }
        )
    }
}