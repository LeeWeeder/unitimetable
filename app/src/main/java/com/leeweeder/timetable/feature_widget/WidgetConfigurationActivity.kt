package com.leeweeder.timetable.feature_widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.lifecycle.lifecycleScope
import com.leeweeder.timetable.feature_widget.ui.WidgetConfigurationScreen
import com.leeweeder.timetable.ui.theme.AppTheme
import kotlinx.coroutines.launch

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

        setContent {
            enableEdgeToEdge(
                navigationBarStyle = SystemBarStyle.auto(
                    lightScrim = Color.TRANSPARENT,
                    darkScrim = Color.TRANSPARENT
                )
            )
            // Your configuration UI here
            AppTheme {
                WidgetConfigurationScreen(
                    onCancelClick = {
                        finish()
                    },
                    onDone = {
                        saveWidgetConfiguration(it)
                        val resultValue = Intent().apply {
                            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                        }
                        setResult(RESULT_OK, resultValue)
                        finish()
                    }
                )
            }
        }
    }

    private fun saveWidgetConfiguration(value: String) {
        lifecycleScope.launch {
            val glanceId = GlanceAppWidgetManager(this@WidgetConfigurationActivity)
                .getGlanceIdBy(appWidgetId)

            updateAppWidgetState(this@WidgetConfigurationActivity, glanceId) { prefs ->
                prefs[WidgetKey] = value
            }

            // Update the widget
            UnitimetableWidget().update(this@WidgetConfigurationActivity, glanceId)
        }
    }
}