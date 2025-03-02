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

package com.leeweeder.unitimetable.feature_widget.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.leeweeder.unitimetable.feature_widget.domain.WidgetPreferenceDataStoreRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

val Context.widgetPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(name = "widget_preferences_datastore")

class WidgetPreferencesDataStoreRepositoryImpl(context: Context) : WidgetPreferenceDataStoreRepository {

    val widgetDataStore = context.widgetPreferencesDataStore

    companion object {
        fun createWidgetKey(widgetId: Int) = intPreferencesKey("widget_$widgetId")
    }

    override suspend fun saveWidgetPreferences(widgetId: Int, timeTableId: Int) {
        widgetDataStore.edit { preferences ->
            preferences[createWidgetKey(widgetId)] = timeTableId
        }
    }

    override suspend fun readWidgetPreferences(widgetId: Int): Int? {
        return widgetDataStore.data.first()[createWidgetKey(widgetId)]
    }

    override suspend fun deleteWidgetPreferences(widgetId: Int) {
        widgetDataStore.edit { preferences ->
            preferences.remove(createWidgetKey(widgetId))
        }
    }

    override val data: Flow<Preferences>
        get() = widgetDataStore.data

    override suspend fun updateData(transform: suspend (Preferences) -> Preferences): Preferences {
        TODO()
    }


}