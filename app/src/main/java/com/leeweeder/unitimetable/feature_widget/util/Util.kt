package com.leeweeder.unitimetable.feature_widget.util

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidgetManager

/**
 * Create a preferences key for a given widget ID.
 * This is used to allow different timetable for each widget in case of multiple timetables
 *
 * @param glanceId The [GlanceId] of the widget
 *
 * @return the preferences key for the given widget ID
 * */
internal fun createPreferencesKey(glanceId: GlanceId, context: Context): Preferences.Key<String> {
    return stringPreferencesKey(
        "unitimetable_widget_${
            GlanceAppWidgetManager(context).getAppWidgetId(
                glanceId
            )
        }"
    )
}