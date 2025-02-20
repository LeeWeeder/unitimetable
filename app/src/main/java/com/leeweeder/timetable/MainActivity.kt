package com.leeweeder.timetable

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.leeweeder.timetable.ui.NavGraph
import com.leeweeder.timetable.ui.theme.AppTheme
import com.leeweeder.timetable.ui.timetable_setup.components.CancelTextButton
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

                    var undoWarningDialog by remember { mutableStateOf<UndoEvent?>(null) }

                    val eventFlow by viewModel.eventFlow.collectAsStateWithLifecycle()

                    LaunchedEffect(eventFlow) {
                        when (eventFlow) {
                            is MainActivityUiEvent.ShowSnackbar -> {
                                snackbarHostState.showSnackbar(message = (viewModel.eventFlow.value as MainActivityUiEvent.ShowSnackbar).message)
                            }

                            null -> Unit
                        }
                        viewModel.onEvent(MainActivityEvent.ClearEventFlow)
                    }

                    if (undoWarningDialog != null) {
                        AlertDialog(
                            onDismissRequest = {
                                undoWarningDialog = null
                            }, confirmButton = {
                                Button(onClick = {
                                    if (undoWarningDialog != null) {
                                        viewModel.onEvent(MainActivityEvent.Undo(undoWarningDialog!!))
                                        undoWarningDialog = null
                                    }
                                }) {
                                    Text("Undo")
                                }
                            }, dismissButton = {
                                CancelTextButton {
                                    undoWarningDialog = null
                                }
                            }, title = {
                                Text("Undo deletion?")
                            }, text = {
                                val text =
                                    if (undoWarningDialog is UndoEvent.UndoInstructorDeletion) {
                                        "Only schedule entries (with this instructor originally) with no instructor will be affected. Continue?"
                                    } else {
                                        // TODO: Implement option to override new schedule and to maintain the new schedules
                                        "New schedules will be overridden. Continue?"
                                    }
                                Text(text)
                            }
                        )
                    }

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
                                        actionLabel = "Undo",
                                        duration = SnackbarDuration.Long
                                    )

                                    if (result == SnackbarResult.ActionPerformed) {
                                        undoWarningDialog =
                                            UndoEvent.UndoScheduleEntryDeletion(
                                                subjectInstructorCrossRef,
                                                affectedSessions
                                            )
                                    }
                                }
                            },
                            onSuccessfulSubjectDeletion = { subject, affectedSessions, affectSubjectInstructorCrossRefs ->
                                scope.launch {
                                    val result = snackbarHostState.showSnackbar(
                                        message = "Subject deleted successfully. Affected sessions: ${affectedSessions.size}",
                                        actionLabel = "Undo",
                                        duration = SnackbarDuration.Long
                                    )

                                    if (result == SnackbarResult.ActionPerformed) {
                                        undoWarningDialog =
                                            UndoEvent.UndoSubjectDeletion(
                                                subject,
                                                affectedSessions,
                                                affectSubjectInstructorCrossRefs
                                            )
                                    }
                                }
                            },
                            snackbarHostState = snackbarHostState,
                            onSuccessfulInstructorDeletion = { instructor, affectedCrossRefIds ->
                                scope.launch {
                                    val result = snackbarHostState.showSnackbar(
                                        message = "Instructor deleted successfully",
                                        actionLabel = "Undo",
                                        duration = SnackbarDuration.Long
                                    )

                                    if (result == SnackbarResult.ActionPerformed) {
                                        undoWarningDialog =
                                            UndoEvent.UndoInstructorDeletion(
                                                instructor,
                                                affectedCrossRefIds
                                            )
                                    }
                                }
                            },
                            onSuccessfulTimeTableDeletion = {
                                scope.launch {
                                    val result = snackbarHostState.showSnackbar(
                                        message = "Timetable deleted successfully",
                                        actionLabel = "Undo",
                                        duration = SnackbarDuration.Long
                                    )

                                    if (result == SnackbarResult.ActionPerformed) {
                                        viewModel.onEvent(
                                            MainActivityEvent.Undo(
                                                UndoEvent.UndoTimeTableDeletion(
                                                    it
                                                )
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