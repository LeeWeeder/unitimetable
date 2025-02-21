package com.leeweeder.unitimetable.ui.subject

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.leeweeder.unitimetable.R
import com.leeweeder.unitimetable.domain.model.Session
import com.leeweeder.unitimetable.domain.model.Subject
import com.leeweeder.unitimetable.domain.model.SubjectInstructorCrossRef
import com.leeweeder.unitimetable.ui.components.DeleteConfirmationDialog
import com.leeweeder.unitimetable.ui.components.ItemCRUDAlertDialog
import com.leeweeder.unitimetable.ui.components.TextField
import org.koin.androidx.compose.koinViewModel

@Composable
fun SubjectDialog(
    onDismissRequest: () -> Unit,
    onDeleteSuccessful: (Subject, List<Session>, List<SubjectInstructorCrossRef>) -> Unit,
    viewModel: SubjectDialogViewModel = koinViewModel()
) {
    SubjectDialog(
        uiState = viewModel.uiState.value,
        eventFlow = viewModel.eventFlow.collectAsStateWithLifecycle().value,
        onEvent = viewModel::onEvent,
        onDismissRequest = onDismissRequest,
        onDeleteSuccessful = onDeleteSuccessful
    )
}

@Composable
private fun SubjectDialog(
    uiState: SubjectDialogUiState,
    eventFlow: SubjectDialogUiEvent?,
    onEvent: (SubjectDialogEvent) -> Unit,
    onDeleteSuccessful: (Subject, List<Session>, List<SubjectInstructorCrossRef>) -> Unit,
    onDismissRequest: () -> Unit
) {

    var isDeleteConfirmationDialogVisible by remember { mutableStateOf(false) }

    LaunchedEffect(eventFlow) {
        when (eventFlow) {
            SubjectDialogUiEvent.DoneSaving -> {
                onDismissRequest()
            }

            null -> Unit
            is SubjectDialogUiEvent.DeletionSuccessful -> {
                isDeleteConfirmationDialogVisible = false
                onDismissRequest()
                onDeleteSuccessful(
                    eventFlow.subject,
                    eventFlow.sessions,
                    eventFlow.subjectInstructorCrossRefs
                )
            }
        }
    }

    val isFormValid by remember(uiState.code, uiState.description) {
        derivedStateOf {
            uiState.code.isNotBlank() && uiState.description.isNotBlank()
        }
    }

    DeleteConfirmationDialog(
        isDeleteConfirmationDialogVisible,
        onDismissRequest = {
            isDeleteConfirmationDialogVisible = false
        },
        onConfirm = {
            onEvent(SubjectDialogEvent.DeleteSubject)
        },
        title = "Delete subject?",
        description = "All schedule entries referring this subject will also be deleted.",
    )

    val isAddMode by remember { derivedStateOf { uiState.id == null } }

    ItemCRUDAlertDialog(
        onDismissRequest = onDismissRequest,
        onSave = {
            onEvent(SubjectDialogEvent.Save)
        },
        isSaveButtonEnabled = isFormValid,
        title = (if (isAddMode) "Add" else "Edit") + " subject",
        iconId = R.drawable.book_24px,
        error = uiState.conflictError,
        onDeleteClick = if (isAddMode) null else {
            {
                isDeleteConfirmationDialogVisible = true
            }
        }
    ) {
        val descriptionFocusRequester = remember { FocusRequester() }
        val codeFocusRequester = remember { FocusRequester() }

        var codeIsFromFocus by remember { mutableStateOf(false) }
        var descriptionIsFromFocus by remember { mutableStateOf(false) }

        fun clearError(code: Boolean, description: Boolean) {
            if (uiState.conflictError != null) {
                onEvent(
                    SubjectDialogEvent.ClearError(
                        code = true,
                        description = true,
                        conflict = true
                    )
                )
            } else {
                onEvent(
                    SubjectDialogEvent.ClearError(
                        code = code,
                        description = description,
                        conflict = false
                    )
                )
            }
        }

        SubjectDialogTextField(
            value = uiState.code,
            onClearError = {
                clearError(code = true, description = false)
            },
            onValueChange = {
                onEvent(SubjectDialogEvent.StartCodeErrorChecking)
                onEvent(SubjectDialogEvent.EditCode(it))
            },
            label = "Code",
            isError = uiState.isCodeError,
            errorSupportingText = if (uiState.conflictError != null) null else "Subject code can't be empty",
            maxLines = 1,
            modifier = Modifier
                .focusRequester(codeFocusRequester)
                .onFocusChanged {
                    if (it.isFocused)
                        codeIsFromFocus = true

                    if (!it.isFocused && uiState.code.isBlank() && codeIsFromFocus) {
                        onEvent(SubjectDialogEvent.ForceCodeError)
                        codeIsFromFocus = false
                    }
                },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
                keyboardType = KeyboardType.Text,
                imeAction = if (uiState.description.isBlank()) ImeAction.Next else ImeAction.Done,
                showKeyboardOnFocus = true
            ),
            keyboardActions = KeyboardActions(onNext = {
                descriptionFocusRequester.requestFocus()
            }, onDone = {
                if (!isFormValid)
                    return@KeyboardActions

                onEvent(SubjectDialogEvent.Save)
            }),
        )

        SubjectDialogTextField(
            value = uiState.description,
            onClearError = { clearError(code = false, description = true) },
            onValueChange = {
                onEvent(SubjectDialogEvent.StartDescriptionErrorChecking)
                onEvent(SubjectDialogEvent.EditDescription(it))
            },
            label = "Description",
            isError = uiState.isDescriptionError,
            errorSupportingText = if (uiState.conflictError != null) null else "Subject description can't be empty",
            maxLines = 3,
            modifier = Modifier
                .focusRequester(descriptionFocusRequester)
                .onFocusChanged {
                    if (it.isFocused)
                        descriptionIsFromFocus = true

                    if (!it.isFocused && uiState.description.isBlank() && descriptionIsFromFocus) {
                        onEvent(SubjectDialogEvent.ForceDescriptionError)
                        descriptionIsFromFocus = false
                    }
                },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                keyboardType = KeyboardType.Text,
                imeAction = if (uiState.code.isBlank()) ImeAction.Next else ImeAction.Done,
                showKeyboardOnFocus = true
            ),
            keyboardActions = KeyboardActions(onNext = {
                codeFocusRequester.requestFocus()
            }, onDone = {
                if (!isFormValid)
                    return@KeyboardActions

                onEvent(SubjectDialogEvent.Save)
            }),
        )

        LaunchedEffect(Unit) {
            if (uiState.code.isNotBlank() && uiState.description.isBlank()) {
                descriptionFocusRequester.requestFocus()
            } else {
                codeFocusRequester.requestFocus()
            }
        }
    }
}

@Composable
private fun SubjectDialogTextField(
    value: String,
    onClearError: () -> Unit,
    onValueChange: (String) -> Unit,
    label: String,
    isError: Boolean,
    errorSupportingText: String?,
    maxLines: Int,
    modifier: Modifier,
    keyboardOptions: KeyboardOptions,
    keyboardActions: KeyboardActions
) {
    TextField(
        value = value,
        onValueChange = {
            onClearError()

            onValueChange(it)
        },
        label = label,
        isError = isError,
        supportingText = {
            errorSupportingText?.let {
                AnimatedVisibility(isError) {
                    Text(errorSupportingText)
                }
            }
        },
        maxLines = maxLines,
        modifier = modifier,
        keyboardActions = keyboardActions,
        keyboardOptions = keyboardOptions
    )
}

@Preview
@Composable
private fun SubjectDialogPreview() {
    SubjectDialog(
        uiState = SubjectDialogUiState(),
        eventFlow = null,
        onEvent = {},
        onDeleteSuccessful = { _, _, _ -> }) { }
}