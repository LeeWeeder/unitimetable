package com.leeweeder.unitimetable.ui.timetable_setup

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.leeweeder.unitimetable.R
import com.leeweeder.unitimetable.ui.timetable_setup.components.CancelTextButton
import org.koin.androidx.compose.koinViewModel

@Composable
fun TimeTableNameDialog(
    onDismissRequest: () -> Unit,
    onNavigateToTimeTableSetupDialog: (timeTableName: String, isInitialization: Boolean) -> Unit,
    isCancelButtonEnabled: Boolean,
    viewModel: TimeTableNameViewModel = koinViewModel()
) {
    val defaultTimeTableName by viewModel.timeTableName

    TimeTableNameDialog(
        defaultTimeTableName = defaultTimeTableName,
        onDismissRequest = onDismissRequest,
        isCancelButtonEnabled = isCancelButtonEnabled,
        onNavigateToTimeTableSetupDialog = {
            onNavigateToTimeTableSetupDialog(it, viewModel.isInitialization)
        },
        isRename = viewModel.isRename,
        onSaveNewName = viewModel::saveTimeTableName,
        isSuccess = viewModel.isSuccess.collectAsStateWithLifecycle().value
    )
}

@Composable
fun TimeTableNameDialog(
    defaultTimeTableName: String,
    isCancelButtonEnabled: Boolean,
    isRename: Boolean,
    onSaveNewName: (String) -> Unit,
    isSuccess: Boolean,
    onDismissRequest: () -> Unit,
    onNavigateToTimeTableSetupDialog: (timeTableName: String) -> Unit
) {
    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            onDismissRequest()
        }
    }

    var timeTableName by remember(defaultTimeTableName) { mutableStateOf(defaultTimeTableName) }

    Log.d("GetTimeTableNameDialog", "Time table name: $timeTableName")
    Log.d("GetTimeTableNameDialog", "Default time table name: $defaultTimeTableName")

    val isError by remember(defaultTimeTableName) {
        derivedStateOf { timeTableName.isBlank() }
    }

    AlertDialog(
        onDismissRequest = onDismissRequest, confirmButton = {
            Button(
                onClick = {
                    if (isRename) {
                        onSaveNewName(timeTableName.trim())
                    } else {
                        onNavigateToTimeTableSetupDialog(timeTableName.trim())
                    }
                },
                enabled = !isError
            ) {
                Text(if (isRename) "Save" else "Next")
            }
        }, dismissButton = {
            CancelTextButton(onClick = onDismissRequest, enabled = isCancelButtonEnabled)
        }, title = {
            Text("Enter time table name")
        }, text = {
            OutlinedTextField(
                value = timeTableName,
                onValueChange = { timeTableName = it },
                label = {
                    Text("Time table name")
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.table_24px_outlined),
                        contentDescription = null
                    )
                },
                isError = isError,
                supportingText = {
                    AnimatedVisibility(isError) {
                        Text("Time table name can't be empty.")
                    }
                }
            )
        }
    )
}