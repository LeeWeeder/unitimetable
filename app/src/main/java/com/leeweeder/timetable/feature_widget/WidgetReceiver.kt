package com.leeweeder.timetable.feature_widget

import android.annotation.SuppressLint
import android.appwidget.AppWidgetManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.text.TextPaint
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.unit.sp
import androidx.core.util.TypedValueCompat.spToPx
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
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
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.text.Text
import androidx.glance.unit.ColorProvider
import com.leeweeder.timetable.domain.relation.TimeTableWithSession
import com.leeweeder.timetable.domain.repository.TimeTableRepository
import com.leeweeder.timetable.feature_widget.data.repository.WidgetPreferencesDataStoreRepositoryImpl
import com.leeweeder.timetable.feature_widget.domain.WidgetPreferenceDataStoreRepository
import com.leeweeder.timetable.feature_widget.ui.theme.WidgetTheme
import com.leeweeder.timetable.ui.CellBorderDirection
import com.leeweeder.timetable.ui.Schedule
import com.leeweeder.timetable.ui.toMappedSchedules
import com.leeweeder.timetable.ui.util.Constants
import com.leeweeder.timetable.ui.util.getDays
import com.leeweeder.timetable.ui.util.getTimes
import com.leeweeder.timetable.ui.util.plusOneHour
import com.leeweeder.timetable.util.isSystemInDarkTheme
import com.leeweeder.timetable.util.toColor
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.inject
import java.io.File
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.TextStyle
import java.util.Locale
import kotlin.collections.forEach

class UnitimetableWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget by inject(Widget::class.java)

    // Add this to specify the configuration activity
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        // Configuration activity will be launched before the widget is added
    }
}

@Suppress("LocalVariableName")
class Widget(
    private val timeTableRepository: TimeTableRepository,
    private val widgetPreferenceDataStoreRepository: WidgetPreferenceDataStoreRepository
) : GlanceAppWidget() {

    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId
    ) {

        provideContent {
            val timeTableId =
                currentState<Preferences>()[WidgetPreferencesDataStoreRepositoryImpl.createWidgetKey(
                    GlanceAppWidgetManager(context).getAppWidgetId(id)
                )]

            val _timeTableWithSession = remember { mutableStateOf<TimeTableWithSession?>(null) }
            val timeTableWithSession: State<TimeTableWithSession?> =
                _timeTableWithSession

            val scope = rememberCoroutineScope()

            LaunchedEffect(timeTableId) {
                scope.launch {
                    _timeTableWithSession.value = timeTableRepository.getTimeTableWithDetails(
                        widgetPreferenceDataStoreRepository.readWidgetPreferences(
                            GlanceAppWidgetManager(
                                context
                            ).getAppWidgetId(id)
                        ) ?: 0
                    )
                }
            }

            WidgetTheme {
                val timeTableWithDetails = timeTableWithSession.value

                if (timeTableWithDetails != null) {
                    val timeTable = timeTableWithDetails.timeTable
                    val days = getDays(
                        timeTable.startingDay,
                        timeTable.numberOfDays
                    )
                    val dayOfWeekNow = LocalDate.now().dayOfWeek
                    val startTimes = getTimes(timeTable.startTime, timeTable.endTime)
                    Widget(
                        days = days,
                        dayOfWeekNow = dayOfWeekNow,
                        startTimes = startTimes,
                        dayScheduleMap = timeTableWithDetails.sessions.toMappedSchedules()
                    )
                }
            }
        }
    }

    override suspend fun onDelete(context: Context, glanceId: GlanceId) {
        super.onDelete(context, glanceId)
        val widgetId = glanceId.toString().toIntOrNull() ?: return
        widgetPreferenceDataStoreRepository.deleteWidgetPreferences(widgetId)
    }

    override val stateDefinition: GlanceStateDefinition<Preferences>
        get() = object : GlanceStateDefinition<Preferences> {
            override suspend fun getDataStore(
                context: Context,
                fileKey: String
            ): DataStore<Preferences> {
                return WidgetPreferencesDataStoreRepositoryImpl(context)
            }

            override fun getLocation(
                context: Context,
                fileKey: String
            ): File {
                return File(context.applicationInfo.dataDir, "datastore/file")
            }

        }
}

@Composable
fun Widget(
    days: List<DayOfWeek>,
    dayOfWeekNow: DayOfWeek,
    startTimes: List<LocalTime>,
    dayScheduleMap: Map<DayOfWeek, List<Schedule>>
) {
    val context = LocalContext.current
    Row(
        modifier = GlanceModifier.fillMaxSize()
            .background(color = GlanceTheme.colors.surface.getColor(context))
    ) {
        val size = LocalSize.current
        val leaderColumnWidth = size.width * 0.125f

        val widgetHeight = size.height

        val headerRowHeight = widgetHeight * 0.05f

        val rowHeight = (widgetHeight - headerRowHeight) / startTimes.size

        val labelSmall = MaterialTheme.typography.labelSmall

        val fontSize = labelSmall.fontSize * SCALE

        val density = Density(context)

        val labelHeight = with(density) {
            labelSmall.lineHeight.toDp()
        }

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
                        .width(leaderColumnWidth)
                ) {
                    GlanceText(
                        text = period.format(Constants.TimeFormatter),
                        style = style,
                        modifier = GlanceModifier.fillMaxWidth()
                            .height(labelHeight),
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
                width = BorderSize.of(bottom = 1.dp),
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

            Grid(dayScheduleMap, rowHeight = rowHeight, context = context)
        }
    }
}

private const val SCALE = 0.9f

@SuppressLint("RestrictedApi")
@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun Grid(
    dayScheduleMap: Map<DayOfWeek, List<Schedule>>,
    context: Context,
    rowHeight: Dp
) {
    Row(modifier = GlanceModifier.fillMaxSize()) {
        dayScheduleMap.forEach { (_, schedules) ->
            Column(
                modifier = GlanceModifier.defaultWeight().fillMaxHeight()
            ) {
                schedules.forEach { schedule ->

                    val subjectDescriptionMaxLine =
                        if (schedule.periodSpan == 1) 1 else Int.MAX_VALUE
                    val instructorNameMaxLine = if (schedule.periodSpan == 1) 1 else Int.MAX_VALUE

                    BorderContainer(
                        modifier = GlanceModifier.height(rowHeight * schedule.periodSpan)
                            .fillMaxWidth(),
                        width = BorderSize.of(bottom = 1.dp, start = 1.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalAlignment = Alignment.CenterVertically
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
                                        .background(color = scheme.primary.toColor()),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    // TODO: Utilize parent size to distribute position and sizing of the texts
                                    Text(
                                        schedule.subjectInstructor.subject!!.code.uppercase(),
                                        style = MaterialTheme.typography.labelSmallEmphasized.copy(
                                            fontSize = MaterialTheme.typography.labelSmallEmphasized.fontSize * SCALE
                                        ).toGlanceTextStyle(
                                            color = scheme.onPrimary.toColor(),
                                            textAlign = TextAlign.Center
                                        ),
                                        // TODO: Implement auto-size for subject code
                                    )

                                    val bodySmall =
                                        MaterialTheme.typography.bodySmall.copy(fontSize = MaterialTheme.typography.bodySmall.fontSize * SCALE)
                                    val bodySmallFontSizeValue = bodySmall.fontSize.value

                                    val textColor = scheme.onPrimary.toColor()

                                    Text(
                                        schedule.subjectInstructor.subject.description,
                                        style = bodySmall.copy(
                                            fontSize = (bodySmallFontSizeValue - 2).sp,
                                            lineHeight = (bodySmallFontSizeValue - 1).sp
                                        ).toGlanceTextStyle(
                                            color = textColor,
                                            textAlign = TextAlign.Center
                                        ),
                                        maxLines = subjectDescriptionMaxLine
                                    )

                                    Text(
                                        schedule.subjectInstructor.instructor?.name
                                            ?: "No instructor",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = MaterialTheme.typography.labelSmall.fontSize * SCALE)
                                            .toGlanceTextStyle(
                                                color = textColor,
                                                textAlign = TextAlign.Center
                                            ),
                                        maxLines = instructorNameMaxLine
                                    )
                                }
                            } else {
                                Box(
                                    modifier = GlanceModifier
                                        .fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(if (schedule.label != null) schedule.label else "Break")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@ConsistentCopyVisibility
data class BorderSize private constructor(
    val top: Dp,
    val bottom: Dp,
    val start: Dp,
    val end: Dp
) {
    companion object {
        fun of(all: Dp): BorderSize {
            return BorderSize(all, all, all, all)
        }

        fun of(horizontal: Dp, vertical: Dp = 0.dp): BorderSize {
            return BorderSize(
                start = horizontal,
                end = horizontal,
                top = vertical,
                bottom = vertical
            )
        }

        fun of(top: Dp = 0.dp, bottom: Dp = 0.dp, start: Dp = 0.dp, end: Dp = 0.dp): BorderSize {
            return BorderSize(
                start = start,
                end = end,
                top = top,
                bottom = bottom
            )
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

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview
@Composable
private fun GlanceTextPreview() {
    val lineHeight = 10.sp
    val style =
        MaterialTheme.typography.labelSmall.copy(fontSize = lineHeight, lineHeight = lineHeight)
    val context = LocalContext.current
    GlanceText(
        text = "Test",
        style = style,
        color = Color.Red,
        modifier = GlanceModifier.height(lineHeight.value.dp)
    )
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview
@Composable
private fun GlanceWithoutHeightTextPreview() {
    val lineHeight = 20.sp
    val style =
        MaterialTheme.typography.labelSmall.copy(fontSize = lineHeight, lineHeight = lineHeight)
    GlanceText(
        text = "Test",
        style = style,
        color = Color.Red
    )
}

@Composable
private fun BorderContainer(
    width: BorderSize = BorderSize.of(1.dp),
    contentAlignment: Alignment = Alignment.TopStart,
    modifier: GlanceModifier = GlanceModifier,
    content: (@Composable () -> Unit)? = null
) {
    Row(modifier) {
        if (width.start.value != 0f) CellBorder(
            CellBorderDirection.Vertical,
            thickness = width.start
        )

        Column(modifier = GlanceModifier.defaultWeight().fillMaxHeight()) {
            if (width.top.value != 0f) {
                CellBorder(CellBorderDirection.Horizontal, thickness = width.top)
            }

            content?.let {
                Box(
                    modifier = GlanceModifier.defaultWeight(),
                    contentAlignment = contentAlignment
                ) {
                    it()
                }
            }

            if (width.bottom.value != 0f) {
                CellBorder(CellBorderDirection.Horizontal, thickness = width.bottom)
            }
        }

        if (width.end.value != 0f) {
            CellBorder(CellBorderDirection.Vertical, thickness = width.end)
        }
    }
}


@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 300, heightDp = 300)
@Composable
private fun BorderContainerPreview() {
    Box(modifier = GlanceModifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        BorderContainer(
            modifier = GlanceModifier.height(50.dp),
            contentAlignment = Alignment.Center,
            width = BorderSize.of(top = 1.dp)
        ) {
            Text("Hello")
        }
    }
}

@Composable
private fun GlanceModifier.labelBackground() =
    this.background(GlanceTheme.colors.surface.getColor(LocalContext.current).copy(alpha = 0.5f))

@Composable
private fun CellBorder(borderDirection: CellBorderDirection, thickness: Dp) {
    val color = GlanceTheme.colors.outline

    when (borderDirection) {
        CellBorderDirection.Horizontal -> {
            Box(modifier = GlanceModifier.fillMaxWidth().height(thickness).background(color)) { }
        }

        CellBorderDirection.Vertical -> {
            Box(modifier = GlanceModifier.fillMaxHeight().width(thickness).background(color)) { }
        }
    }
}

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