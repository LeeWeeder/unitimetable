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

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.text.TextPaint
import android.util.Log
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.util.TypedValueCompat.spToPx
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.appwidget.CircularProgressIndicator
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.Text
import androidx.glance.unit.ColorProvider
import com.leeweeder.unitimetable.domain.repository.TimetableRepository
import com.leeweeder.unitimetable.feature_widget.model.DisplayOption
import com.leeweeder.unitimetable.feature_widget.model.toStringSet
import com.leeweeder.unitimetable.feature_widget.ui.components.BorderContainer
import com.leeweeder.unitimetable.feature_widget.ui.components.BorderContainerDefaults
import com.leeweeder.unitimetable.feature_widget.ui.components.BorderProperties
import com.leeweeder.unitimetable.feature_widget.ui.components.BorderProperty
import com.leeweeder.unitimetable.feature_widget.ui.theme.WidgetTheme
import com.leeweeder.unitimetable.feature_widget.util.createDisplayOptionsKey
import com.leeweeder.unitimetable.feature_widget.util.createStringPreferencesKey
import com.leeweeder.unitimetable.feature_widget.util.createWidgetTimetableIdKey
import com.leeweeder.unitimetable.ui.Schedule
import com.leeweeder.unitimetable.ui.toGroupedSchedules
import com.leeweeder.unitimetable.ui.util.Constants
import com.leeweeder.unitimetable.ui.util.getDays
import com.leeweeder.unitimetable.ui.util.getTimes
import com.leeweeder.unitimetable.ui.util.plusOneHour
import com.leeweeder.unitimetable.util.isSystemInDarkTheme
import com.leeweeder.unitimetable.util.toColor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.koin.java.KoinJavaComponent.inject
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.TextStyle
import java.util.Locale
import kotlin.collections.forEach
import kotlin.text.lowercase

class UnitimetableWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = UnitimetableWidget()

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)

        // Clean up preferences for each deleted widget
        appWidgetIds.forEach { widgetId ->
            val glanceId = GlanceAppWidgetManager(context).getGlanceIdBy(widgetId)
            // Delete the preferences in a coroutine scope
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    updateAppWidgetState(context, glanceId) { preferences ->
                        // Remove our custom preference keys
                        preferences.remove(createWidgetTimetableIdKey(glanceId, context))
                    }
                } catch (e: Exception) {
                    Log.e("UnitimetableWidgetReceiver", "Error cleaning up widget preferences", e)
                }
            }
        }
    }
}

class UnitimetableWidget() : GlanceAppWidget() {
    override val sizeMode = SizeMode.Exact

    private suspend fun migrateTimetablePreferences(id: GlanceId, context: Context) {
        val stringKey = createStringPreferencesKey(id, context)
        val intKey = createWidgetTimetableIdKey(id, context)

        updateAppWidgetState(context, id) { pref ->
            if (pref[intKey] == null) {
                pref[stringKey]?.let {
                    try {
                        // Lowercase the string so that it won't have issues in case sensitivity
                        val jsonObject = Json.parseToJsonElement(it.lowercase()).jsonObject

                        Log.d(
                            "UnitimetableWidgetReceiver",
                            "Migrating widget preferences for id $it"
                        )
                        Log.d("UnitimetableWidgetReceiver", "Json string: $jsonObject")

                        val timetableObject = jsonObject["timetable"]?.jsonObject

                        Log.d(
                            "UnitimetableWidgetReceiver",
                            "Timetable object: $timetableObject"
                        )
                        Log.d(
                            "UnitimetableWidgetReceiver",
                            "Timetable id: ${timetableObject?.get("id")}"
                        )

                        val timetableId =
                            timetableObject?.get("id")?.jsonPrimitive?.int


                        // Save the timetable id from JSON if not null
                        timetableId?.let {
                            pref[intKey] = it

                            // delete the string preferences key
                            pref.remove(stringKey)
                        }

                        update(context, id)
                    } catch (e: SerializationException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId
    ) {
        // Migrate the widget preferences
        // The migration is necessary from version 0.0.1alpha01 and 0.0.1alpha02 to future releases
        // Because of renaming of parameters in the serialization
        migrateTimetablePreferences(id, context)

        val timetableRepository by inject<TimetableRepository>(TimetableRepository::class.java)

        provideContent {
            val prefs = currentState<Preferences>()
            val widgetKey = createWidgetTimetableIdKey(id, context)
            val displayOptionsKey = createDisplayOptionsKey(id, context)

            val timetableData = prefs[widgetKey]?.let { id ->
                timetableRepository.observeTimetableWithDetails(id)
                    .collectAsState(initial = null).value
            }

            WidgetTheme {
                timetableData?.let { data ->
                    // Your existing widget composition
                    val days = getDays(data.timetable.startingDay, data.timetable.numberOfDays)
                    Widget(
                        days = days,
                        dayOfWeekNow = LocalDate.now().dayOfWeek,
                        startTimes = getTimes(data.timetable.startTime, data.timetable.endTime),
                        groupedSchedules = data.sessions.toGroupedSchedules(days = days),
                        displayOptions = DisplayOption.fromString(
                            prefs[displayOptionsKey] ?: DisplayOption.DEFAULT.toStringSet()
                        )
                    )
                } ?: Box(
                    modifier = GlanceModifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = GlanceTheme.colors.onSurface)
                }
            }
        }
    }
}

@Composable
fun Widget(
    days: List<DayOfWeek>,
    dayOfWeekNow: DayOfWeek,
    startTimes: List<LocalTime>,
    groupedSchedules: List<List<Schedule>>,
    displayOptions: Set<DisplayOption>
) {
    val context = LocalContext.current
    val size = LocalSize.current

    val density = Density(context)

    val labelSmall = MaterialTheme.typography.labelSmall

    val labelHeight = with(density) {
        labelSmall.lineHeight.toDp()
    }

    Row(
        modifier = GlanceModifier.fillMaxWidth()
            .height(size.height + labelHeight)
            .background(color = GlanceTheme.colors.surface.getColor(context))
    ) {
        val leaderColumnWidth = size.width * 0.125f

        val widgetHeight = size.height

        val headerRowHeight = widgetHeight * 0.05f

        val availableHeight = widgetHeight - headerRowHeight

        val rowHeight = availableHeight / startTimes.size

        Log.d(
            "Widget",
            "Widget height: $widgetHeight, available height: $availableHeight, row height: $rowHeight"
        )

        val fontSize = labelSmall.fontSize * 0.8f

        // Leader
        Column(
            modifier = GlanceModifier
                .width(leaderColumnWidth)
                .padding(top = headerRowHeight - (labelHeight / 2))
                .fillMaxHeight()
                .labelBackground(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val labels = startTimes.toMutableList().apply {
                add(startTimes.last().plusOneHour())
            }.toList()

            labels.forEachIndexed { index, period ->
                val style = labelSmall.copy(fontSize = fontSize)
                Box(
                    modifier = GlanceModifier.height(rowHeight)
                        .width(leaderColumnWidth),
                    contentAlignment = Alignment.TopEnd
                ) {
                    GlanceText(
                        text = period.format(Constants.TimeFormatter),
                        style = style,
                        modifier = GlanceModifier
                            .height(labelHeight).padding(end = 4.dp),
                        alignment = Alignment.Center
                    )
                }
            }
        }

        // Body
        Column(
            modifier = GlanceModifier
                .defaultWeight()
                .fillMaxWidth()
        ) {
            // Header
            BorderContainer(
                width = BorderProperties.of(
                    bottom = BorderProperty(
                        1.dp,
                        BorderContainerDefaults.color
                    )
                ),
                modifier = GlanceModifier.height(headerRowHeight).fillMaxWidth().labelBackground()
            ) {
                Row(modifier = GlanceModifier.fillMaxWidth()) {
                    days.forEach { dayOfWeek ->
                        val backgroundColor = if (dayOfWeek == dayOfWeekNow) {
                            GlanceTheme.colors.primary.getColor(context).copy(alpha = 0.5f)
                        } else {
                            Color.Transparent
                        }

                        Box(
                            modifier = GlanceModifier
                                .defaultWeight()
                                .fillMaxHeight()
                                .background(color = backgroundColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                dayOfWeek.getDisplayName(
                                    TextStyle.SHORT_STANDALONE, Locale.getDefault()
                                ),
                                style = MaterialTheme.typography.labelMedium.toGlanceTextStyle(
                                    color = GlanceTheme.colors.onSurface.getColor(
                                        context
                                    )
                                )
                            )
                        }
                    }
                }
            }

            Grid(
                groupedSchedules,
                rowHeight = rowHeight,
                context = context,
                modifier = GlanceModifier.height(availableHeight).fillMaxWidth(),
                displayOptions = displayOptions
            )
        }
    }
}

private const val SCALE = 1f // TODO: Implement use preferences for the scaling

@SuppressLint("RestrictedApi")
@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun Grid(
    groupedSchedules: List<List<Schedule>>,
    context: Context,
    rowHeight: Dp,
    modifier: GlanceModifier,
    displayOptions: Set<DisplayOption>
) {
    Row(modifier = modifier) {
        groupedSchedules.forEachIndexed { index, schedules ->
            Column(
                modifier = GlanceModifier.defaultWeight().fillMaxHeight()
            ) {

                schedules.forEach { schedule ->

                    val periodSpan = schedule.periodSpan

                    BorderContainer(
                        modifier = GlanceModifier.height(rowHeight * periodSpan)
                            .fillMaxWidth(),
                        width = BorderProperties.of(
                            bottom = BorderProperty(1.dp, BorderContainerDefaults.color),
                            start = if (index == 0 || schedule.subjectInstructor == null) BorderProperty() else BorderProperty(
                                1.dp
                            )
                        ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (schedule.subjectInstructor != null) {
                            val scheme =
                                schedule.subjectInstructor.hue.createScheme(
                                    isSystemInDarkTheme(
                                        context
                                    )
                                )
                            Column(
                                modifier = GlanceModifier
                                    .fillMaxSize()
                                    .background(color = scheme.primary.toColor())
                                    .padding(horizontal = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {

                                Log.d(
                                    "Grid",
                                    "Subject Code: ${schedule.subjectInstructor.subject.code}, Description: ${schedule.subjectInstructor.subject.description}, Instructor: ${schedule.subjectInstructor.instructor?.name}"
                                )

                                val showBoth = displayOptions.containsAll(
                                    setOf(DisplayOption.SUBJECT_CODE, DisplayOption.INSTRUCTOR)
                                )
                                val maxLinesSubjectCode = when {
                                    // Both shown and period span is 1
                                    showBoth && periodSpan == 1 -> 1
                                    // Both shown and period span is 2
                                    showBoth && periodSpan == 2 -> Int.MAX_VALUE
                                    // Only one option shown (exclusive)
                                    !showBoth -> Int.MAX_VALUE
                                    // All other cases
                                    else -> Int.MAX_VALUE
                                }

                                val maxLinesInstructor = when {
                                    // Both shown and period span is 1
                                    showBoth && periodSpan == 1 -> 1
                                    // Both shown and period span is 2
                                    showBoth && periodSpan == 2 -> 1
                                    // Only one option shown (exclusive)
                                    !showBoth -> Int.MAX_VALUE
                                    // All other cases
                                    else -> Int.MAX_VALUE
                                }

                                // TODO: Utilize parent size to distribute position and sizing of the texts
                                if (displayOptions.contains(DisplayOption.SUBJECT_CODE)) {
                                    Text(
                                        schedule.subjectInstructor.subject.code.uppercase(),
                                        style = MaterialTheme.typography.labelMedium.let {
                                            it.copy(
                                                fontSize = it.fontSize * SCALE
                                            )
                                        }.toGlanceTextStyle(
                                            color = scheme.onPrimary.toColor(),
                                            textAlign = TextAlign.Center
                                        ),
                                        maxLines = maxLinesSubjectCode
                                        // TODO: Implement auto-size for subject code
                                    )
                                }

                                val textColor = scheme.onPrimary.toColor()

                                if (displayOptions.contains(DisplayOption.INSTRUCTOR)) {
                                    Text(
                                        schedule.subjectInstructor.instructor?.name ?: "",
                                        style = MaterialTheme.typography.bodySmall.let {
                                            it.copy(fontSize = it.fontSize * SCALE)
                                        }.toGlanceTextStyle(
                                            color = textColor,
                                            textAlign = TextAlign.Center
                                        ),
                                        maxLines = maxLinesInstructor
                                    )
                                }
                            }
                        } else {
                            Box(
                                modifier = GlanceModifier
                                    .fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                schedule.label?.let {
                                    Text(
                                        schedule.label,
                                        style = androidx.glance.text.TextStyle(color = GlanceTheme.colors.onSurface)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun Context.textAsBitmap(
    text: String,
    style: androidx.compose.ui.text.TextStyle,
    color: Color
): Bitmap {
    val paint = TextPaint(Paint.ANTI_ALIAS_FLAG)
    paint.textSize = spToPx(style.fontSize.value, this.resources.displayMetrics)
    paint.color = color.toArgb()

    val baseline = (paint.textSize - (paint.descent()))
    val width = (paint.measureText(text)).toInt()
    val height = (baseline + 2f).toInt()
    val image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(image)
    canvas.drawText(text, 0f, baseline, paint)
    return image
}

@Composable
fun GlanceText(
    text: String,
    style: androidx.compose.ui.text.TextStyle,
    modifier: GlanceModifier = GlanceModifier,
    alignment: Alignment = Alignment.TopStart,
    color: Color = GlanceTheme.colors.onSurface.getColor(LocalContext.current)
) {
    Box(modifier = modifier, contentAlignment = alignment) {
        Image(
            provider = ImageProvider(
                LocalContext.current.textAsBitmap(
                    text = text,
                    style = style,
                    color = color
                )
            ),
            contentDescription = null
        )
    }
}

@Composable
private fun GlanceModifier.labelBackground() =
    this.background(
        GlanceTheme.colors.surface.getColor(LocalContext.current).copy(alpha = 0.5f)
    )

@SuppressLint("RestrictedApi")
@Composable
fun androidx.compose.ui.text.TextStyle.toGlanceTextStyle(
    color: Color = GlanceTheme.colors.onSurface.getColor(LocalContext.current),
    textAlign: TextAlign = this.textAlign
): androidx.glance.text.TextStyle {
    return androidx.glance.text.TextStyle(
        color = ColorProvider(color),
        fontSize = this.fontSize,
        fontWeight = this.fontWeight?.toGlanceFontWeight(),
        fontStyle = this.fontStyle?.toGlanceFontStyle(),
        textAlign = textAlign.toGlanceTextAlign(),
        textDecoration = this.textDecoration?.toGlanceTextDecoration(),
        fontFamily = this.fontFamily?.toGlanceFontFamily()
    )
}

fun FontFamily.toGlanceFontFamily(): androidx.glance.text.FontFamily {
    return if (this == FontFamily.Serif) {
        androidx.glance.text.FontFamily.Serif
    } else if (this == FontFamily.Cursive) {
        androidx.glance.text.FontFamily.Cursive
    } else if (this == FontFamily.Monospace) {
        androidx.glance.text.FontFamily.Monospace
    } else {
        androidx.glance.text.FontFamily.SansSerif
    }

    // TODO: Search how to get the default font family of the system
}

fun TextDecoration.toGlanceTextDecoration(): androidx.glance.text.TextDecoration {
    return if (this == TextDecoration.None) {
        androidx.glance.text.TextDecoration.None
    } else if (this == TextDecoration.LineThrough) {
        androidx.glance.text.TextDecoration.LineThrough
    } else {
        androidx.glance.text.TextDecoration.Underline
    }
}

fun FontWeight.toGlanceFontWeight(): androidx.glance.text.FontWeight {
    return if (this.weight <= 400) {
        androidx.glance.text.FontWeight.Normal
    } else if (this.weight > 400 && this.weight < 500) {
        androidx.glance.text.FontWeight.Medium
    } else {
        androidx.glance.text.FontWeight.Bold
    }
}

fun FontStyle.toGlanceFontStyle(): androidx.glance.text.FontStyle {
    return if (this == FontStyle.Normal) {
        androidx.glance.text.FontStyle.Normal
    } else {
        androidx.glance.text.FontStyle.Italic
    }
}

fun TextAlign.toGlanceTextAlign(): androidx.glance.text.TextAlign {
    return if (this == TextAlign.Center) {
        androidx.glance.text.TextAlign.Center
    } else if (this == TextAlign.End) {
        androidx.glance.text.TextAlign.End
    } else if (this == TextAlign.Right) {
        androidx.glance.text.TextAlign.Right
    } else if (this == TextAlign.Start) {
        androidx.glance.text.TextAlign.Start
    } else {
        androidx.glance.text.TextAlign.Left
    }
}