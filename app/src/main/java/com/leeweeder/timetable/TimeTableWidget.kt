/*
package com.leeweeder.timetable

import android.content.Context
import androidx.annotation.IntRange
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.Text
import com.leeweeder.timetable.data.Course
import com.leeweeder.timetable.util.formatTime
import com.leeweeder.timetable.util.toGlanceTextStyle

const val HEADER_ROW_HEIGHT = 25

val Days = listOf("M", "T", "W", "T", "F")

class TimeTableWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            Widget()
        }
    }
}

@Composable
fun Widget() {
    Column(modifier = GlanceModifier.background(MaterialTheme.colorScheme.surface).fillMaxSize()) {
        Header()
        Content()
    }
}

@Composable
fun Content(modifier: GlanceModifier = GlanceModifier) {
    Row(modifier = modifier.fillMaxSize()) {
        val weight = GlanceModifier.defaultWeight()
        TimeColumn(modifier = weight)
        Days.forEach {
            ScheduleColumn(weight.fillMaxHeight()) {
                CourseBlock(course = Course.AP3, span = 2)
            }
        }
    }
}

@Composable
fun CourseBlock(course: Course, span: Int) {
    val height = (((220 - HEADER_ROW_HEIGHT) / 9) * span).dp

    BorderBox(
        borderWidth = BorderWidth(1.dp),
        backgroundColor = course.color,
        modifier = GlanceModifier.height(height)
    ) {
        Text(text = course.code)
        Text(text = course.instructor)
    }
}

@Composable
fun TimeColumn(modifier: GlanceModifier = GlanceModifier) {
    BorderBox(borderWidth = BorderWidth(end = 1.dp), modifier = modifier) {
        Column(
            modifier = GlanceModifier.fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val weight = GlanceModifier.defaultWeight()
            (8..16).forEach { time ->
                BorderBox(
                    borderWidth = BorderWidth(bottom = 1.dp),
                    modifier = weight.fillMaxWidth()
                ) {
                    TimeBox(from = time)
                }
            }
        }
    }
}

@Composable
fun ScheduleColumn(modifier: GlanceModifier = GlanceModifier, content: @Composable () -> Unit) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        content()
    }
}

*/
/**
 * A composable that shows the start and the end time of a session
 * @param from The start time of a session
 * @param duration The duration of the session in hours defaults to [1]
 * *//*

@Composable
fun TimeBox(
    @IntRange(from = 0L, to = 23L) from: Int,
    @IntRange(from = 0L, to = 23L) duration: Int = 1
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = GlanceModifier.fillMaxSize()
    ) {
        Text(
            text = "${formatTime(from)}-${formatTime(from + duration)}",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                .toGlanceTextStyle(color = MaterialTheme.colorScheme.onSurface)
        )
    }
}

@Composable
fun Header(modifier: GlanceModifier = GlanceModifier) {
    BorderBox(borderWidth = BorderWidth(bottom = 1.5.dp)) {
        Row(
            modifier = modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).height(
                HEADER_ROW_HEIGHT.dp
            ),
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            val weight = GlanceModifier.defaultWeight()
            BorderBox(
                modifier = weight,
                borderWidth = BorderWidth(end = 1.dp)
            ) {
                Text(
                    text = "Week",
                    style = MaterialTheme.typography.labelSmall.toGlanceTextStyle(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }
            Days.forEach {
                DayHeader(day = it, modifier = weight)
            }
        }
    }
}

@Composable
fun DayHeader(day: String, modifier: GlanceModifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Text(
            text = day,
            style = MaterialTheme.typography.labelMedium.toGlanceTextStyle(color = MaterialTheme.colorScheme.onSurface)
        )
    }
}

val DEFAULT_BORDER_WIDTH = 0.dp

data class BorderWidth(
    val top: Dp = DEFAULT_BORDER_WIDTH,
    val bottom: Dp = DEFAULT_BORDER_WIDTH,
    val start: Dp = DEFAULT_BORDER_WIDTH,
    val end: Dp = DEFAULT_BORDER_WIDTH
) {
    constructor(horizontal: Dp = DEFAULT_BORDER_WIDTH, vertical: Dp = DEFAULT_BORDER_WIDTH) : this(
        top = vertical,
        bottom = vertical,
        start = horizontal,
        end = horizontal
    )

    constructor(all: Dp = DEFAULT_BORDER_WIDTH) : this(
        top = all,
        bottom = all,
        start = all,
        end = all
    )
}

*/
/**
 * A [Box] composable that has border
 * @param modifier The modifier to be applied to the content of this composable
 * @param borderWidth The width of border
 * @param borderColor The color of the border
 * @param backgroundColor The background color
 * @param contentAlignment The [Alignment] of the content
 * @param content The content of this composable
 * *//*

@Composable
fun BorderBox(
    modifier: GlanceModifier = GlanceModifier,
    borderWidth: BorderWidth,
    borderColor: Color = MaterialTheme.colorScheme.outlineVariant,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    contentAlignment: Alignment = Alignment.Center,
    content: @Composable () -> Unit
) {
    Box(
        modifier = GlanceModifier.background(borderColor).padding(
            top = borderWidth.top,
            bottom = borderWidth.bottom,
            start = borderWidth.start,
            end = borderWidth.end
        )
    ) {
        Box(
            modifier = modifier.background(backgroundColor),
            contentAlignment = contentAlignment
        ) {
            content()
        }
    }
}*/
