package com.leeweeder.timetable

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import com.leeweeder.timetable.ui.HomeScreen
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
                    NavHost(
                        navController = navController,
                        startDestination = uiState.startDestination
                    ) {
                        composable<Destination.Screen.HomeScreen> {
                            HomeScreen()
                        }

                        dialog<Destination.Dialog.TimeTableSetupDialog> { backStackEntry ->
                            TimeTableSetupDialog(onDismissRequest = {
                                navController.navigateUp()
                            }, onNavigateToHomeScreen = {
                                navController.navigate(Destination.Screen.HomeScreen) {
                                    popUpTo(Destination.Screen.HomeScreen) {
                                        inclusive = true
                                    }
                                }
                            })
                        }

                        dialog<Destination.Dialog.GetTimeTableNameDialog> {
                            GetTimeTableNameDialog(
                                onDismissRequest = {
                                    navController.navigateUp()
                                },
                                onNavigateToTimeTableSetupDialog = {
                                    navController.navigate(
                                        Destination.Dialog.TimeTableSetupDialog(
                                            it
                                        )
                                    )
                                },
                                isCancelButtonEnabled = uiState.startDestination == Destination.Screen.HomeScreen
                            )
                        }
                    }
                }
            }
        }
    }
}