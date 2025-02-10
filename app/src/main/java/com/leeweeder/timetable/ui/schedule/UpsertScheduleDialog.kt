package com.leeweeder.timetable.ui.schedule

import android.annotation.SuppressLint
import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.leeweeder.timetable.R
import com.leeweeder.timetable.domain.model.Instructor
import com.leeweeder.timetable.domain.model.Subject
import com.leeweeder.timetable.feature_color_picker.ColorPickerDialog
import com.leeweeder.timetable.ui.components.Icon
import com.leeweeder.timetable.ui.components.IconButton
import com.leeweeder.timetable.ui.components.selection_and_addition_bottom_sheet.CreateButtonConfig
import com.leeweeder.timetable.ui.components.selection_and_addition_bottom_sheet.CreateButtonProperties
import com.leeweeder.timetable.ui.components.selection_and_addition_bottom_sheet.ItemTransform
import com.leeweeder.timetable.ui.components.selection_and_addition_bottom_sheet.SelectionAndAdditionBottomSheet
import com.leeweeder.timetable.ui.components.selection_and_addition_bottom_sheet.SelectionAndAdditionBottomSheetConfig
import com.leeweeder.timetable.ui.components.selection_and_addition_bottom_sheet.SelectionAndAdditionBottomSheetStateHolder
import com.leeweeder.timetable.ui.components.selection_and_addition_bottom_sheet.rememberSelectionAndAdditionBottomSheetController
import com.leeweeder.timetable.util.Hue
import com.leeweeder.timetable.util.toColor
import org.koin.androidx.compose.koinViewModel

@Composable
fun UpsertScheduleDialog(
    onNavigateBack: () -> Unit,
    onNavigateToUpsertSubjectDialog: (Subject?) -> Unit,
    onNavigateToUpsertInstructorDialog: (Instructor?) -> Unit,
    viewModel: UpsertScheduleDialogViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState

    UpsertScheduleDialog(
        onNavigateBack = onNavigateBack,
        uiState = uiState,
        dataState = viewModel.dataState.collectAsStateWithLifecycle().value,
        eventFlow = viewModel.eventFlow.collectAsStateWithLifecycle().value,
        subjectBottomSheetState = viewModel.subjectBottomSheetState,
        instructorBottomSheetState = viewModel.instructorBottomSheetState,
        onNavigateToUpsertSubjectDialog = onNavigateToUpsertSubjectDialog,
        onNavigateToUpsertInstructorDialog = onNavigateToUpsertInstructorDialog,
        onEvent = viewModel::onEvent
    )
}

@SuppressLint("RestrictedApi")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UpsertScheduleDialog(
    onNavigateBack: () -> Unit,
    uiState: UpsertScheduleDialogUiState,
    dataState: UpsertScheduleDialogDataState,
    eventFlow: UpsertScheduleDialogUiEvent?,
    subjectBottomSheetState: SelectionAndAdditionBottomSheetStateHolder<Subject>,
    instructorBottomSheetState: SelectionAndAdditionBottomSheetStateHolder<Instructor>,
    onNavigateToUpsertSubjectDialog: (Subject?) -> Unit,
    onNavigateToUpsertInstructorDialog: (Instructor?) -> Unit,
    onEvent: (UpsertScheduleDialogEvent) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(eventFlow) {
        when (eventFlow) {
            UpsertScheduleDialogUiEvent.DoneSaving -> {
                onNavigateBack()
            }

            is UpsertScheduleDialogUiEvent.ShowSnackbar -> {
                snackbarHostState.showSnackbar(message = eventFlow.message)
            }

            null -> Unit
        }

        onEvent(UpsertScheduleDialogEvent.ClearUiEvent)
    }

    val subjectBottomSheetController = rememberSelectionAndAdditionBottomSheetController()

    SelectionAndAdditionBottomSheet(
        controller = subjectBottomSheetController,
        state = subjectBottomSheetState,
        config = SelectionAndAdditionBottomSheetConfig(
            searchPlaceholderTitle = "subject",
            itemLabel = "Subjects",
            onItemClick = {
                onEvent(UpsertScheduleDialogEvent.SetSelectedSubject(it.id))
            },
            onItemEdit = {
                onNavigateToUpsertSubjectDialog(it)
            },
            actionButtonConfig = CreateButtonConfig(
                fromScratch = CreateButtonProperties.FromScratch(
                    label = "subject"
                ) {
                    onNavigateToUpsertSubjectDialog(null)
                },
                fromQuery = listOf(
                    CreateButtonProperties.FromQuery(label = "subject with code") {
                        onNavigateToUpsertSubjectDialog(Subject(description = "", code = it))
                    },
                    CreateButtonProperties.FromQuery(label = "subject with description") {
                        onNavigateToUpsertSubjectDialog(Subject(description = it, code = ""))
                    }
                )
            ),
            itemTransform = ItemTransform(
                headlineText = {
                    it.description
                },
                overlineText = {
                    it.code
                }
            )
        )
    )

    val instructorBottomSheetController = rememberSelectionAndAdditionBottomSheetController()

    var isColorPickerDialogVisible by remember { mutableStateOf(false) }

    ColorPickerDialog(
        visible = isColorPickerDialogVisible,
        onDismissRequest = {
            isColorPickerDialogVisible = false
        },
        initialHue = uiState.selectedHue
    ) {
        onEvent(UpsertScheduleDialogEvent.SetSelectedHue(it))
        isColorPickerDialogVisible = false
    }

    SelectionAndAdditionBottomSheet(
        controller = instructorBottomSheetController, state = instructorBottomSheetState,
        config = SelectionAndAdditionBottomSheetConfig(
            searchPlaceholderTitle = "instructor",
            itemLabel = "Instructors",
            onItemClick = {
                onEvent(UpsertScheduleDialogEvent.SetSelectedInstructor(it.id))
            },
            onItemEdit = {
                onNavigateToUpsertInstructorDialog(it)
            },
            actionButtonConfig = CreateButtonConfig(
                fromScratch = CreateButtonProperties.FromScratch("instructor") {
                    onNavigateToUpsertInstructorDialog(null)
                },
                fromQuery = listOf(
                    CreateButtonProperties.FromQuery("instructor") {
                        onNavigateToUpsertInstructorDialog(Instructor(id = 0, name = it))
                    }
                )
            ),
            itemTransform = ItemTransform(
                headlineText = {
                    it.name
                }
            )
        )
    )

    Dialog(
        onDismissRequest = onNavigateBack,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(topBar = {
            LargeTopAppBar(title = {
                Text((if (uiState.id == null) "Add" else "Edit") + " schedule entry")
            }, navigationIcon = {
                IconButton(
                    R.drawable.arrow_back_24px,
                    contentDescription = "Navigate back",
                    onClick = onNavigateBack
                )
            })
        }, snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }) {
            Column(
                modifier = Modifier
                    .padding(it)
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                val selectedSubject =
                    if (dataState is UpsertScheduleDialogDataState.Success) dataState.subject else null
                SelectionField(
                    label = "Subject",
                    placeholder = "Select subject",
                    value = selectedSubject?.description,
                    overLine = selectedSubject?.code,
                    iconId = R.drawable.book_24px
                ) {
                    subjectBottomSheetController.show()
                }

                val selectedInstructor =
                    if (dataState is UpsertScheduleDialogDataState.Success) dataState.instructor else null
                SelectionField(
                    label = "Instructor",
                    placeholder = "Select instructor",
                    value = selectedInstructor?.name,
                    iconId = R.drawable.account_box_24px
                ) {
                    instructorBottomSheetController.show()
                }

                val isDarkTheme = isSystemInDarkTheme()

                BaseSelectionField(
                    label = "Color",
                    icon = {
                        Canvas(modifier = Modifier.size(24.dp)) {
                            val colors = List(Hue.MAX_HUE_DEGREES) {
                                Hue(it).createScheme(isDarkTheme).primary.toColor()
                            }

                            colors.forEachIndexed { index, color ->
                                drawArc(
                                    color = color,
                                    startAngle = index.toFloat(),
                                    sweepAngle = 1f,
                                    useCenter = true,
                                    topLeft = Offset.Zero,
                                    size = size
                                )
                            }
                        }
                    },
                    headlineContent = {
                        Box(
                            modifier = Modifier
                                .background(
                                    color = uiState.selectedHue.createScheme(isDarkTheme).primary.toColor(),
                                    shape = MaterialTheme.shapes.medium
                                )
                                .fillMaxSize()
                        )
                    }
                ) {
                    isColorPickerDialogVisible = true
                }

                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        onEvent(UpsertScheduleDialogEvent.Save)
                    },
                    modifier = Modifier.align(Alignment.End),
                    enabled = selectedSubject != null && selectedInstructor != null
                ) {
                    Text("Schedule")
                }
            }
        }
    }
}

@Composable
private fun BaseSelectionField(
    icon: @Composable () -> Unit,
    label: String,
    headlineContent: @Composable () -> Unit,
    overLine: String? = null,
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
            ListItem(
                headlineContent = headlineContent,
                overlineContent = if (overLine == null) {
                    null
                } else {
                    {
                        Text(
                            overLine,
                            color = OutlinedTextFieldDefaults.colors().unfocusedTextColor,
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
                leadingContent = icon,
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
                .offset(x = 14.dp, y = -(10).dp)
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
    BaseSelectionField(
        icon = {
            Icon(iconId, null)
        },
        label = label,
        headlineContent = {
            val color = if (value == null) {
                OutlinedTextFieldDefaults.colors().unfocusedPlaceholderColor
            } else {
                OutlinedTextFieldDefaults.colors().unfocusedTextColor
            }

            Text(
                value ?: placeholder,
                color = color,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        },
        overLine = overLine, onClick = onClick
    )
}


@Preview(showSystemUi = true)
@Composable
private fun UpsertScheduleDialogPreview() {
    Box(Modifier.fillMaxSize()) {
        UpsertScheduleDialog(
            onNavigateBack = {},
            onNavigateToUpsertSubjectDialog = {},
            onNavigateToUpsertInstructorDialog = {},
            uiState = UpsertScheduleDialogUiState(),
            dataState = UpsertScheduleDialogDataState.Success(null, null),
            eventFlow = null,
            subjectBottomSheetState = SelectionAndAdditionBottomSheetStateHolder<Subject>(),
            instructorBottomSheetState = SelectionAndAdditionBottomSheetStateHolder<Instructor>(),
            onEvent = {}
        )
    }
}