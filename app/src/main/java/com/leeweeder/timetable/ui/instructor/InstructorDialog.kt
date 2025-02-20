package com.leeweeder.timetable.ui.instructor

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.leeweeder.timetable.R
import com.leeweeder.timetable.domain.model.Instructor
import com.leeweeder.timetable.ui.components.DeleteConfirmationDialog
import com.leeweeder.timetable.ui.components.ItemCRUDAlertDialog
import com.leeweeder.timetable.ui.components.TextField
import org.koin.androidx.compose.koinViewModel

@Composable
fun InstructorDialog(
    onDismissRequest: () -> Unit,
    onDeleteSuccessful: (Instructor, List<Int>) -> Unit,
    onDeletionError: (String) -> Unit,
    viewModel: InstructorDialogViewModel = koinViewModel()
) {
    InstructorDialog(
        onDismissRequest = onDismissRequest,
        uiState = viewModel.uiState.value,
        eventFlow = viewModel.eventFlow.collectAsStateWithLifecycle().value,
        onEvent = viewModel::onEvent,
        onDeleteSuccessful = onDeleteSuccessful,
        onDeletionError = onDeletionError
    )
}

@Composable
private fun InstructorDialog(
    onDismissRequest: () -> Unit,
    uiState: InstructorDialogUiState,
    eventFlow: InstructorDialogUiEvent?,
    onEvent: (InstructorDialogEvent) -> Unit,
    onDeleteSuccessful: (Instructor, List<Int>) -> Unit,
    onDeletionError: (String) -> Unit
) {
    LaunchedEffect(eventFlow) {
        when (eventFlow) {
            InstructorDialogUiEvent.DoneSavingInstructor -> {
                onDismissRequest()
            }

            null -> Unit
            is InstructorDialogUiEvent.Error -> {
                onDeletionError(eventFlow.message)
            }

            is InstructorDialogUiEvent.InstructorDeleted -> {
                onDeleteSuccessful(eventFlow.instructor, eventFlow.crossRefIds)
                onDismissRequest()
            }
        }
    }

    var isDeleteConfirmationDialogVisible by remember { mutableStateOf(false) }
    DeleteConfirmationDialog(
        isDeleteConfirmationDialogVisible,
        onDismissRequest = {
            isDeleteConfirmationDialogVisible = false
        },
        onConfirm = {
            onEvent(InstructorDialogEvent.DeleteInstructor)
        },
        title = "Delete instructor?",
        description = "All schedule entries' instructors referring this instructor will be set to none."
    )

    ItemCRUDAlertDialog(
        onDismissRequest = onDismissRequest,
        onSave = {
            onEvent(InstructorDialogEvent.Save)
        },
        isSaveButtonEnabled = uiState.name.isNotBlank(),
        title = (if (true) "Add" else "Edit") + " instructor",
        iconId = R.drawable.account_box_24px,
        error = uiState.conflictError,
        onDeleteClick = if (uiState.id != null) {
            {
                isDeleteConfirmationDialogVisible = true
            }
        } else null
    ) {
        val focusRequester = remember { FocusRequester() }

        TextField(
            value = uiState.name,
            onValueChange = {

                onEvent(InstructorDialogEvent.StartCheckingForError)
                onEvent(InstructorDialogEvent.EditName(it))
            },
            label = "Name",
            isError = uiState.isError,
            maxLines = 1,
            modifier = Modifier.focusRequester(focusRequester),
            supportingText = {
                AnimatedContent(uiState.isError) { isError ->
                    Text(
                        if (isError) {
                            if (uiState.conflictError == null) "Instructor name can't be empty" else ""
                        } else {
                            "Use consistent and short format for instructor names, e.g. JD Cruz or Cruz, JD, etc."
                        }
                    )
                }

            },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done,
                showKeyboardOnFocus = true
            ),
            keyboardActions = KeyboardActions(onDone = {
                onEvent(InstructorDialogEvent.Save)
            })
        )

        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }
    }
}

@Preview
@Composable
private fun InstructorDialogPreview() {
    InstructorDialog(
        onDismissRequest = {},
        uiState = InstructorDialogUiState(),
        eventFlow = null,
        onEvent = { },
        onDeleteSuccessful = { _, _ -> },
        onDeletionError = { }
    )
}
