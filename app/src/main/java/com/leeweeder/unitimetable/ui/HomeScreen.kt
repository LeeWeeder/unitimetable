package com.leeweeder.unitimetable.ui

import android.annotation.SuppressLint
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Badge
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.RichTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberTooltipState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.leeweeder.unitimetable.NonExistingMainTimeTableId
import com.leeweeder.unitimetable.R
import com.leeweeder.unitimetable.domain.model.Instructor
import com.leeweeder.unitimetable.domain.model.Session
import com.leeweeder.unitimetable.domain.model.Subject
import com.leeweeder.unitimetable.domain.model.Timetable
import com.leeweeder.unitimetable.domain.relation.SessionWithDetails
import com.leeweeder.unitimetable.domain.relation.SubjectInstructorCrossRefWithDetails
import com.leeweeder.unitimetable.domain.relation.TimetableWithSession
import com.leeweeder.unitimetable.ui.components.DeleteConfirmationDialog
import com.leeweeder.unitimetable.ui.components.IconButton
import com.leeweeder.unitimetable.ui.components.searchable_bottom_sheet.CreateButtonConfig
import com.leeweeder.unitimetable.ui.components.searchable_bottom_sheet.CreateButtonProperties
import com.leeweeder.unitimetable.ui.components.searchable_bottom_sheet.ItemTransform
import com.leeweeder.unitimetable.ui.components.searchable_bottom_sheet.SearchableBottomSheet
import com.leeweeder.unitimetable.ui.components.searchable_bottom_sheet.SearchableBottomSheetConfig
import com.leeweeder.unitimetable.ui.components.searchable_bottom_sheet.SearchableBottomSheetStateHolder
import com.leeweeder.unitimetable.ui.components.searchable_bottom_sheet.rememberSearchableBottomSheetController
import com.leeweeder.unitimetable.ui.timetable_setup.LabelText
import com.leeweeder.unitimetable.ui.timetable_setup.components.TextButton
import com.leeweeder.unitimetable.ui.util.Constants
import com.leeweeder.unitimetable.ui.util.plusOneHour
import com.leeweeder.unitimetable.util.TimetableIdAndName
import com.leeweeder.unitimetable.util.randomHue
import com.leeweeder.unitimetable.util.toColor
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.TextStyle
import java.util.Locale
import kotlin.collections.component1
import kotlin.collections.component2

@Composable
fun HomeScreen(
    selectedTimeTableId: Int,
    onNavigateToTimeTableNameDialog: (isInitialization: Boolean, selectedTimeTableId: Int, timetable: TimetableIdAndName?) -> Unit,
    onNavigateToScheduleEntryDialog: (Int?, Int) -> Unit,
    onNavigateToEditTimetableLayoutScreen: (Timetable) -> Unit,
    onDeleteTimetableSuccessful: (TimetableWithSession) -> Unit,
    viewModel: HomeViewModel = koinViewModel()
) {
    val dataState by viewModel.homeDataState.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (dataState is HomeDataState.Error) {
        Log.e("HomeScreen", "DataState error", (dataState as HomeDataState.Error).throwable)
    } else if (dataState is HomeDataState.Loading) {
        Log.d("HomeScreen", "Loading...")
    }

    LaunchedEffect(Unit) {
        if (selectedTimeTableId == NonExistingMainTimeTableId) {
            // TODO: Implement navigating away to initialize main time table id
        }
    }

    HomeScreen(
        dataState = dataState,
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onNavigateToNewTimeTableNameDialog = {
            onNavigateToTimeTableNameDialog(false, uiState.selectedTimetable.id, null)
        },
        onNavigateToTimetableNameDialog = {
            onNavigateToTimeTableNameDialog(
                false, uiState.selectedTimetable.id, TimetableIdAndName(
                    id = it.id,
                    name = it.name
                )
            )
        },
        onNavigateToScheduleEntryDialog = onNavigateToScheduleEntryDialog,
        scheduleEntryBottomSheetState = viewModel.scheduleEntryBottomSheetState,
        onNavigateToEditTimetableLayoutScreen = onNavigateToEditTimetableLayoutScreen,
        eventFlow = viewModel.eventFlow.collectAsStateWithLifecycle().value,
        onDeleteTimetableSuccessful = onDeleteTimetableSuccessful
    )
}

@SuppressLint("RestrictedApi")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun HomeScreen(
    dataState: HomeDataState,
    uiState: HomeUiState,
    onNavigateToNewTimeTableNameDialog: () -> Unit,
    onNavigateToTimetableNameDialog: (Timetable) -> Unit,
    onNavigateToScheduleEntryDialog: (subjectInstructorId: Int?, selectedTimeTableId: Int) -> Unit,
    onNavigateToEditTimetableLayoutScreen: (Timetable) -> Unit,
    onEvent: (HomeEvent) -> Unit,
    eventFlow: HomeUiEvent?,
    scheduleEntryBottomSheetState: SearchableBottomSheetStateHolder<SubjectInstructorCrossRefWithDetails>,
    onDeleteTimetableSuccessful: (TimetableWithSession) -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val snackBarHostState = remember { SnackbarHostState() }

    val newScheduleEntryController = rememberSearchableBottomSheetController()

    var isTimetableDeleteConfirmationDialogVisible by remember { mutableStateOf(false) }
    var toBeDeletedTimetableId by remember { mutableIntStateOf(0) }

    LaunchedEffect(eventFlow) {
        when (eventFlow) {
            is HomeUiEvent.SuccessTimetableDeletion -> {
                isTimetableDeleteConfirmationDialogVisible = false
                onDeleteTimetableSuccessful(eventFlow.deletedTimetableWithDetails)
            }

            null -> Unit
        }

        onEvent(HomeEvent.ClearUiEvent)
    }

    DeleteConfirmationDialog(
        visible = isTimetableDeleteConfirmationDialogVisible,
        onDismissRequest = { isTimetableDeleteConfirmationDialogVisible = false },
        onConfirm = {
            onEvent(HomeEvent.DeleteTimeTable(toBeDeletedTimetableId))
        },
        title = "Delete timetable?",
        description = "Schedule entries, subjects, and instructors added won't be affected. Continue?"
    )

    SearchableBottomSheet(
        controller = newScheduleEntryController,
        state = scheduleEntryBottomSheetState,
        config = SearchableBottomSheetConfig(
            searchPlaceholderTitle = "schedule entry",
            itemLabel = "Schedule entries",
            onItemClick = { onEvent(HomeEvent.SetToEditMode(it.id)) },
            onItemEdit = { onNavigateToScheduleEntryDialog(it.id, uiState.selectedTimetable.id) },
            actionButtonConfig = CreateButtonConfig(
                fromScratch = CreateButtonProperties.FromScratch(label = "schedule") {
                    onNavigateToScheduleEntryDialog(null, uiState.selectedTimetable.id)
                }
            ),
            itemTransform = ItemTransform(
                headlineText = {
                    it.subject.description
                },
                overlineText = {
                    it.subject.code
                },
                supportingText = {
                    it.instructor?.name ?: ""
                },
                trailingContent = {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(
                                color = it.hue.createScheme(isSystemInDarkTheme()).primary.toColor(),
                                shape = CircleShape
                            )
                    )
                }
            ),
        )
    )

    fun closeDrawer() {
        scope.launch {
            drawerState.close()
        }
    }

    ModalNavigationDrawer(
        drawerContent = {
            TimeTableNavigationDrawer(
                drawerState = drawerState,
                selectedTimetable = uiState.selectedTimetable,
                onTimeTableClick = { timeTableId ->
                    onEvent(HomeEvent.SelectTimeTable(timeTableId))
                    closeDrawer()
                },
                dataState = dataState,
                onRenameTimeTable = onNavigateToTimetableNameDialog,
                onDeleteMenuClick = {
                    isTimetableDeleteConfirmationDialogVisible = true
                    toBeDeletedTimetableId = it
                },
                onEditLayoutClick = onNavigateToEditTimetableLayoutScreen
            )
        }, drawerState = drawerState
    ) {
        Scaffold(snackbarHost = {
            SnackbarHost(hostState = snackBarHostState)
        }, topBar = {
            TopAppBar(title = uiState.selectedTimetable.name,
                topAppBarMode = if (uiState.isOnEditMode) {
                    TopAppBarMode.EditMode(onDoneClick = {
                        onEvent(HomeEvent.SetToDefaultMode)
                    })
                } else {
                    TopAppBarMode.Default(
                        onAddNewScheduleClick = {
                            newScheduleEntryController.show()
                        },
                        onNewTimeTableClick = onNavigateToNewTimeTableNameDialog
                    )
                },
                onNavigationMenuClick = {
                    scope.launch {
                        drawerState.open()
                    }
                })
        }) { it ->
            Column(modifier = Modifier.padding(it)) {
                val leaderColumnWidth = 56.dp

                // Header
                LabelContainer(
                    modifier = Modifier.height(32.dp)
                ) {
                    Row {
                        Row {
                            Box(
                                modifier = Modifier.width(leaderColumnWidth)
                            )
                            CellBorder(CellBorderDirection.Vertical)
                        }

                        uiState.days.forEach { dayOfWeek ->
                            val backgroundColor = if (dayOfWeek == LocalDate.now().dayOfWeek) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            } else {
                                Color.Transparent
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .background(color = backgroundColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    dayOfWeek.getDisplayName(
                                        TextStyle.SHORT_STANDALONE, Locale.getDefault()
                                    ),
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Normal)
                                )
                            }
                        }
                    }
                }

                // Body
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 24.dp)
                ) {
                    // Leader
                    LabelContainer(modifier = Modifier.width(leaderColumnWidth)) {
                        Column(modifier = Modifier.weight(1f)) {
                            val startTimes = uiState.startTimes
                            CellBorder(borderDirection = CellBorderDirection.Horizontal)
                            startTimes.forEachIndexed { index, period ->
                                Row(
                                    modifier = Modifier.height(RowHeight),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val style = MaterialTheme.typography.bodySmallEmphasized

                                    @Composable
                                    fun TimeText(time: LocalTime) {
                                        Text(
                                            time.format(Constants.TimeFormatter), style = style
                                        )
                                    }

                                    Column(
                                        modifier = Modifier.weight(1f),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        TimeText(period)
                                        Box(
                                            modifier = Modifier.size(width = 4.dp, height = 3.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(1.dp)
                                                    .background(color = MaterialTheme.colorScheme.onSurface)
                                            )
                                        }
                                        TimeText(period.plusOneHour())
                                    }
                                    CellBorder(borderDirection = CellBorderDirection.Vertical)
                                }
                                CellBorder(borderDirection = CellBorderDirection.Horizontal)
                            }
                        }
                    }

                    if (dataState is HomeDataState.Success) {
                        val selectedTimeTableId = uiState.selectedTimetable.id

                        if (uiState.isOnEditMode) {
                            EditModeGrid(dataState.getSessionsWithSubjectInstructor(
                                selectedTimeTableId
                            ), onGridClick = {
                                onEvent(HomeEvent.SetSessionWithActiveSubjectInstructor(it))
                            })
                        } else {
                            DefaultModeGrid(dataState.getGroupedSchedules(
                                selectedTimeTableId,
                                uiState.days
                            ),
                                onChangeToEditMode = {
                                    onNavigateToScheduleEntryDialog(
                                        it,
                                        uiState.selectedTimetable.id
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TimeTableNavigationDrawer(
    drawerState: DrawerState,
    selectedTimetable: Timetable,
    onTimeTableClick: (Int) -> Unit,
    dataState: HomeDataState,
    onRenameTimeTable: (Timetable) -> Unit,
    onDeleteMenuClick: (Int) -> Unit,
    onEditLayoutClick: (Timetable) -> Unit
) {

    @Composable
    fun IconToggleButton(
        checked: Boolean, onCheckedChange: (Boolean) -> Unit, modifier: Modifier = Modifier
    ) {
        var iconSize by remember { mutableStateOf(Size.Zero) }
        IconToggleButton(
            modifier = Modifier
                .minimumInteractiveComponentSize()
                .then(modifier)
                .onGloballyPositioned {
                    iconSize = it.size.toSize()
                },
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = IconButtonDefaults.iconToggleButtonColors(
                checkedContainerColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                    0.1f
                )
            )
        ) {
            val size = (iconSize / 2f)
            val density = LocalDensity.current
            Icon(
                painter = painterResource(R.drawable.more_vert_24px),
                contentDescription = "More options",
                modifier = Modifier.size(with(density) {
                    DpSize(size.width.toDp(), size.height.toDp())
                })
            )
        }
    }

    ModalDrawerSheet(
        drawerState = drawerState, drawerShape = RectangleShape
    ) {
        if (dataState is HomeDataState.Success) {
            Column(
                modifier = Modifier.padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Unitimetable - University Timetable",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                HorizontalDivider(thickness = Dp.Hairline)
                LazyColumn {
                    item {
                        Box(modifier = Modifier.padding(bottom = 8.dp, start = 16.dp, top = 8.dp)) {
                            LabelText("Time tables")
                        }
                    }
                    items(dataState.timetables) { timetable ->
                        val selected = timetable == selectedTimetable

                        Log.d(
                            "TimeTableNavigationDrawer",
                            "selected time table id: ${selectedTimetable.id}"
                        )

                        Box(modifier = Modifier.padding(horizontal = 8.dp)) {
                            NavigationDrawerItem(label = {
                                Text(timetable.name)
                            }, selected = selected, icon = {
                                Icon(
                                    painter = painterResource(if (selected) R.drawable.table_24px else R.drawable.table_24px_outlined),
                                    contentDescription = null
                                )
                            }, onClick = {
                                onTimeTableClick(timetable.id)
                            }, badge = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.offset(x = 12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    var isMainTable by remember { mutableStateOf(false) }

                                    isMainTable = dataState.mainTimetable == timetable
                                    if (isMainTable) {
                                        Badge(containerColor = MaterialTheme.colorScheme.primary) {
                                            Text("Main")
                                        }
                                    }

                                    Box {
                                        var expanded by remember { mutableStateOf(false) }

                                        IconToggleButton(
                                            checked = expanded, onCheckedChange = {
                                                expanded = it
                                            }, modifier = Modifier.size(36.dp)
                                        )

                                        DropdownMenu(expanded = expanded, onDismissRequest = {
                                            expanded = false
                                        }) {
                                            DropdownMenuItem(text = {
                                                Text("Set as main timetable")
                                            }, enabled = !isMainTable, onClick = {
                                                // TODO: Implement onEvent for setting mainTableId data store
                                            }, trailingIcon = {
                                                if (isMainTable) {
                                                    Icon(
                                                        painter = painterResource(R.drawable.done_24px),
                                                        contentDescription = null
                                                    )
                                                }
                                            })
                                            HorizontalDivider(
                                                thickness = Dp.Hairline,
                                                color = MaterialTheme.colorScheme.outlineVariant
                                            )
                                            DropdownMenuItem(text = {
                                                Text("Rename")
                                            }, onClick = {
                                                onRenameTimeTable(timetable)
                                            }, leadingIcon = {
                                                Icon(
                                                    painter = painterResource(R.drawable.text_format_24px),
                                                    contentDescription = null
                                                )
                                            })
                                            DropdownMenuItem(text = {
                                                Text("Edit layout")
                                            }, onClick = {
                                                onEditLayoutClick(timetable)
                                            }, leadingIcon = {
                                                Icon(
                                                    painter = painterResource(R.drawable.table_edit_24px),
                                                    contentDescription = null
                                                )
                                            })
                                            // Hide delete option if there is only one timetable
                                            if (dataState.timetables.size > 1) {
                                                DropdownMenuItem(
                                                    text = {
                                                        Text("Delete")
                                                    },
                                                    onClick = {
                                                        onDeleteMenuClick(timetable.id)
                                                    },
                                                    leadingIcon = {
                                                        Icon(
                                                            painter = painterResource(R.drawable.delete_24px),
                                                            contentDescription = null
                                                        )
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            })
                        }
                    }
                }
            }
        }
    }
}

sealed interface TopAppBarMode {
    data class Default(
        val onAddNewScheduleClick: () -> Unit, val onNewTimeTableClick: () -> Unit
    ) : TopAppBarMode

    data class EditMode(val onDoneClick: () -> Unit) : TopAppBarMode
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopAppBar(
    title: String, topAppBarMode: TopAppBarMode, onNavigationMenuClick: () -> Unit
) {
    TopAppBar(title = {
        Text(text = title)
    }, actions = {
        AnimatedContent(topAppBarMode) { topAppBarMode ->
            when (topAppBarMode) {
                is TopAppBarMode.Default -> {
                    Row {
                        IconButton(
                            R.drawable.add_24px,
                            contentDescription = "Add new schedule",
                            onClick = topAppBarMode.onAddNewScheduleClick
                        )
                        Box {
                            var expanded by remember { mutableStateOf(false) }

                            IconButton(R.drawable.more_vert_24px,
                                contentDescription = "Open more options menu",
                                onClick = {
                                    expanded = true
                                })
                            DropdownMenu(expanded = expanded, onDismissRequest = {
                                expanded = false
                            }) {
                                DropdownMenuItem(text = {
                                    Text("New timetable")
                                }, onClick = {
                                    topAppBarMode.onNewTimeTableClick()
                                    expanded = false
                                }, leadingIcon = {
                                    // TODO: Change icon to add timetable
                                    Icon(
                                        painter = painterResource(R.drawable.add_24px),
                                        contentDescription = null
                                    )
                                })
                            }
                        }
                    }
                }

                is TopAppBarMode.EditMode -> {
                    IconButton(
                        R.drawable.done_24px,
                        contentDescription = "Finish scheduling",
                        onClick = topAppBarMode.onDoneClick
                    )
                }
            }
        }
    }, navigationIcon = {
        IconButton(
            R.drawable.menu_24px, "Open navigation menu", onClick = onNavigationMenuClick
        )
    }, colors = TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
    )
    )
}

@Composable
private fun LabelContainer(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Surface(
        shadowElevation = 1.dp, tonalElevation = 1.dp, modifier = modifier
    ) {
        content()
    }
}

@Composable
private fun CellBorder(borderDirection: CellBorderDirection) {
    val thickness = Dp.Hairline
    val color = MaterialTheme.colorScheme.outlineVariant

    when (borderDirection) {
        CellBorderDirection.Horizontal -> {
            HorizontalDivider(thickness = thickness, color = color)
        }

        CellBorderDirection.Vertical -> {
            VerticalDivider(thickness = thickness, color = color)
        }
    }
}

@SuppressLint("RestrictedApi")
@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun RowScope.DefaultModeGrid(
    groupedSchedules: List<List<Schedule>>,
    onChangeToEditMode: (subjectInstructorId: Int) -> Unit
) {
    groupedSchedules.forEach { schedules ->
        Column(modifier = Modifier.weight(1f)) {
            schedules.forEach { schedule ->

                val subjectDescriptionMaxLine = if (schedule.periodSpan == 1) 2 else Int.MAX_VALUE
                val instructorNameMaxLine = if (schedule.periodSpan == 1) 1 else Int.MAX_VALUE

                Column(
                    modifier = Modifier.height(RowHeight * schedule.periodSpan),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (schedule.subjectInstructor != null) {
                        val scheme =
                            schedule.subjectInstructor.hue.createScheme(isSystemInDarkTheme())

                        var isTextTruncated by remember { mutableStateOf(false) }

                        val state = rememberTooltipState(isPersistent = true)

                        @Composable
                        fun Chip(
                            @DrawableRes iconId: Int,
                            iconColor: Color = AssistChipDefaults.assistChipColors().leadingIconContentColor,
                            text: String
                        ) {
                            AssistChip(onClick = { }, label = {
                                Text(
                                    text, style = MaterialTheme.typography.bodySmall
                                )
                            }, leadingIcon = {
                                Icon(
                                    painter = painterResource(iconId),
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = iconColor
                                )
                            }, modifier = Modifier.height(24.dp)
                            )
                        }

                        TooltipBox(
                            positionProvider = TooltipDefaults.rememberTooltipPositionProvider(),
                            state = state,
                            tooltip = @Composable {
                                RichTooltip(action = {
                                    TextButton("Edit", onClick = {
                                        onChangeToEditMode(schedule.subjectInstructor.id)
                                    })
                                }, title = {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Chip(
                                            iconId = R.drawable.book_24px,
                                            text = schedule.subjectInstructor.subject.code
                                        )
                                        Text(
                                            schedule.subjectInstructor.subject.description,
                                            style = MaterialTheme.typography.bodyLargeEmphasized
                                        )
                                    }
                                }) {
                                    Chip(
                                        iconId = R.drawable.account_box_24px,
                                        text = schedule.subjectInstructor.instructor?.name
                                            ?: "No Instructor",
                                        iconColor = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            val scope = rememberCoroutineScope()
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .border()
                                    .background(color = scheme.primary.toColor())
                                    .padding(4.dp)
                                    .clickable(onClick = {
                                        if (isTextTruncated) {
                                            scope.launch {
                                                state.show()
                                            }
                                        } else {
                                            onChangeToEditMode(schedule.subjectInstructor.id)
                                        }
                                    }),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // TODO: Utilize parent size to distribute position and sizing of the texts
                                Text(
                                    schedule.subjectInstructor.subject.code.uppercase(),
                                    style = MaterialTheme.typography.labelMediumEmphasized,
                                    color = scheme.onPrimary.toColor(),
                                    textAlign = TextAlign.Center
                                    // TODO: Implement auto-size for subject code
                                )

                                val bodySmall = MaterialTheme.typography.bodySmall
                                val bodySmallFontSizeValue = bodySmall.fontSize.value
                                Text(schedule.subjectInstructor.subject.description,
                                    style = bodySmall.copy(
                                        fontSize = (bodySmallFontSizeValue - 2).sp,
                                        lineHeight = (bodySmallFontSizeValue - 1).sp
                                    ),
                                    color = scheme.onPrimary.toColor(),
                                    maxLines = subjectDescriptionMaxLine,
                                    softWrap = true,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center,
                                    onTextLayout = {
                                        isTextTruncated = it.hasVisualOverflow
                                    })
                                Text(schedule.subjectInstructor.instructor?.name ?: "No instructor",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = scheme.onPrimary.toColor(),
                                    modifier = Modifier.padding(top = 4.dp),
                                    maxLines = instructorNameMaxLine,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center,
                                    onTextLayout = {
                                        isTextTruncated = it.hasVisualOverflow
                                    })
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .border(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(schedule.label ?: "")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Modifier.border(color: Color = MaterialTheme.colorScheme.surface): Modifier {
    return this.then(
        Modifier.border(
            width = Dp.Hairline, color = color
        )
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@SuppressLint("RestrictedApi")
@Composable
private fun RowScope.EditModeGrid(
    sessionsWithSubjectAndInstructor: List<SessionWithDetails>, onGridClick: (Session) -> Unit
) {
    sessionsWithSubjectAndInstructor.groupBy { it.session.dayOfWeek }
        .forEach { (_, sessionsWithSubjectAndInstructor) ->
            Column(modifier = Modifier.weight(1f)) {
                sessionsWithSubjectAndInstructor.forEach { sessionAndSubjectAndInstructor ->
                    Box(
                        modifier = Modifier
                            .height(RowHeight)
                            .fillMaxWidth()
                            .clickable(onClick = {
                                onGridClick(sessionAndSubjectAndInstructor.session)
                            })
                    ) {
                        if (sessionAndSubjectAndInstructor.session.isSubject) {
                            val scheme =
                                sessionAndSubjectAndInstructor.subjectWithInstructor!!.hue.createScheme(
                                    isSystemInDarkTheme()
                                )

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .border(
                                        width = Dp.Hairline,
                                        color = MaterialTheme.colorScheme.outlineVariant
                                    )
                                    .background(color = scheme.primary.toColor()),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    sessionAndSubjectAndInstructor.subjectWithInstructor.subject.code.uppercase(),
                                    modifier = Modifier.padding(4.dp),
                                    color = scheme.onPrimary.toColor(),
                                    style = MaterialTheme.typography.bodySmallEmphasized,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        MaterialTheme.colorScheme.surfaceColorAtElevation(
                                            8.dp
                                        )
                                    ), contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.add_24px),
                                    contentDescription = "Add schedule",
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
}

private val PreviewSessionWithSubjectWrapperAndInstructor = DayOfWeek.entries.flatMap { dayOfWeek ->
    List(5) {
        SessionWithDetails(
            session = if (it == 0 || it == 1) {
                Session.subjectSession(
                    timeTableId = 1,
                    dayOfWeek = dayOfWeek,
                    startTime = LocalTime.of(it, 0),
                    crossRefId = 0
                )
            } else {
                Session.emptySession(1, dayOfWeek, LocalTime.of(it, 0))
            }, subjectWithInstructor = SubjectInstructorCrossRefWithDetails(
                0, subject = Subject(
                    code = "Math 123",
                    description = "Mathematics literature",
                ), instructor = Instructor(name = "John Doe"), hue = randomHue()
            )
        )
    }
}

@Preview(showBackground = true, widthDp = 500)
@Composable
private fun EditModeGridPreview() {
    Row {
        EditModeGrid(sessionsWithSubjectAndInstructor = PreviewSessionWithSubjectWrapperAndInstructor,
            onGridClick = {})
    }
}

@Preview(showBackground = true, widthDp = 500)
@Composable
private fun DefaultModeGridPreview() {
    Row {
        DefaultModeGrid(groupedSchedules = PreviewSessionWithSubjectWrapperAndInstructor.toGroupedSchedules(
            emptyList()
        ),
            onChangeToEditMode = { })
    }
}


private val RowHeight = 72.dp

enum class CellBorderDirection {
    Horizontal, Vertical
}