package com.leeweeder.timetable

import android.os.Bundle
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
                        startDestination = uiState.startDestination
                    )
                }
            }
        }
    }
}

@Composable
private fun MainNavHost(navController: NavHostController, startDestination: Destination) {

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

        homeScreen { navController.navigate(Destination.Screen.SubjectsScreen) }

        timeTableSetupDialog(
            onNavigateUp = {
                navigateUp()
            },
            onNavigateToHomeScreen = {
                navigateAndPreventGoingBack(Destination.Screen.HomeScreen())
            }
        )

        getTimeTableNameDialog(
            onNavigateUp = {
                navigateUp()
            },
            onNavigateToTimeTableSetupDialog = {
                navController.navigate(Destination.Dialog.TimeTableSetupDialog(it))
            },
            isCancelable = startDestination == Destination.Screen.HomeScreen
        )

        subjectsScreen(
            onNavigateBack = {
                navController.popBackStack()
            },
            onNavigateToHomeScreenForSubjectEdit = {
                navigateAndPreventGoingBack(Destination.Screen.HomeScreen(it))
            }
        )
    }
}

private fun NavGraphBuilder.homeScreen(
    onNavigateToSubjectsScreen: () -> Unit
) {
    composable<Destination.Screen.HomeScreen> {
        HomeScreen(onNavigateToSubjectsScreen = onNavigateToSubjectsScreen)
    }
}

private fun NavGraphBuilder.getTimeTableNameDialog(
    onNavigateUp: () -> Unit,
    onNavigateToTimeTableSetupDialog: (String) -> Unit,
    isCancelable: Boolean
) {
    dialog<Destination.Dialog.GetTimeTableNameDialog> {
        GetTimeTableNameDialog(
            onDismissRequest = onNavigateUp,
            onNavigateToTimeTableSetupDialog = onNavigateToTimeTableSetupDialog,
            isCancelButtonEnabled = isCancelable
        )
    }
}

private fun NavGraphBuilder.timeTableSetupDialog(
    onNavigateUp: () -> Unit,
    onNavigateToHomeScreen: () -> Unit
) {
    dialog<Destination.Dialog.TimeTableSetupDialog> { backStackEntry ->
        TimeTableSetupDialog(
            onDismissRequest = onNavigateUp,
            onNavigateToHomeScreen = onNavigateToHomeScreen
        )
    }
}

private fun NavGraphBuilder.subjectsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToHomeScreenForSubjectEdit: (Int) -> Unit
) {
    composable<Destination.Screen.SubjectsScreen> {
        SubjectsScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToHomeScreenForSubjectEdit = onNavigateToHomeScreenForSubjectEdit
        )
    }
}