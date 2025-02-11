package com.leeweeder.timetable.feature_widget.domain

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

interface WidgetPreferenceDataStoreRepository : DataStore<Preferences> {
    suspend fun saveWidgetPreferences(widgetId: Int, timeTableId: Int)
    suspend fun readWidgetPreferences(widgetId: Int): Int?
    suspend fun deleteWidgetPreferences(widgetId: Int)
}