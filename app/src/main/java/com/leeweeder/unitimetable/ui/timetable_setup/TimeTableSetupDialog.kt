package com.leeweeder.unitimetable.ui.timetable_setup

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.leeweeder.unitimetable.R
import com.leeweeder.unitimetable.ui.components.AlertDialogActionButtons
import com.leeweeder.unitimetable.ui.components.IconButton
import com.leeweeder.unitimetable.ui.components.OkayTextButton
import com.leeweeder.unitimetable.ui.timetable_setup.components.CancelTextButton
import com.leeweeder.unitimetable.ui.timetable_setup.components.NumberOfDaysSlider
import com.leeweeder.unitimetable.ui.timetable_setup.components.TextButton
import com.leeweeder.unitimetable.ui.timetable_setup.components.TimePicker
import com.leeweeder.unitimetable.ui.timetable_setup.components.rememberTimePickerState
import com.leeweeder.unitimetable.ui.util.Constants
import com.leeweeder.unitimetable.ui.util.plusOneHour
import org.koin.androidx.compose.koinViewModel
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun TimeTableSetupDialog(
    onDismissRequest: () -> Unit,
    onNavigateToHomeScreen: (selectedTimeTableId: Int) -> Unit,
    viewModel: TimeTableSetupViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState
    val eventFlow by viewModel.eventFlow.collectAsStateWithLifecycle(null)


    TimeTableSetupDialog(
        onDismissRequest = onDismissRequest,
        onNavigateToHomeScreen = onNavigateToHomeScreen,
        uiEvent = eventFlow,
        uiState = uiState,
        onEvent = viewModel::onEvent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimeTableSetupDialog(
    onDismissRequest: () -> Unit,
    onNavigateToHomeScreen: (selectedTimeTableId: Int) -> Unit,
    uiEvent: TimeTableSetUpUiEvent?,
    uiState: TimeTableSetupUiState,
    onEvent: (TimeTableSetupEvent) -> Unit
) {
    LaunchedEffect(uiEvent) {
        when (uiEvent) {
            is TimeTableSetUpUiEvent.FinishedSaving -> {
                onNavigateToHomeScreen(uiEvent.timeTableId)
            }

            null -> {
                // Do nothing
            }
        }
    }

    var isDaySelectionDialogVisible by remember {
        mutableStateOf(false)
    }

    val dismissDaySelectionDialog = {
        isDaySelectionDialogVisible = false
    }

    AnimatedVisibility(isDaySelectionDialogVisible) {
        var selectedDayOfWeek by remember {
            mutableStateOf(uiState.timeTable.startingDay)
        }

        com.leeweeder.unitimetable.ui.components.AlertDialog(
            onDismissRequest = dismissDaySelectionDialog,
            title = {
                Text("Starting day")
            },
            actionButtons = {
                AlertDialogActionButtons(
                    onCancelClick = dismissDaySelectionDialog,
                    onOkayClick = {
                        onEvent(TimeTableSetupEvent.UpdateStartingDay(selectedDayOfWeek))
                        dismissDaySelectionDialog()
                    })
            }
        ) {
            Column(
                modifier = Modifier
                    .selectableGroup()
            ) {
                DayOfWeek.entries.forEach {
                    Row(
                        modifier = Modifier
                            .selectable(selectedDayOfWeek == it, onClick = {
                                selectedDayOfWeek = it
                            }, role = Role.RadioButton)
                            .height(64.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        RadioButton(
                            selected = selectedDayOfWeek == it,
                            onClick = null,
                            modifier = Modifier.padding(start = 36.dp)
                        )
                        Text(it.getDisplayName(TextStyle.FULL, Locale.getDefault()))
                    }
                }
            }
        }
    }

    var isNumberOfDaysSelectionDialogVisible by remember { mutableStateOf(false) }

    val dismissNumberOfDaySelectionDialog = {
        isNumberOfDaysSelectionDialogVisible = false
    }

    AnimatedVisibility(isNumberOfDaysSelectionDialogVisible) {
        var sliderValue by remember { mutableIntStateOf(uiState.timeTable.numberOfDays) }

        AlertDialog(onDismissRequest = dismissNumberOfDaySelectionDialog, confirmButton = {
            OkayTextButton(onClick = {
                onEvent(TimeTableSetupEvent.UpdateNumberOfDays(sliderValue))
                dismissNumberOfDaySelectionDialog()
            })
        }, dismissButton = {
            CancelTextButton(onClick = dismissNumberOfDaySelectionDialog)
        }, title = {
            Text("Number of days")
        }, text = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                LabelText("1")
                NumberOfDaysSlider(sliderValue, onValueChange = {
                    sliderValue = it
                }, modifier = Modifier.weight(1f))
                LabelText(DayOfWeek.entries.size.toString())
            }
        })
    }

    val timeTable = uiState.timeTable

    var isStartTimeSelectionDialogVisible by remember { mutableStateOf(false) }

    val dismissStartTimeSelectionDialog = { isStartTimeSelectionDialogVisible = false }

    val startTime = timeTable.startTime

    TimePickerDialog(
        visible = isStartTimeSelectionDialogVisible,
        onDismissRequest = dismissStartTimeSelectionDialog,
        onConfirm = {
            onEvent(TimeTableSetupEvent.UpdateTime(it, TimeTableSetupEvent.UpdateTime.Part.Start))
        },
        initialTime = startTime,
        title = "Set start time"
    )

    var isEndTimeSelectionDialogVisible by remember { mutableStateOf(false) }

    val dismissEndTimeSelectionDialog = { isEndTimeSelectionDialogVisible = false }

    val endTime = timeTable.endTime

    TimePickerDialog(
        visible = isEndTimeSelectionDialogVisible,
        onDismissRequest = dismissEndTimeSelectionDialog,
        onConfirm = {
            onEvent(TimeTableSetupEvent.UpdateTime(it, TimeTableSetupEvent.UpdateTime.Part.End))
        },
        initialTime = endTime,
        title = "Set end time"
    )

    val topAppBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(topAppBarState)

    Scaffold(
        topBar = {
            TopAppBar(navigationIcon = {
                IconButton(
                    R.drawable.close_24px,
                    contentDescription = "Close timetable setup dialog",
                    onClick = onDismissRequest
                )
            }, title = {
                Text(uiState.timeTable.name)
            }, actions = {
                TextButton("Save", onClick = {
                    onEvent(TimeTableSetupEvent.Save)
                    // TODO: Implement loading icon while saving as leading icon and disable the save button
                })
            }, scrollBehavior = scrollBehavior)
        },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        var selectedPeriodIndex by remember {
            mutableIntStateOf(-1)
        }

        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .padding(
                    bottom = WindowInsets.statusBars.asPaddingValues(LocalDensity.current)
                        .calculateBottomPadding()
                )
                .nestedScroll(scrollBehavior.nestedScrollConnection)
        ) {
            item {
                HeaderText("Layout")
            }
            item {
                OptionGroup(R.drawable.clear_day_24px) {
                    ListItemCard(
                        label = "Number of days",
                        listItemCardPosition = ListItemCardPosition.Top,
                        onClick = {
                            isNumberOfDaysSelectionDialogVisible = true
                        }
                    ) {
                        TrailingContent(uiState.timeTable.numberOfDays.toString())
                    }
                    ListItemCard(
                        label = "Start of day",
                        listItemCardPosition = ListItemCardPosition.Bottom,
                        onClick = {
                            isDaySelectionDialogVisible = true
                        }
                    ) {
                        TrailingContent(
                            uiState.timeTable.startingDay.getDisplayName(
                                TextStyle.FULL_STANDALONE,
                                Locale.getDefault()
                            )
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    ListItem(headlineContent = {
                        LabelText("Days")
                    }, trailingContent = {
                        Row {
                            uiState.days.forEachIndexed { index, dayOfWeek ->
                                Text(
                                    dayOfWeek.getDisplayName(
                                        TextStyle.SHORT_STANDALONE,
                                        Locale.getDefault()
                                    ) + if (index != uiState.timeTable.numberOfDays - 1) ", " else "",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }, colors = ListItemDefaults.colors(containerColor = Color.Transparent))
                }
            }

            item {
                OptionGroup(R.drawable.schedule_24px, modifier = Modifier.padding(top = 8.dp)) {
                    ListItemCard(
                        label = "Start time",
                        listItemCardPosition = ListItemCardPosition.Top,
                        onClick = {
                            isStartTimeSelectionDialogVisible = true
                        }
                    ) {
                        TrailingContent(
                            timeTable.startTime.format(Constants.TimeFormatter)
                        )
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    ListItemCard(
                        label = "End time",
                        listItemCardPosition = ListItemCardPosition.Bottom,
                        onClick = {
                            isEndTimeSelectionDialogVisible = true
                        }
                    ) {
                        TrailingContent(
                            timeTable.endTime.format(Constants.TimeFormatter)
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(
                    thickness = Dp.Hairline,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                HeaderText("Schedule")
            }

            itemsIndexed(uiState.periodStartTimes) { index, startTime ->
                OutlinedCard(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    onClick = {
                        selectedPeriodIndex = index
                    },
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        ListItem(
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            headlineContent = {
                                LabelText(
                                    "${startTime.format(Constants.TimeFormatter)}–${
                                        startTime.plusOneHour().format(Constants.TimeFormatter)
                                    }",
                                )
                            },
                            leadingContent = {
                                Icon(
                                    painter = painterResource(R.drawable.schedule_24px_filled),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun HeaderText(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.secondary,
        modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
    )
}

@Composable
fun LabelText(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun ListItemCard(
    label: String,
    listItemCardPosition: ListItemCardPosition,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    /** Preferably a [TrailingContent]*/
    trailingContent: @Composable () -> Unit
) {
    val defaultShape = MaterialTheme.shapes.extraLarge
    val cornerSize = CornerSize(0.dp)
    val shape = when (listItemCardPosition) {
        ListItemCardPosition.Top -> defaultShape.copy(
            bottomStart = cornerSize,
            bottomEnd = cornerSize
        )

        ListItemCardPosition.Bottom -> defaultShape.copy(
            topStart = cornerSize,
            topEnd = cornerSize
        )
    }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(12.dp)
        ),
        shape = shape,
        onClick = onClick
    ) {
        ListItem(
            headlineContent = {
                LabelText(label)
            },
            trailingContent = trailingContent,
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }
}

enum class ListItemCardPosition {
    Top,
    Bottom
}

@Composable
private fun OptionGroup(
    @DrawableRes iconId: Int,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
        ),
        modifier = modifier
            .padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .padding(bottom = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Icon(
                    painter = painterResource(iconId),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            content()
        }
    }
}


@Composable
private fun TrailingContent(text: String) {
    Row {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text,
                style = MaterialTheme.typography.titleMedium
            )
            Icon(
                painter = painterResource(R.drawable.chevron_forward_24px),
                contentDescription = null
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    /** No need to call dismiss request, onConfirm will handle dismissal automatically */
    onConfirm: (newHour: Int) -> Unit,
    initialTime: LocalTime,
    title: String
) {
    AnimatedVisibility(visible) {
        val timePickerState =
            rememberTimePickerState(
                initialHour = initialTime.hour
            )

        AlertDialog(onDismissRequest = onDismissRequest, confirmButton = {
            OkayTextButton(onClick = {
                onConfirm(timePickerState.hour)
                onDismissRequest()
            })
        }, dismissButton = {
            CancelTextButton(onClick = onDismissRequest)
        }, text = {
            TimePicker(timePickerState, modifier = Modifier.padding(horizontal = 16.dp))
        }, title = {
            Text(title)
        })
    }
}