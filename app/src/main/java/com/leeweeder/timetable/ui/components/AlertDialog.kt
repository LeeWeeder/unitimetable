package com.leeweeder.timetable.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.leeweeder.timetable.ui.timetable_setup.components.CancelTextButton
import com.leeweeder.timetable.ui.timetable_setup.components.TextButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertDialog(
    onDismissRequest: () -> Unit,
    title: @Composable () -> Unit,
    actionButtons: @Composable RowScope.() -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    BasicAlertDialog(
        onDismissRequest = onDismissRequest
    ) {
        Surface(
            color = AlertDialogDefaults.containerColor,
            tonalElevation = AlertDialogDefaults.TonalElevation,
            shape = AlertDialogDefaults.shape
        ) {
            Column {
                CompositionLocalProvider(
                    LocalTextStyle.provides(
                        MaterialTheme.typography.headlineSmall.copy(
                            color = AlertDialogDefaults.titleContentColor
                        )
                    )
                ) {
                    Box(
                        modifier = Modifier.padding(top = 24.dp, bottom = 16.dp, start = 24.dp)
                    ) {
                        title()
                    }
                }

                Column(
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    content()
                }

                AlertDialogActionButtonContainer {
                    actionButtons()
                }
            }
        }
    }
}

@Composable
fun AlertDialog(
    onDismissRequest: () -> Unit,
    onSave: () -> Unit,
    isSaveButtonEnabled: Boolean,
    title: String,
    @DrawableRes iconId: Int,
    error: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    AlertDialog(onDismissRequest = onDismissRequest, confirmButton = {
        Button(onClick = onSave, enabled = isSaveButtonEnabled) {
            Text("Save")
        }
    }, dismissButton = {
        CancelTextButton(onClick = {
            onDismissRequest()
        })
    }, title = {
        Text(title)
    }, text = {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AnimatedVisibility(
                visible = error != null
            ) {
                Text(
                    text = error ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            content()
        }
    }, icon = {
        Icon(iconId, null)
    })
}

@Composable
fun RowScope.AlertDialogActionButtons(onCancelClick: () -> Unit, onOkayClick: () -> Unit) {
    CancelTextButton(onClick = onCancelClick)
    OkayTextButton(onOkayClick)
}

@Composable
private fun AlertDialogActionButtonContainer(content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp)
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(
            8.dp, Alignment.End
        )
    ) {
        content()
    }
}

@Composable
fun OkayTextButton(onClick: () -> Unit, enabled: Boolean = true) {
    TextButton("Okay", onClick, enabled = enabled)
}