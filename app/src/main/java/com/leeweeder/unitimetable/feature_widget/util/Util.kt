package com.leeweeder.unitimetable.feature_widget.util

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
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
@Deprecated(
    "Use `createIntPreferencesKey` instead as we are migrating to storing the timetable id only instead of the whole data as json string.",
    replaceWith = ReplaceWith(
        expression = "createIntPreferencesKey(glanceId, context)",
        imports = ["com.leeweeder.unitimetable.feature_widget.util"]
    )
)
internal fun createStringPreferencesKey(
    glanceId: GlanceId,
    context: Context
): Preferences.Key<String> {
    return stringPreferencesKey(
        "unitimetable_widget_${
            GlanceAppWidgetManager(context).getAppWidgetId(
                glanceId
            )
        }"
    )
}

/**
 * Create a preferences key for a given widget ID.
 * This is used to allow different timetable for each widget in case of multiple timetables
 *
 * @param glanceId The [GlanceId] of the widget
 *
 * @return the preferences key for the given widget ID
 * */
internal fun createIntPreferencesKey(glanceId: GlanceId, context: Context): Preferences.Key<Int> {
    return intPreferencesKey(
        "unitimetable_widget_timetable_id=${
            GlanceAppWidgetManager(context).getAppWidgetId(
                glanceId
            )
        }"
    )
}