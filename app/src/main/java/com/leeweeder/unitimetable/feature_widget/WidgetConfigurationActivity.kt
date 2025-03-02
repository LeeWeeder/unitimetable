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

package com.leeweeder.unitimetable.feature_widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.lifecycle.lifecycleScope
import com.leeweeder.unitimetable.feature_widget.model.DisplayOption
import com.leeweeder.unitimetable.feature_widget.model.toStringSet
import com.leeweeder.unitimetable.feature_widget.ui.WidgetConfigurationScreen
import com.leeweeder.unitimetable.feature_widget.util.createDisplayOptionsKey
import com.leeweeder.unitimetable.feature_widget.util.createWidgetTimetableIdKey
import com.leeweeder.unitimetable.ui.theme.AppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class WidgetConfigurationActivity : ComponentActivity() {
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        appWidgetId =
            intent?.extras?.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        // If the user backs out of the activity before reaching the end, the system notifies the
        // app widget host that the configuration is canceled and the host doesn't add the widget
        val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(RESULT_CANCELED, resultValue)

        val glanceId = getGlanceId()
        val (initialTimetableId: Int?, initialDisplayOptions: Set<DisplayOption>) = runBlocking {
            try {
                val prefs: Preferences = getAppWidgetState(
                    context = this@WidgetConfigurationActivity,
                    glanceId = glanceId,
                    definition = PreferencesGlanceStateDefinition
                )
                prefs[createWidgetTimetableIdKey(
                    glanceId,
                    this@WidgetConfigurationActivity
                )] to DisplayOption.fromString(
                    prefs[createDisplayOptionsKey(
                        glanceId,
                        this@WidgetConfigurationActivity
                    )] ?: DisplayOption.DEFAULT.toStringSet()
                )
            } catch (e: Exception) {
                Log.e("WidgetConfiguration", "Error getting initial state", e)
                null to DisplayOption.DEFAULT
            }
        }

        setContent {
            enableEdgeToEdge(
                navigationBarStyle = SystemBarStyle.auto(
                    lightScrim = Color.TRANSPARENT,
                    darkScrim = Color.TRANSPARENT
                )
            )
            AppTheme {
                WidgetConfigurationScreen(
                    onCancelClick = {
                        finish()
                    },
                    onDone = { timetableId: Int, displayOptions: Set<DisplayOption> ->
                        saveWidgetConfiguration(
                            timetableId = timetableId,
                            displayOptions = displayOptions
                        )
                        val resultValue = Intent().apply {
                            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                        }
                        setResult(RESULT_OK, resultValue)
                        finish()
                    },
                    initialTimetableId = initialTimetableId.also {
                        Log.d("WidgetConfiguration", "Initial timetable ID: $it")
                    },
                    initialDisplayOptions = initialDisplayOptions
                )
            }
        }
    }

    private fun getGlanceId() = GlanceAppWidgetManager(this).getGlanceIdBy(appWidgetId)

    private fun saveWidgetConfiguration(timetableId: Int, displayOptions: Set<DisplayOption>) {
        saveWidgetConfiguration(
            timetableId = timetableId,
            displayOptions = displayOptions,
            scope = lifecycleScope,
            context = this,
            glanceId = getGlanceId()
        )
    }
}

fun saveWidgetConfiguration(
    timetableId: Int,
    displayOptions: Set<DisplayOption>,
    scope: CoroutineScope,
    context: Context,
    glanceId: GlanceId
) {
    scope.launch(Dispatchers.IO) {
        updateAppWidgetState(context, glanceId) { prefs ->
            prefs[createWidgetTimetableIdKey(glanceId, context)] = timetableId
            prefs[createDisplayOptionsKey(glanceId, context)] = displayOptions.toStringSet()
        }

        // Update the widget
        UnitimetableWidget().update(context, glanceId)
        Log.d("WidgetConfiguration", "Updated widget for glanceId: $glanceId")
    }
}