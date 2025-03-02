/*
 * Copyright (C) 2025 Lyniel Jhay G. Maquilan (@LeeWeeder)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.leeweeder.unitimetable.ui.components.searchable_bottom_sheet

import android.util.Log
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

sealed interface SelectionAndAdditionBottomSheetState<T> {
    data class Success<T>(val items: List<T>) : SelectionAndAdditionBottomSheetState<T>
    data class Error<T>(val throwable: Throwable) : SelectionAndAdditionBottomSheetState<T>
    class Loading<T> : SelectionAndAdditionBottomSheetState<T>
}

class SearchableBottomSheetStateHolder<T> {
    val searchFieldState = TextFieldState()

    @Suppress("PropertyName")
    internal var _searchResults by mutableStateOf<List<T>>(emptyList())
    val searchResults: List<T> get() = _searchResults

    internal lateinit var runSearch: suspend () -> Unit
}

class SearchableBottomSheetStateFactory(private val scope: CoroutineScope) {
    @OptIn(FlowPreview::class)
    fun <T> create(
        dataFlow: Flow<List<T>>,
        searchPredicate: (T, String) -> Boolean
    ): SearchableBottomSheetStateHolder<T> {
        val stateHolder = SearchableBottomSheetStateHolder<T>()

        val dataState = dataFlow
            .map { SelectionAndAdditionBottomSheetState.Success(it) }
            .catch {
                Log.e("SelectionAndAdditionBottomSheetStateFactory", it.message ?: "", it)
                SelectionAndAdditionBottomSheetState.Error<T>(it)
            }
            .stateIn(
                scope,
                SharingStarted.WhileSubscribed(5000),
                SelectionAndAdditionBottomSheetState.Loading<T>()
            )

        stateHolder.runSearch = {
            combine(
                dataState,
                snapshotFlow { stateHolder.searchFieldState.text }.debounce(200)
            ) { state, searchFieldText ->
                when (state) {
                    is SelectionAndAdditionBottomSheetState.Success -> {
                        val searchQuery = searchFieldText.toString()
                        if (searchQuery.isBlank()) state.items
                        else state.items.filter { searchPredicate(it, searchQuery) }
                    }

                    else -> emptyList()
                }
            }.collectLatest { stateHolder._searchResults = it }
        }

        return stateHolder
    }
}