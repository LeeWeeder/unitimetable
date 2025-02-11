package com.leeweeder.timetable.feature_widget.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.leeweeder.timetable.R
import com.leeweeder.timetable.domain.model.TimeTable
import com.leeweeder.timetable.feature_widget.UnitimetableWidgetReceiver
import com.leeweeder.timetable.ui.components.SelectionField
import com.leeweeder.timetable.ui.components.selection_and_addition_bottom_sheet.ItemTransform
import com.leeweeder.timetable.ui.components.selection_and_addition_bottom_sheet.SearchableBottomSheet
import com.leeweeder.timetable.ui.components.selection_and_addition_bottom_sheet.SearchableBottomSheetConfig
import com.leeweeder.timetable.ui.components.selection_and_addition_bottom_sheet.SearchableBottomSheetStateHolder
import com.leeweeder.timetable.ui.components.selection_and_addition_bottom_sheet.rememberSearchableBottomSheetController
import org.koin.androidx.compose.koinViewModel

@Composable
fun WidgetConfigurationScreen(
    widgetId: Int,
    onCancelClick: () -> Unit,
    onDone: () -> Unit,
    viewModel: WidgetConfigurationScreenViewModel = koinViewModel()
) {
    WidgetConfigurationScreen(
        widgetId = widgetId,
        bottomSheetState = viewModel.bottomSheetState,
        selectedTimeTable = viewModel.selectedTimeTable.value,
        isSuccess = viewModel.isSuccess.collectAsStateWithLifecycle().value,
        onEvent = viewModel::onEvent,
        onCancelClick = onCancelClick,
        onDone = onDone
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WidgetConfigurationScreen(
    widgetId: Int,
    bottomSheetState: SearchableBottomSheetStateHolder<TimeTable>,
    selectedTimeTable: TimeTable?,
    onEvent: (WidgetConfigurationScreenEvent) -> Unit,
    isSuccess: Boolean,
    onDone: () -> Unit,
    onCancelClick: () -> Unit
) {

    val context = LocalContext.current

    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            UnitimetableWidgetReceiver().glanceAppWidget.update(
                context,
                GlanceAppWidgetManager(context).getGlanceIdBy(widgetId)
            )
            onDone()
        }
    }

    val bottomSheetController = rememberSearchableBottomSheetController()

    SearchableBottomSheet(controller = bottomSheetController,
        state = bottomSheetState,
        config = SearchableBottomSheetConfig(
            searchPlaceholderTitle = "timetable",
            itemLabel = "Timetables",
            onItemClick = {
                onEvent(WidgetConfigurationScreenEvent.SelectTimeTable(it))
            },
            itemTransform = ItemTransform(
                headlineText = {
                    it.name
                }
            )
        ))

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Text("Configure widget")
                }
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp)
                    .navigationBarsPadding(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = onCancelClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = {
                        onEvent(WidgetConfigurationScreenEvent.Save(widgetId))
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Okay")
                }
            }
        }
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .padding(top = 8.dp)
        ) {
            SelectionField(
                R.drawable.table_24px_outlined,
                label = "Timetable",
                value = selectedTimeTable?.name,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                bottomSheetController.show()
            }
        }
    }
}

@Preview
@Composable
private fun UnitimetableWidgetPreview() {
    WidgetConfigurationScreen(
        widgetId = 0,
        bottomSheetState = SearchableBottomSheetStateHolder(),
        selectedTimeTable = null,
        onEvent = {}, onDone = {}, onCancelClick = {}, isSuccess = false
    )
}