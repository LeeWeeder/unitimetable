package com.leeweeder.timetable.ui.schedule

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.leeweeder.timetable.R
import com.leeweeder.timetable.domain.model.Subject
import com.leeweeder.timetable.ui.components.CreateFromSearchOrScratchButton
import com.leeweeder.timetable.ui.components.Icon
import com.leeweeder.timetable.ui.components.IconButton
import com.leeweeder.timetable.ui.components.SelectionAndAdditionBottomSheet
import com.leeweeder.timetable.ui.components.SelectionAndAdditionBottomSheetDefaults
import com.leeweeder.timetable.ui.timetable_setup.components.TextButton
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun UpsertScheduleDialog(
    onNavigateBack: () -> Unit,
    viewModel: UpsertScheduleDialogViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState

    val subjectOptions = viewModel.subjectOptions
    val subjectSearchFieldState = viewModel.subjectSearchFieldState

    UpsertScheduleDialog(
        mode = uiState.mode,
        onNavigateBack = onNavigateBack,
        uiState = uiState,
        subjectBottomSheetOptions = subjectOptions,
        subjectSearchFieldState = subjectSearchFieldState,
        runSubjectSearch = viewModel::runSubjectSearch
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpsertScheduleDialog(
    mode: UpsertScheduleDialogMode,
    onNavigateBack: () -> Unit,
    uiState: UpsertScheduleDialogUiState,
    subjectBottomSheetOptions: List<Subject>,
    subjectSearchFieldState: TextFieldState,
    runSubjectSearch: suspend () -> Unit
) {

    var isSubjectBottomSheetVisible by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState()

    LaunchedEffect(isSubjectBottomSheetVisible) {
        if (isSubjectBottomSheetVisible) {
            runSubjectSearch
        }
    }

    var isSubjectSearchComplete by remember { mutableStateOf(false) }

    LaunchedEffect(subjectBottomSheetOptions) {
        isSubjectSearchComplete = true
    }

    val scope = rememberCoroutineScope()

    if (isSubjectSearchComplete) {
        SelectionAndAdditionBottomSheet(
            visible = isSubjectBottomSheetVisible,
            onDismissRequest = { isSubjectBottomSheetVisible = false },
            items = subjectBottomSheetOptions,
            searchBarFieldState = subjectSearchFieldState,
            searchBarPlaceholder = "Find subject",
            itemTransform = { subject, modifier ->
                ListItem(headlineContent = {
                    Text(subject.description)
                }, overlineContent = {
                    Text(subject.code)
                }, modifier = modifier.clickable {
                    // TODO: Implement on event for clicking a subject, preferably setting the selected subject
                    scope.launch {
                        sheetState.hide()
                    }.invokeOnCompletion {
                        isSubjectBottomSheetVisible = false
                    }
                }, trailingContent = {
                    TextButton("Edit", onClick = {
                        // TODO: Implement edit subject
                    })
                }
                )
            },
            additionButtons = {
                AnimatedContent(it.isBlank()) { isBlank ->
                    if (!isBlank) {
                        CreateFromSearchOrScratchButton("Create subject with code \"$it\"") {
                            // TODO: Implement create subject with code field populated with 'it'
                        }
                        CreateFromSearchOrScratchButton("Create subject with description \"$it\"") {
                            // TODO: Implement create subject with description field populated with 'it'
                        }
                    } else {
                        CreateFromSearchOrScratchButton("Create new subject from scratch") {
                            // TODO: Implement create subject from scratch
                        }
                    }
                }
            },
            itemLabel = {
                AnimatedVisibility(subjectBottomSheetOptions.isNotEmpty()) {
                    SelectionAndAdditionBottomSheetDefaults.ItemLabel("My subjects")
                }
            },
            sheetState = sheetState
        )
    }

    Dialog(
        onDismissRequest = onNavigateBack,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(topBar = {
            LargeTopAppBar(title = {
                Text(
                    when (mode) {
                        UpsertScheduleDialogMode.Insert -> "Add"
                        UpsertScheduleDialogMode.Update -> "Edit"
                    } + " schedule entry"
                )
            }, navigationIcon = {
                IconButton(
                    R.drawable.arrow_back_24px,
                    contentDescription = "Navigate back",
                    onClick = onNavigateBack
                )
            })
        }) {
            Column(
                modifier = Modifier
                    .padding(it)
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                val selectedSubject = uiState.selectedSubject
                SelectionField(
                    label = "Subject",
                    placeholder = "Select subject",
                    value = selectedSubject?.description,
                    overLine = selectedSubject?.code,
                    iconId = R.drawable.book_24px
                ) {
                    isSubjectBottomSheetVisible = true
                }
                SelectionField(
                    label = "Instructor",
                    placeholder = "Select instructor",
                    value = uiState.selectedInstructor?.name,
                    iconId = R.drawable.account_box_24px
                ) {
                    // TODO: Implement click
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        // TODO: Implement saving and navigate to scheduling
                    }, modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Schedule")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SelectionField(
    @DrawableRes iconId: Int,
    label: String,
    placeholder: String,
    overLine: String? = null,
    value: String?,
    onClick: () -> Unit
) {
    Box {
        val interactionSource = remember { MutableInteractionSource() }
        OutlinedCard(
            shape = MaterialTheme.shapes.large,
            colors = OutlinedTextFieldDefaults.colors().let {
                CardDefaults.outlinedCardColors(
                    containerColor = it.unfocusedContainerColor,
                    contentColor = it.unfocusedTextColor
                )
            },
            border = BorderStroke(
                width = OutlinedTextFieldDefaults.UnfocusedBorderThickness,
                color = OutlinedTextFieldDefaults.colors().unfocusedIndicatorColor
            ),
            modifier = Modifier.height(72.dp)
        ) {
            val color = if (value == null) {
                OutlinedTextFieldDefaults.colors().unfocusedPlaceholderColor
            } else {
                OutlinedTextFieldDefaults.colors().unfocusedTextColor
            }

            ListItem(
                headlineContent = {
                    Text(
                        value ?: placeholder,
                        color = color,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                overlineContent = if (overLine == null) {
                    null
                } else {
                    {
                        Text(
                            overLine,
                            color = color,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                trailingContent = {
                    androidx.compose.material3.IconButton(
                        interactionSource = interactionSource,
                        onClick = onClick
                    ) {
                        Icon(R.drawable.arrow_drop_down_24px, contentDescription = null)
                    }
                },
                leadingContent = {
                    Icon(iconId, null)
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                modifier = Modifier
                    .fillMaxHeight()
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
            )
        }
        Box(
            modifier = Modifier
                .offset(x = 14.dp, y = -(7).dp)
                .background(color = MaterialTheme.colorScheme.surface)
        ) {
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                color = OutlinedTextFieldDefaults.colors().unfocusedLabelColor,
                modifier = Modifier.padding(horizontal = 6.dp)
            )
        }
    }
}

enum class UpsertScheduleDialogMode {
    Insert,
    Update
}

@Preview(showSystemUi = true)
@Composable
private fun UpsertScheduleDialogPreview() {
    Box(Modifier.fillMaxSize()) {
        UpsertScheduleDialog(
            UpsertScheduleDialogMode.Insert,
            onNavigateBack = {},
            uiState = UpsertScheduleDialogUiState(),
            subjectBottomSheetOptions = emptyList<Subject>(),
            subjectSearchFieldState = TextFieldState(),
            runSubjectSearch = {}
        )
    }
}