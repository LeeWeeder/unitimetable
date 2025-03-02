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

package com.leeweeder.unitimetable.feature_widget.util

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidgetManager

private const val PREF_TIMETABLE_ID = "unitimetable_widget_timetable_id="
private const val PREF_DISPLAY_OPTIONS = "display_options"
private const val PREF_LEGACY = "unitimetable_widget_"

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
        "$PREF_LEGACY${
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
internal fun createWidgetTimetableIdKey(
    glanceId: GlanceId,
    context: Context
): Preferences.Key<Int> {
    return intPreferencesKey(
        "$PREF_TIMETABLE_ID${
            GlanceAppWidgetManager(context).getAppWidgetId(
                glanceId
            )
        }"
    )
}

internal fun createDisplayOptionsKey(
    glanceId: GlanceId,
    context: Context
): Preferences.Key<Set<String>> =
    stringSetPreferencesKey(
        "${PREF_DISPLAY_OPTIONS}_${
            GlanceAppWidgetManager(context).getAppWidgetId(
                glanceId
            )
        }"
    )