package com.leeweeder.timetable.ui.instructor

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.leeweeder.timetable.R
import com.leeweeder.timetable.ui.components.AlertDialog
import com.leeweeder.timetable.ui.components.TextField
import org.koin.androidx.compose.koinViewModel

@Composable
fun UpsertInstructorDialog(
    onDismissRequest: () -> Unit,
    viewModel: UpsertInstructorDialogViewModel = koinViewModel()
) {
    UpsertInstructorDialog(
        onDismissRequest = onDismissRequest,
        uiState = viewModel.uiState.value,
        eventFlow = viewModel.eventFlow.collectAsStateWithLifecycle().value,
        onEvent = viewModel::onEvent
    )
}

@Composable
private fun UpsertInstructorDialog(
    onDismissRequest: () -> Unit,
    uiState: UpsertInstructorDialogUiState,
    eventFlow: UpsertInstructorDialogUiEvent?,
    onEvent: (UpsertInstructorDialogEvent) -> Unit
) {
    LaunchedEffect(eventFlow) {
        when (eventFlow) {
            UpsertInstructorDialogUiEvent.DoneSavingInstructor -> {
                onDismissRequest()
            }

            null -> Unit
        }
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        onSave = {
            onEvent(UpsertInstructorDialogEvent.Save)
        },
        isSaveButtonEnabled = uiState.name.isNotBlank(),
        title = (if (true) "Add" else "Edit") + " instructor",
        iconId = R.drawable.account_box_24px,
        error = uiState.conflictError
    ) {
        val focusRequester = remember { FocusRequester() }

        TextField(
            value = uiState.name,
            onValueChange = {

                onEvent(UpsertInstructorDialogEvent.StartCheckingForError)
                onEvent(UpsertInstructorDialogEvent.EditName(it))
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
                onEvent(UpsertInstructorDialogEvent.Save)
            })
        )

        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }
    }
}

@Preview
@Composable
private fun UpsertInstructorDialogPreview() {
    UpsertInstructorDialog(onDismissRequest = {})
}
