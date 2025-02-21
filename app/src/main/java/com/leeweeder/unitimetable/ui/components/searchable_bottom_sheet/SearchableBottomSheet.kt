package com.leeweeder.unitimetable.ui.components.searchable_bottom_sheet

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.input.clearText
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SheetState
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.leeweeder.unitimetable.R
import com.leeweeder.unitimetable.ui.components.Icon
import com.leeweeder.unitimetable.ui.components.IconButton
import com.leeweeder.unitimetable.ui.timetable_setup.components.TextButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * For transforming purposes. This used to have uniform view of the items.
 *
 * Might be removed if use case for custom list item view is needed.
 *
 * */
data class ItemTransform<T>(
    val headlineText: (T) -> String,
    val supportingText: ((T) -> String)? = null,
    val overlineText: ((T) -> String)? = null,
    val trailingContent: (@Composable (T) -> Unit)? = null
)

data class SearchableBottomSheetConfig<T>(
    val searchPlaceholderTitle: String,
    val itemLabel: String,
    val onItemClick: (T) -> Unit,
    val onItemEdit: ((T) -> Unit)? = null,
    val actionButtonConfig: CreateButtonConfig? = null,
    val itemTransform: ItemTransform<T>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SearchableBottomSheet(
    controller: SearchableBottomSheetController,
    state: SearchableBottomSheetStateHolder<T>,
    config: SearchableBottomSheetConfig<T>,
    snackbar: (@Composable () -> Unit)? = null
) {
    LaunchedEffect(controller.isVisible) {
        if (controller.isVisible) {
            state.runSearch()
        }
    }

    var isSearchComplete by remember { mutableStateOf(false) }

    LaunchedEffect(state.searchResults) {
        isSearchComplete = true
    }

    var snackbarBottomPadding by remember { mutableStateOf(0.dp) }

    val density = LocalDensity.current

    LaunchedEffect(controller.state.currentValue) {
        try {
            val state = controller.state
            with(density) {
                animate(
                    initialValue = snackbarBottomPadding.toPx(),
                    targetValue = state.requireOffset()
                ) { value, _ ->
                    snackbarBottomPadding = with(density) {
                        value.toDp()
                    }
                }
                snackbarBottomPadding = controller.state.requireOffset().toDp()
            }
        } catch (_: IllegalStateException) {
            // Do nothing
        }
    }

    if (controller.isVisible && isSearchComplete) {
        ModalBottomSheet(
            onDismissRequest = controller::hide,
            modifier = Modifier
                .statusBarsPadding()
                .fillMaxHeight(),
            sheetState = controller.state
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                SearchableBottomSheetContent(
                    controller = controller,
                    state = state,
                    config = config
                )
                snackbar?.let {
                    Box(
                        Modifier
                            .align(alignment = Alignment.BottomCenter)
                            .padding(bottom = snackbarBottomPadding)
                    ) {
                        it()
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun <T> SearchableBottomSheetContent(
    controller: SearchableBottomSheetController,
    state: SearchableBottomSheetStateHolder<T>,
    config: SearchableBottomSheetConfig<T>
) {
    Column {
        val searchFieldState = state.searchFieldState
        SearchBarDefaults.InputField(
            state = searchFieldState,
            leadingIcon = {
                Icon(R.drawable.search_24px, "Search")
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .onFocusChanged {
                    if (it.isFocused)
                        controller.expand()
                },
            placeholder = {
                Text("Find " + config.searchPlaceholderTitle.lowercase())
            },
            trailingIcon = {
                AnimatedVisibility(searchFieldState.text.isNotEmpty()) {
                    IconButton(R.drawable.cancel_24px, contentDescription = "Clear input") {
                        searchFieldState.clearText()
                    }
                }
            },
            colors = SearchBarDefaults.inputFieldColors(),
            expanded = true,
            onSearch = {},
            onExpandedChange = {}
        )
        Spacer(Modifier.height(4.dp))

        val actionButtonConfig = config.actionButtonConfig

        if (actionButtonConfig != null) {
            @Composable
            fun CreateFromScratchButton() {
                CreateFromSearchOrScratchButton(
                    "new ${actionButtonConfig.fromScratch.label.lowercase().trim()} from scratch",
                    onClick = actionButtonConfig.fromScratch.onClick
                )
            }

            if (actionButtonConfig.fromQuery == null || actionButtonConfig.fromQuery.isEmpty()) {
                CreateFromScratchButton()
            } else {
                AnimatedContent(searchFieldState.text.isBlank()) { isBlank ->
                    if (isBlank) {
                        CreateFromScratchButton()
                    } else {
                        Column {
                            actionButtonConfig.fromQuery.forEach { properties ->
                                CreateFromSearchOrScratchButton(
                                    "${
                                        properties.label.lowercase().trim()
                                    } \"${properties.transform(searchFieldState.text.toString())}\""
                                ) {
                                    properties.onClick(properties.transform(searchFieldState.text.toString()))
                                }
                            }
                        }
                    }
                }
            }

            HorizontalDivider(thickness = Dp.Hairline)
        }

        AnimatedVisibility(
            state.searchResults.isNotEmpty(), modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(top = 24.dp, bottom = 4.dp)
        ) {
            Text(
                config.itemLabel,
                style = MaterialTheme.typography.bodyMediumEmphasized,
                color = MaterialTheme.colorScheme.outline
            )
        }

        LazyColumn {
            items(state.searchResults, key = { it.hashCode() }) { item ->
                ListItem(
                    overlineContent = {
                        config.itemTransform.overlineText?.let {
                            Text(it(item))
                        }
                    },
                    headlineContent = {
                        Text(config.itemTransform.headlineText(item))
                    },
                    supportingContent = {
                        config.itemTransform.supportingText?.let {
                            Text(it(item))
                        }
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    modifier = Modifier
                        .animateItem()
                        .clickable {
                            config.onItemClick(item)
                            controller.hide()
                        },
                    trailingContent = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            config.itemTransform.trailingContent?.let {
                                it(item)
                            }
                            config.onItemEdit?.let {
                                TextButton("Edit", onClick = {
                                    it(item)
                                })
                            }
                        }
                    }
                )
            }
        }
    }
}

data class CreateButtonConfig(
    val fromScratch: CreateButtonProperties.FromScratch,
    val fromQuery: List<CreateButtonProperties.FromQuery>? = null
)

sealed class CreateButtonProperties(open val label: String) {
    data class FromScratch(override val label: String, val onClick: () -> Unit) :
        CreateButtonProperties(label)

    data class FromQuery(
        override val label: String,
        val transform: (String) -> String = { it },
        val onClick: (searchQuery: String) -> Unit
    ) :
        CreateButtonProperties(label)
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun SearchableBottomSheetPreview() {
    Column {
        SearchableBottomSheetContent(
            state = SearchableBottomSheetStateHolder<String>(),
            controller = rememberSearchableBottomSheetController(),
            config = SearchableBottomSheetConfig(
                searchPlaceholderTitle = "Item",
                itemLabel = "Items",
                onItemClick = {

                },
                onItemEdit = {

                },
                actionButtonConfig = CreateButtonConfig(
                    fromScratch = CreateButtonProperties.FromScratch(
                        "item"
                    ) {}, fromQuery = listOf()
                ),
                itemTransform = ItemTransform(
                    headlineText = {
                        it
                    },
                    supportingText = { it },
                    overlineText = { it }
                )
            )
        )
    }
}

@Composable
fun CreateFromSearchOrScratchButton(text: String, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text("Create $text") },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        leadingContent = {
            Icon(R.drawable.add_24px, contentDescription = null)
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
class SearchableBottomSheetController(
    private val scope: CoroutineScope,
    private val sheetState: SheetState
) {
    var isVisible by mutableStateOf(false)
        private set

    val state: SheetState = sheetState

    fun show() {
        isVisible = true
    }

    fun hide() {
        scope.launch {
            sheetState.hide()
        }.invokeOnCompletion {
            isVisible = false
        }
    }

    fun expand() {
        scope.launch {
            sheetState.expand()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberSearchableBottomSheetController(): SearchableBottomSheetController {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    return remember { SearchableBottomSheetController(scope, sheetState) }
}