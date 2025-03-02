package com.leeweeder.unitimetable.feature_widget.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MultiChoiceSegmentedButtonRow
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.leeweeder.unitimetable.R
import com.leeweeder.unitimetable.domain.model.Timetable
import com.leeweeder.unitimetable.feature_widget.model.DisplayOption
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
    onDone: (Int, Set<DisplayOption>) -> Unit,
    initialTimetableId: Int?,
    initialDisplayOptions: Set<DisplayOption>,
    viewModel: WidgetConfigurationScreenViewModel = koinViewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.selectTimetable(initialTimetableId)
        viewModel.setDisplayOptions(initialDisplayOptions)
    }

    WidgetConfigurationScreen(
        bottomSheetState = viewModel.bottomSheetState,
        onCancelClick = onCancelClick,
        onDone = onDone,
        onSelectTimetable = viewModel::selectTimetable,
        selectedTimetable = viewModel.selectedTimetable.value,
        displayOptions = viewModel.displayOptions.value,
        onToggleDisplayOption = viewModel::toggleDisplayOption
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun WidgetConfigurationScreen(
    bottomSheetState: SearchableBottomSheetStateHolder<Timetable>,
    selectedTimetable: Timetable?,
    displayOptions: Set<DisplayOption>,
    onDone: (Int, Set<DisplayOption>) -> Unit,
    onCancelClick: () -> Unit,
    onSelectTimetable: (Int) -> Unit,
    onToggleDisplayOption: (DisplayOption) -> Unit
) {
    val bottomSheetController = rememberSearchableBottomSheetController()

    SearchableBottomSheet(
        controller = bottomSheetController,
        state = bottomSheetState,
        config = SearchableBottomSheetConfig(
            searchPlaceholderTitle = "timetable",
            itemLabel = "Timetables",
            onItemClick = {
                onSelectTimetable(it.id)
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
                        selectedTimetable?.let { timetable ->
                            onDone(timetable.id, displayOptions)
                        }
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

            Text(
                "Information to display",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp, bottom = 8.dp)
            )

            MultiChoiceSegmentedButtonRow(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
            ) {
                DisplayOption.entries.forEachIndexed { index, option ->
                    SegmentedButton(
                        checked = displayOptions.contains(option),
                        onCheckedChange = { onToggleDisplayOption(option) },
                        shape = SegmentedButtonDefaults.itemShape(
                            index,
                            DisplayOption.entries.size
                        ),
                        enabled = !displayOptions.contains(option) || displayOptions.size > 1
                    ) {
                        Text(text = option.label)
                    }
                }
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
        displayOptions = DisplayOption.DEFAULT,
        onCancelClick = {},
        onDone = { _, _ -> },
        onSelectTimetable = {},
        onToggleDisplayOption = {}
    )
}