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
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.lifecycle.lifecycleScope
import com.leeweeder.unitimetable.feature_widget.ui.WidgetConfigurationScreen
import com.leeweeder.unitimetable.feature_widget.util.createIntPreferencesKey
import com.leeweeder.unitimetable.ui.theme.AppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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

    private fun saveWidgetConfiguration(value: Int) {
        saveWidgetConfiguration(
            value = value,
            scope = lifecycleScope,
            context = this,
            glanceId = GlanceAppWidgetManager(this).getGlanceIdBy(appWidgetId)
        )
    }
}

fun saveWidgetConfiguration(
    value: Int,
    scope: CoroutineScope,
    context: Context,
    glanceId: GlanceId
) {
    scope.launch(Dispatchers.IO) {
        updateAppWidgetState(context, glanceId) { prefs ->
            prefs[createIntPreferencesKey(glanceId, context)] = value
        }

        // Update the widget
        UnitimetableWidget().update(context, glanceId)
        Log.d("WidgetConfiguration", "Updated widget for glanceId: $glanceId")
    }
}