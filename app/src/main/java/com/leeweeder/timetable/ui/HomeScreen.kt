package com.leeweeder.timetable.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Badge
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberDrawerState
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
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.leeweeder.timetable.R
import com.leeweeder.timetable.data.source.session.SessionType
import com.leeweeder.timetable.data.source.timetable.TimeTable
import com.leeweeder.timetable.ui.components.IconButton
import com.leeweeder.timetable.ui.timetable_setup.DefaultTimeTable
import com.leeweeder.timetable.ui.util.Constants
import com.leeweeder.timetable.ui.util.plusOneHour
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.time.LocalTime
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun HomeScreen(viewModel: HomeViewModel = koinViewModel()) {
    val dataState by viewModel.dataState.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeScreen(dataState = dataState, uiState = uiState)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreen(dataState: HomeDataState, uiState: HomeUiState) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerContent = {
            var mainTimeTable by remember { mutableStateOf(DefaultTimeTable) }

            LaunchedEffect(dataState::class) {
                if (dataState is HomeDataState.Success) {
                    mainTimeTable = dataState.mainTimeTable
                }
            }

            TimeTableNavigationDrawer(
                drawerState = drawerState,
                timeTables = uiState.timeTables,
                selectedTimeTable = uiState.selectedTimeTable,
                mainTimeTable = mainTimeTable,
                onTimeTableClick = { timeTable ->
                    // TODO: Implement changing timetables
                }
            )
        }, drawerState = drawerState
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(text = uiState.selectedTimeTable.name)
                    },
                    actions = {
                        IconButton(R.drawable.add_24px, contentDescription = "Add new schedule") {
                            // TODO: Implement adding of schedule
                        }
                        IconButton(R.drawable.tune_24px, contentDescription = "UI settings") { }
                        IconButton(
                            R.drawable.more_vert_24px,
                            contentDescription = "Open more options menu"
                        ) { }
                    },
                    navigationIcon = {
                        IconButton(R.drawable.menu_24px, "Open navigation menu") {
                            scope.launch {
                                drawerState.open()
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                )
            }
        ) { it ->
            Column(modifier = Modifier.padding(it)) {
                val leaderColumnWidth = 56.dp

                val rowHeight = 72.dp

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
                            val backgroundColor = if (dayOfWeek == uiState.dayOfWeekNow) {
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
                                        TextStyle.SHORT_STANDALONE,
                                        Locale.getDefault()
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
                            startTimes.forEachIndexed { index, period ->
                                CellBorder(borderDirection = CellBorderDirection.Horizontal)
                                Row(
                                    modifier = Modifier
                                        .height(rowHeight),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val style = MaterialTheme.typography.labelSmall

                                    @Composable
                                    fun TimeText(time: LocalTime) {
                                        Text(
                                            time.format(Constants.TimeFormatter),
                                            style = style
                                        )
                                    }

                                    Column(
                                        modifier = Modifier.weight(1f),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        TimeText(period)
                                        Text(
                                            "-",
                                            style = style.copy(lineHeight = style.lineHeight * 0.01f)
                                        )
                                        TimeText(period.plusOneHour())
                                    }
                                    CellBorder(borderDirection = CellBorderDirection.Vertical)
                                }
                            }
                        }
                    }

                    if (dataState is HomeDataState.Success) {

                        dataState.dayScheduleMap.forEach { (_, schedules) ->
                            Column(modifier = Modifier.weight(1f)) {
                                schedules.forEach { schedule ->
                                    Column(
                                        modifier = Modifier
                                            .height(rowHeight * schedule.periodSpan),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Row(modifier = Modifier.weight(1f)) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                when (schedule.type) {
                                                    SessionType.Subject -> {
                                                        Text("Subject")
                                                    }

                                                    SessionType.Vacant -> {
                                                        Text("Vacant")
                                                    }

                                                    SessionType.Break -> {
                                                        Text(schedule.breakDescription ?: "Break")
                                                    }

                                                    SessionType.Empty -> return@Column
                                                }
                                            }
                                            if (schedule.type == SessionType.Subject) {
                                                CellBorder(CellBorderDirection.Vertical)
                                            }
                                        }
                                        CellBorder(CellBorderDirection.Horizontal)
                                    }
                                }
                            }
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
    timeTables: List<TimeTable>,
    selectedTimeTable: TimeTable,
    mainTimeTable: TimeTable,
    onTimeTableClick: (TimeTable) -> Unit
) {
    ModalDrawerSheet(
        drawerState = drawerState,
        drawerShape = RectangleShape
    ) {
        Column(
            modifier = Modifier.padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Time tables",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 16.dp, horizontal = 24.dp)
            )
            HorizontalDivider(thickness = Dp.Hairline)
            LazyColumn(modifier = Modifier.padding(horizontal = 8.dp)) {
                items(timeTables) { timeTable ->
                    val selected = timeTable == selectedTimeTable

                    NavigationDrawerItem(label = {
                        Text(timeTable.name)
                    }, selected = selected, icon = {
                        Icon(
                            painter = painterResource(if (selected) R.drawable.table_24px else R.drawable.table_24px_outlined),
                            contentDescription = null
                        )
                    }, onClick = {
                        onTimeTableClick(timeTable)
                    }, badge = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .offset(x = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            var isMainTable by remember { mutableStateOf(false) }

                            isMainTable = mainTimeTable == timeTable
                            if (isMainTable) {
                                Badge(containerColor = MaterialTheme.colorScheme.primary) {
                                    Text("Main")
                                }
                            }

                            Box {
                                var expanded by remember { mutableStateOf(false) }

                                IconToggleButton(
                                    modifier = Modifier
                                        .size(36.dp),
                                    checked = expanded,
                                    onCheckedChange = {
                                        expanded = it
                                    },
                                    colors = IconButtonDefaults.iconToggleButtonColors(
                                        checkedContainerColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                            0.1f
                                        )
                                    )
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.more_vert_24px),
                                        contentDescription = "More options",
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = {
                                        expanded = false
                                    }
                                ) {
                                    DropdownMenuItem(
                                        text = {
                                            Text("Set as main timetable")
                                        },
                                        enabled = !isMainTable,
                                        onClick = {
                                            // TODO: Implement onEvent for setting mainTableId data store
                                        },
                                        trailingIcon = {
                                            if (isMainTable) {
                                                Icon(
                                                    painter = painterResource(R.drawable.done_24px),
                                                    contentDescription = null
                                                )
                                            }
                                        }
                                    )
                                    HorizontalDivider(thickness = Dp.Hairline)
                                    DropdownMenuItem(text = {
                                        Text("Rename")
                                    }, onClick = {
                                        // TODO: Implement onEvent in renaming current timetable
                                    }, leadingIcon = {
                                        Icon(
                                            painter = painterResource(R.drawable.text_format_24px),
                                            contentDescription = null
                                        )
                                    })
                                    DropdownMenuItem(text = {
                                        Text("Edit layout")
                                    }, onClick = {
                                        // TODO: Implement onEvent in editing the layout of current timetable
                                    }, leadingIcon = {
                                        Icon(
                                            painter = painterResource(R.drawable.table_edit_24px),
                                            contentDescription = null
                                        )
                                    })
                                    DropdownMenuItem(text = {
                                        Text("Delete")
                                    }, onClick = {
                                        // TODO: Implement onEvent in deleting current timetable
                                    }, leadingIcon = {
                                        Icon(
                                            painter = painterResource(R.drawable.delete_24px),
                                            contentDescription = null
                                        )
                                    })
                                }
                            }
                        }
                    }
                    )
                }
            }
        }
    }
}

@Composable
private fun LabelContainer(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Surface(
        shadowElevation = 1.dp,
        tonalElevation = 1.dp,
        modifier = modifier
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

enum class CellBorderDirection {
    Horizontal,
    Vertical
}