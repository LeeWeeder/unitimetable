package com.leeweeder.timetable.ui.timetable_setup

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import com.leeweeder.timetable.R
import com.leeweeder.timetable.ui.timetable_setup.components.CancelTextButton
import org.koin.androidx.compose.koinViewModel

@Composable
fun GetTimeTableNameDialog(
    onDismissRequest: () -> Unit,
    onNavigateToTimeTableSetupDialog: (timeTableName: String, isInitialization: Boolean) -> Unit,
    isCancelButtonEnabled: Boolean,
    viewModel: GetTimeTableNameViewModel = koinViewModel()
) {
    val defaultTimeTableName by viewModel.timeTableName

    GetTimeTableNameDialog(
        defaultTimeTableName = defaultTimeTableName,
        onDismissRequest = onDismissRequest,
        isCancelButtonEnabled = isCancelButtonEnabled,
        onNavigateToTimeTableSetupDialog = {
            onNavigateToTimeTableSetupDialog(it, viewModel.isInitialization)
        }
    )
}

@Composable
fun GetTimeTableNameDialog(
    defaultTimeTableName: String,
    isCancelButtonEnabled: Boolean,
    onDismissRequest: () -> Unit,
    onNavigateToTimeTableSetupDialog: (timeTableName: String) -> Unit
) {
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
                    onDismissRequest()
                    onNavigateToTimeTableSetupDialog(timeTableName.trim())
                },
                enabled = !isError
            ) {
                Text("Next")
            }
        }, dismissButton = {
            CancelTextButton(onDismissRequest, enabled = isCancelButtonEnabled)
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