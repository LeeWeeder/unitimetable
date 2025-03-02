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

package com.leeweeder.unitimetable.feature_widget.domain

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

interface WidgetPreferenceDataStoreRepository : DataStore<Preferences> {
    suspend fun saveWidgetPreferences(widgetId: Int, timeTableId: Int)
    suspend fun readWidgetPreferences(widgetId: Int): Int?
    suspend fun deleteWidgetPreferences(widgetId: Int)
}