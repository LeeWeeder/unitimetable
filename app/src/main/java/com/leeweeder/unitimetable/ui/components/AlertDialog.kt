package com.leeweeder.unitimetable.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.leeweeder.unitimetable.R
import com.leeweeder.unitimetable.ui.timetable_setup.components.CancelTextButton
import com.leeweeder.unitimetable.ui.timetable_setup.components.TextButton
import com.leeweeder.unitimetable.ui.timetable_setup.components.TextButtonWithIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertDialog(
    onDismissRequest: () -> Unit,
    title: @Composable () -> Unit,
    icon: (@Composable () -> Unit)? = null,
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
            Column(modifier = Modifier.padding(top = 24.dp)) {
                icon?.let {
                    Box(
                        modifier = Modifier
                            .padding(bottom = 16.dp)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        it()
                    }
                }
                CompositionLocalProvider(
                    LocalTextStyle.provides(
                        MaterialTheme.typography.headlineSmall.copy(
                            color = AlertDialogDefaults.titleContentColor
                        )
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .padding(bottom = 16.dp)
                            .fillMaxWidth(),
                        contentAlignment = if (icon != null) Alignment.TopCenter else Alignment.TopStart
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
fun ItemCRUDAlertDialog(
    onDismissRequest: () -> Unit,
    onSave: () -> Unit,
    isSaveButtonEnabled: Boolean,
    title: String,
    @DrawableRes iconId: Int,
    error: String? = null,
    onDeleteClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    AlertDialog(onDismissRequest = onDismissRequest, title = {
        Text(title)
    }, actionButtons = {
        Box(modifier = Modifier.weight(1f)) {
            onDeleteClick?.let {
                OutlinedButton(
                    onClick = it,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    contentPadding = ButtonDefaults.ButtonWithIconContentPadding
                ) {
                    Icon(
                        R.drawable.delete_24px, contentDescription = null, modifier = Modifier.size(
                            ButtonDefaults.IconSize
                        )
                    )
                    Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
                    Text("Delete")
                }
            }
        }
        CancelTextButton(onClick = onDismissRequest)
        Button(onClick = onSave, enabled = isSaveButtonEnabled) {
            Text("Save")
        }
    }, icon = {
        Icon(iconId, null)
    }) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 24.dp)
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
    }
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

@Preview
@Composable
private fun ItemCRUDAlertDialogPreview() {
    ItemCRUDAlertDialog(
        onDismissRequest = {},
        onSave = {},
        isSaveButtonEnabled = true,
        title = "Test",
        iconId = R.drawable.book_24px,
        error = null,
        onDeleteClick = {}
    ) { }
}

@Composable
fun DeleteConfirmationDialog(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    title: String,
    description: String
) {
    if (visible) {
        AlertDialog(onDismissRequest = onDismissRequest, confirmButton = {
            TextButtonWithIcon(
                onClick = onConfirm,
                iconId = R.drawable.delete_24px,
                label = "Delete",
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            )
        }, dismissButton = {
            CancelTextButton(onClick = onDismissRequest)
        }, title = {
            Text(title)
        }, text = {
            Text(description)
        }, icon = {
            Icon(
                R.drawable.delete_24px,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        })
    }
}