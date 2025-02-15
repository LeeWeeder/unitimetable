package com.leeweeder.timetable

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.leeweeder.timetable.ui.NavGraph
import com.leeweeder.timetable.ui.theme.AppTheme
import kotlinx.coroutines.launch
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

                    val snackbarHostState = remember { SnackbarHostState() }

                    val scope = rememberCoroutineScope()

                    Scaffold(snackbarHost = {
                        SnackbarHost(snackbarHostState)
                    }) { paddingValues ->
                        NavGraph(
                            navController = navController,
                            startDestination = uiState.startDestination,
                            mainTimeTableId = uiState.mainTimeTableId,
                            modifier = Modifier.padding(paddingValues),
                            onSuccessfulScheduleEntryDeletion = { subjectInstructorCrossRef, affectedSessions ->
                                scope.launch {
                                    val result = snackbarHostState.showSnackbar(
                                        message = "Schedule entry deleted successfully. Affected sessions: ${affectedSessions.size}",
                                        actionLabel = "Undo"
                                    )

                                    if (result == SnackbarResult.ActionPerformed) {
                                        viewModel.onEvent(
                                            MainActivityEvent.UndoScheduleEntryDeletion(
                                                subjectInstructorCrossRef,
                                                affectedSessions
                                            )
                                        )
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}