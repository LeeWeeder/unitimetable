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

package com.leeweeder.unitimetable.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.dataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.leeweeder.timetable.data.source.TimeTablePref
import com.leeweeder.unitimetable.data.data_source.TimeTablePrefSerializer
import com.leeweeder.unitimetable.domain.repository.DataStoreRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

// TODO: Remove the main timetable pref as it is to be replaced by selected timetable pref

private val Context.timeTablePref: DataStore<TimeTablePref> by dataStore(
    fileName = "timeTablePref.pb", serializer = TimeTablePrefSerializer
)

private val Context.selectedTimetablePref: DataStore<Preferences> by preferencesDataStore("selectedTimetablePref")

class DataStoreRepositoryImpl(context: Context) : DataStoreRepository {

    companion object {
        val SELECTED_TIMETABLE_KEY = intPreferencesKey("selected_timetable")
    }

    val timeTablePrefDataStore = context.timeTablePref

    val selectedTimetableDataStore = context.selectedTimetablePref

    override val timeTablePrefFlow: Flow<TimeTablePref>
        get() = timeTablePrefDataStore.data.catch { exception ->
            if (exception is IOException) {
                emit(TimeTablePref.getDefaultInstance())
            } else {
                throw exception
            }
        }
    override val selectedTimetableIdFlow: Flow<Int>
        get() = selectedTimetableDataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[SELECTED_TIMETABLE_KEY] ?: -1 // Default value if not found
            }

    override suspend fun setMainTimeTableId(id: Int) {
        timeTablePrefDataStore.updateData {
            it.toBuilder().setMainTimeTableId(id).build()
        }
    }

    override suspend fun setSelectedTimetable(id: Int) {
        selectedTimetableDataStore.edit { preferences ->
            preferences[SELECTED_TIMETABLE_KEY] = id
        }
    }
}