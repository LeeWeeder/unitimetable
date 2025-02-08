package com.leeweeder.timetable.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.leeweeder.timetable.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SelectionAndAdditionBottomSheet(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    sheetState: SheetState,
    searchBarFieldState: TextFieldState,
    items: List<T>,
    /** Typically a [SelectionAndAdditionBottomSheetDefaults.ItemLabel] */
    itemLabel: @Composable () -> Unit,
    searchBarPlaceholder: String,
    itemTransform: @Composable (ColumnScope.(T, Modifier) -> Unit),
    additionButtons: @Composable ColumnScope.(String) -> Unit
) {
    val scope = rememberCoroutineScope()

    if (visible) {
        ModalBottomSheet(
            onDismissRequest = {
                scope.launch {
                    sheetState.hide()
                }.invokeOnCompletion {
                    if (!sheetState.isVisible)
                        onDismissRequest()
                }
            },
            modifier = Modifier
                .statusBarsPadding()
                .fillMaxHeight(),
            sheetState = sheetState
        ) {
            SelectionAndAdditionBottomSheetContent(
                items = items,
                searchBarPlaceholder = searchBarPlaceholder,
                transform = itemTransform,
                additionButtons = additionButtons,
                searchBarFieldState = searchBarFieldState,
                itemLabel = itemLabel,
                onFieldFocusChange = {
                    scope.launch {
                        sheetState.expand()
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun <T> ColumnScope.SelectionAndAdditionBottomSheetContent(
    items: List<T>,
    searchBarFieldState: TextFieldState,
    searchBarPlaceholder: String,
    itemLabel: @Composable () -> Unit,
    transform: @Composable ColumnScope.(T, Modifier) -> Unit,
    additionButtons: @Composable ColumnScope.(searchFieldValue: String) -> Unit,
    onFieldFocusChange: () -> Unit
) {
    SearchBarDefaults.InputField(
        state = searchBarFieldState,
        leadingIcon = {
            Icon(R.drawable.search_24px, "Search")
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .onFocusChanged {
                if (it.isFocused)
                    onFieldFocusChange()
            },
        placeholder = {
            Text(searchBarPlaceholder)
        },
        trailingIcon = {
            AnimatedVisibility(searchBarFieldState.text.isNotEmpty()) {
                IconButton(R.drawable.cancel_24px, contentDescription = "Clear input") {
                    searchBarFieldState.clearText()
                }
            }
        },
        colors = SearchBarDefaults.inputFieldColors(),
        expanded = true,
        onSearch = {},
        onExpandedChange = {}
    )
    Spacer(Modifier.height(4.dp))
    additionButtons(searchBarFieldState.text.toString())
    HorizontalDivider(thickness = Dp.Hairline)


    CompositionLocalProvider(
        LocalTextStyle provides MaterialTheme.typography.bodyMediumEmphasized,
        LocalContentColor provides MaterialTheme.colorScheme.outline
    ) {
        itemLabel()
    }

    LazyColumn {
        items(items, key = { it.hashCode() }) {
            transform(it, Modifier.animateItem())
        }
    }
}

object SelectionAndAdditionBottomSheetDefaults {
    @Composable
    fun ItemLabel(text: String) {
        Column {
            Box(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = 24.dp, bottom = 4.dp)
            ) {
                Text(text, style = LocalTextStyle.current, color = LocalContentColor.current)
            }
            HorizontalDivider(
                thickness = Dp.Hairline,
                color = MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun SelectionAndAdditionBottomSheetPreview() {
    Column {
        SelectionAndAdditionBottomSheetContent(
            items = listOf("Item1", "Item2", "Item3"),
            searchBarPlaceholder = "Find an item",
            additionButtons = {
                AnimatedContent(it.isBlank()) { isBlank ->
                    if (isBlank) {
                        CreateFromSearchOrScratchButton("Create item from scratch", onClick = {})
                    } else {
                        CreateFromSearchOrScratchButton(
                            "Create item \"$it\"",
                            onClick = {})
                    }
                }
            },
            transform = { item, modifier ->
                Text(item)
            },
            searchBarFieldState = rememberTextFieldState(),
            itemLabel = {
                SelectionAndAdditionBottomSheetDefaults.ItemLabel("My items")
            },
            onFieldFocusChange = {}
        )
    }
}

@Composable
fun CreateFromSearchOrScratchButton(text: String, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(text) },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        leadingContent = {
            Icon(R.drawable.add_24px, contentDescription = null)
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}