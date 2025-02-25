package com.leeweeder.unitimetable.feature_widget.ui

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.leeweeder.unitimetable.R
import com.leeweeder.unitimetable.domain.model.Timetable
import com.leeweeder.unitimetable.ui.components.SelectionField
import com.leeweeder.unitimetable.ui.components.searchable_bottom_sheet.ItemTransform
import com.leeweeder.unitimetable.ui.components.searchable_bottom_sheet.SearchableBottomSheet
import com.leeweeder.unitimetable.ui.components.searchable_bottom_sheet.SearchableBottomSheetConfig
import com.leeweeder.unitimetable.ui.components.searchable_bottom_sheet.SearchableBottomSheetStateHolder
import com.leeweeder.unitimetable.ui.components.searchable_bottom_sheet.rememberSearchableBottomSheetController
import org.koin.androidx.compose.koinViewModel

@Composable
fun WidgetConfigurationScreen(
    onCancelClick: () -> Unit,
    onDone: (Int) -> Unit,
    viewModel: WidgetConfigurationScreenViewModel = koinViewModel()
) {
    WidgetConfigurationScreen(
        bottomSheetState = viewModel.bottomSheetState,
        selectedTimetable = viewModel.selectedTimetable.value,
        id = viewModel.timetableId.collectAsStateWithLifecycle().value,
        onEvent = viewModel::onEvent,
        onCancelClick = onCancelClick,
        onDone = onDone
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WidgetConfigurationScreen(
    bottomSheetState: SearchableBottomSheetStateHolder<Timetable>,
    selectedTimetable: Timetable?,
    onEvent: (WidgetConfigurationScreenEvent) -> Unit,
    id: Int?,
    onDone: (Int) -> Unit,
    onCancelClick: () -> Unit
) {
    LaunchedEffect(id) {
        if (id != null) {
            onDone(id)
        } else Unit
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
                        onEvent(WidgetConfigurationScreenEvent.Save)
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
                value = selectedTimetable?.name,
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
        bottomSheetState = SearchableBottomSheetStateHolder(),
        selectedTimetable = null,
        onEvent = {}, onDone = {}, onCancelClick = {}, id = null
    )
}