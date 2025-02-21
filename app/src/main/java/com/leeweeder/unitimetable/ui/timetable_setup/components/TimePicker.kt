package com.leeweeder.unitimetable.ui.timetable_setup.components

import android.text.format.DateFormat.is24HourFormat
import androidx.annotation.IntRange
import androidx.collection.IntList
import androidx.collection.MutableIntList
import androidx.collection.intListOf
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.MutatePriority.PreventUserInput
import androidx.compose.foundation.MutatorMutex
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.Typography
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.SuspendingPointerInputModifierNode
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.LayoutModifier
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.DelegatingNode
import androidx.compose.ui.node.LayoutAwareModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.PointerInputModifierNode
import androidx.compose.ui.node.requireDensity
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.platform.InspectorValueInfo
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.selectableGroup
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.center
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastFilter
import androidx.compose.ui.util.fastFirst
import androidx.compose.ui.util.fastFirstOrNull
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.util.fastMap
import androidx.compose.ui.zIndex
import com.leeweeder.unitimetable.ui.timetable_setup.components.TimePickerTokens.PeriodSelectorContainerShape
import com.leeweeder.unitimetable.ui.timetable_setup.components.TimePickerTokens.PeriodSelectorHorizontalContainerHeight
import com.leeweeder.unitimetable.ui.timetable_setup.components.TimePickerTokens.PeriodSelectorHorizontalContainerWidth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale
import java.util.WeakHashMap
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.round
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 *
 * Time pickers help users select and set a specific time.
 *
 * Shows a picker that allows the user to select time. Subscribe to updates through
 * [TimePickerState]
 *
 * [state] state for this timepicker, allows to subscribe to changes to [TimePickerState.hour]
 *    and set the initial time for this picker.
 *
 * @param state state for this time input, allows to subscribe to changes to [TimePickerState.hour]
 *   and set the initial time for this input.
 * @param modifier the [Modifier] to be applied to this time input
 * @param colors colors [TimePickerColors] that will be used to resolve the colors used for this
 *   time picker in different states. See [TimePickerDefaults.colors].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePicker(
    state: TimePickerState,
    modifier: Modifier = Modifier,
    colors: TimePickerColors = TimePickerDefaults.colors()
) {
    val analogState = remember(state) { AnalogTimePickerState(state) }
    TimePicker(
        state = analogState,
        modifier = modifier,
        colors = colors
    )
}

/** Contains the default values used by [TimePicker] */
@Stable
object TimePickerDefaults {

    /** Default colors used by a [TimePicker] in different states */
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun colors() = MaterialTheme.colorScheme.defaultTimePickerColors

    /**
     * Default colors used by a [TimePicker] in different states
     *
     * @param clockDialColor The color of the clock dial.
     * @param clockDialSelectedContentColor the color of the numbers of the clock dial when they are
     *   selected or overlapping with the selector
     * @param clockDialUnselectedContentColor the color of the numbers of the clock dial when they
     *   are unselected
     * @param selectorColor The color of the clock dial selector.
     * @param containerColor The container color of the time picker.
     * @param periodSelectorBorderColor the color used for the border of the AM/PM toggle.
     * @param periodSelectorSelectedContainerColor the color used for the selected container of the
     *   AM/PM toggle
     * @param periodSelectorUnselectedContainerColor the color used for the unselected container of
     *   the AM/PM toggle
     * @param periodSelectorSelectedContentColor color used for the selected content of the AM/PM
     *   toggle
     * @param periodSelectorUnselectedContentColor color used for the unselected content of the
     *   AM/PM toggle
     * @param timeSelectorSelectedContainerColor color used for the selected container of the
     *   display buttons to switch between hour and minutes
     * @param timeSelectorUnselectedContainerColor color used for the unselected container of the
     *   display buttons to switch between hour and minutes
     * @param timeSelectorSelectedContentColor color used for the selected content of the display
     *   buttons to switch between hour and minutes
     * @param timeSelectorUnselectedContentColor color used for the unselected content of the
     *   display buttons to switch between hour and minutes
     */
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun colors(
        clockDialColor: Color = Color.Unspecified,
        clockDialSelectedContentColor: Color = Color.Unspecified,
        clockDialUnselectedContentColor: Color = Color.Unspecified,
        selectorColor: Color = Color.Unspecified,
        containerColor: Color = Color.Unspecified,
        periodSelectorBorderColor: Color = Color.Unspecified,
        periodSelectorSelectedContainerColor: Color = Color.Unspecified,
        periodSelectorUnselectedContainerColor: Color = Color.Unspecified,
        periodSelectorSelectedContentColor: Color = Color.Unspecified,
        periodSelectorUnselectedContentColor: Color = Color.Unspecified,
        timeSelectorSelectedContainerColor: Color = Color.Unspecified,
        timeSelectorUnselectedContainerColor: Color = Color.Unspecified,
        timeSelectorSelectedContentColor: Color = Color.Unspecified,
        timeSelectorUnselectedContentColor: Color = Color.Unspecified,
    ) =
        MaterialTheme.colorScheme.defaultTimePickerColors.copy(
            clockDialColor = clockDialColor,
            clockDialSelectedContentColor = clockDialSelectedContentColor,
            clockDialUnselectedContentColor = clockDialUnselectedContentColor,
            selectorColor = selectorColor,
            containerColor = containerColor,
            periodSelectorBorderColor = periodSelectorBorderColor,
            periodSelectorSelectedContainerColor = periodSelectorSelectedContainerColor,
            periodSelectorUnselectedContainerColor = periodSelectorUnselectedContainerColor,
            periodSelectorSelectedContentColor = periodSelectorSelectedContentColor,
            periodSelectorUnselectedContentColor = periodSelectorUnselectedContentColor,
            timeSelectorSelectedContainerColor = timeSelectorSelectedContainerColor,
            timeSelectorUnselectedContainerColor = timeSelectorUnselectedContainerColor,
            timeSelectorSelectedContentColor = timeSelectorSelectedContentColor,
            timeSelectorUnselectedContentColor = timeSelectorUnselectedContentColor
        )

    @OptIn(ExperimentalMaterial3Api::class)
    internal val ColorScheme.defaultTimePickerColors: TimePickerColors
        get() {
            return TimePickerColors(
                clockDialColor = fromToken(TimePickerTokens.ClockDialColor),
                clockDialSelectedContentColor = fromToken(TimePickerTokens.ClockDialSelectedLabelTextColor),
                clockDialUnselectedContentColor =
                fromToken(TimePickerTokens.ClockDialUnselectedLabelTextColor),
                selectorColor = fromToken(TimePickerTokens.ClockDialSelectorHandleContainerColor),
                containerColor = fromToken(TimePickerTokens.ContainerColor),
                periodSelectorBorderColor = fromToken(TimePickerTokens.PeriodSelectorOutlineColor),
                periodSelectorSelectedContainerColor =
                fromToken(TimePickerTokens.PeriodSelectorSelectedContainerColor),
                periodSelectorUnselectedContainerColor = Color.Transparent,
                periodSelectorSelectedContentColor =
                fromToken(TimePickerTokens.PeriodSelectorSelectedLabelTextColor),
                periodSelectorUnselectedContentColor =
                fromToken(TimePickerTokens.PeriodSelectorUnselectedLabelTextColor),
                timeSelectorSelectedContainerColor =
                fromToken(TimePickerTokens.TimeSelectorSelectedContainerColor),
                timeSelectorUnselectedContainerColor =
                fromToken(TimePickerTokens.TimeSelectorUnselectedContainerColor),
                timeSelectorSelectedContentColor =
                fromToken(TimePickerTokens.TimeSelectorSelectedLabelTextColor),
                timeSelectorUnselectedContentColor =
                fromToken(TimePickerTokens.TimeSelectorUnselectedLabelTextColor),
            )
        }
}

/**
 * Represents the colors used by a [TimePicker] in different states
 *
 * @param clockDialColor The color of the clock dial.
 * @param clockDialSelectedContentColor the color of the numbers of the clock dial when they are
 *   selected or overlapping with the selector
 * @param clockDialUnselectedContentColor the color of the numbers of the clock dial when they are
 *   unselected
 * @param selectorColor The color of the clock dial selector.
 * @param containerColor The container color of the time picker.
 * @param periodSelectorBorderColor the color used for the border of the AM/PM toggle.
 * @param periodSelectorSelectedContainerColor the color used for the selected container of the
 *   AM/PM toggle
 * @param periodSelectorUnselectedContainerColor the color used for the unselected container of the
 *   AM/PM toggle
 * @param periodSelectorSelectedContentColor color used for the selected content of the AM/PM toggle
 * @param periodSelectorUnselectedContentColor color used for the unselected content of the AM/PM
 *   toggle
 * @param timeSelectorSelectedContainerColor color used for the selected container of the display
 *   buttons to switch between hour and minutes
 * @param timeSelectorUnselectedContainerColor color used for the unselected container of the
 *   display buttons to switch between hour and minutes
 * @param timeSelectorSelectedContentColor color used for the selected content of the display
 *   buttons to switch between hour and minutes
 * @param timeSelectorUnselectedContentColor color used for the unselected content of the display
 *   buttons to switch between hour and minutes
 * @constructor create an instance with arbitrary colors. See [TimePickerDefaults.colors] for the
 *   default implementation that follows Material specifications.
 */
@Immutable
@ExperimentalMaterial3Api
class TimePickerColors(
    val clockDialColor: Color,
    val selectorColor: Color,
    val containerColor: Color,
    val periodSelectorBorderColor: Color,
    val clockDialSelectedContentColor: Color,
    val clockDialUnselectedContentColor: Color,
    val periodSelectorSelectedContainerColor: Color,
    val periodSelectorUnselectedContainerColor: Color,
    val periodSelectorSelectedContentColor: Color,
    val periodSelectorUnselectedContentColor: Color,
    val timeSelectorSelectedContainerColor: Color,
    val timeSelectorUnselectedContainerColor: Color,
    val timeSelectorSelectedContentColor: Color,
    val timeSelectorUnselectedContentColor: Color,
) {
    /**
     * Returns a copy of this TimePickerColors, optionally overriding some of the values. This uses
     * the Color.Unspecified to mean “use the value from the source”
     */
    fun copy(
        clockDialColor: Color = this.containerColor,
        selectorColor: Color = this.selectorColor,
        containerColor: Color = this.containerColor,
        periodSelectorBorderColor: Color = this.periodSelectorBorderColor,
        clockDialSelectedContentColor: Color = this.clockDialSelectedContentColor,
        clockDialUnselectedContentColor: Color = this.clockDialUnselectedContentColor,
        periodSelectorSelectedContainerColor: Color = this.periodSelectorSelectedContainerColor,
        periodSelectorUnselectedContainerColor: Color = this.periodSelectorUnselectedContainerColor,
        periodSelectorSelectedContentColor: Color = this.periodSelectorSelectedContentColor,
        periodSelectorUnselectedContentColor: Color = this.periodSelectorUnselectedContentColor,
        timeSelectorSelectedContainerColor: Color = this.timeSelectorSelectedContainerColor,
        timeSelectorUnselectedContainerColor: Color = this.timeSelectorUnselectedContainerColor,
        timeSelectorSelectedContentColor: Color = this.timeSelectorSelectedContentColor,
        timeSelectorUnselectedContentColor: Color = this.timeSelectorUnselectedContentColor,
    ) =
        TimePickerColors(
            clockDialColor.takeOrElse { this.clockDialColor },
            selectorColor.takeOrElse { this.selectorColor },
            containerColor.takeOrElse { this.containerColor },
            periodSelectorBorderColor.takeOrElse { this.periodSelectorBorderColor },
            clockDialSelectedContentColor.takeOrElse { this.clockDialSelectedContentColor },
            clockDialUnselectedContentColor.takeOrElse { this.clockDialUnselectedContentColor },
            periodSelectorSelectedContainerColor.takeOrElse {
                this.periodSelectorSelectedContainerColor
            },
            periodSelectorUnselectedContainerColor.takeOrElse {
                this.periodSelectorUnselectedContainerColor
            },
            periodSelectorSelectedContentColor.takeOrElse {
                this.periodSelectorSelectedContentColor
            },
            periodSelectorUnselectedContentColor.takeOrElse {
                this.periodSelectorUnselectedContentColor
            },
            timeSelectorSelectedContainerColor.takeOrElse {
                this.timeSelectorSelectedContainerColor
            },
            timeSelectorUnselectedContainerColor.takeOrElse {
                this.timeSelectorUnselectedContainerColor
            },
            timeSelectorSelectedContentColor.takeOrElse { this.timeSelectorSelectedContentColor },
            timeSelectorUnselectedContentColor.takeOrElse {
                this.timeSelectorUnselectedContentColor
            },
        )

    @Stable
    internal fun clockDialContentColor(selected: Boolean) =
        if (selected) {
            clockDialSelectedContentColor
        } else {
            clockDialUnselectedContentColor
        }

    @Stable
    internal fun periodSelectorContainerColor(selected: Boolean) =
        if (selected) {
            periodSelectorSelectedContainerColor
        } else {
            periodSelectorUnselectedContainerColor
        }

    @Stable
    internal fun periodSelectorContentColor(selected: Boolean) =
        if (selected) {
            periodSelectorSelectedContentColor
        } else {
            periodSelectorUnselectedContentColor
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other === null) return false
        if (this::class != other::class) return false

        other as TimePickerColors

        if (clockDialColor != other.clockDialColor) return false
        if (selectorColor != other.selectorColor) return false
        if (containerColor != other.containerColor) return false
        if (periodSelectorBorderColor != other.periodSelectorBorderColor) return false
        if (periodSelectorSelectedContainerColor != other.periodSelectorSelectedContainerColor)
            return false
        if (periodSelectorUnselectedContainerColor != other.periodSelectorUnselectedContainerColor)
            return false
        if (periodSelectorSelectedContentColor != other.periodSelectorSelectedContentColor)
            return false
        if (periodSelectorUnselectedContentColor != other.periodSelectorUnselectedContentColor)
            return false
        if (timeSelectorSelectedContainerColor != other.timeSelectorSelectedContainerColor)
            return false
        if (timeSelectorUnselectedContainerColor != other.timeSelectorUnselectedContainerColor)
            return false
        if (timeSelectorSelectedContentColor != other.timeSelectorSelectedContentColor) return false
        if (timeSelectorUnselectedContentColor != other.timeSelectorUnselectedContentColor)
            return false

        return true
    }

    override fun hashCode(): Int {
        var result = clockDialColor.hashCode()
        result = 31 * result + selectorColor.hashCode()
        result = 31 * result + containerColor.hashCode()
        result = 31 * result + periodSelectorBorderColor.hashCode()
        result = 31 * result + periodSelectorSelectedContainerColor.hashCode()
        result = 31 * result + periodSelectorUnselectedContainerColor.hashCode()
        result = 31 * result + periodSelectorSelectedContentColor.hashCode()
        result = 31 * result + periodSelectorUnselectedContentColor.hashCode()
        result = 31 * result + timeSelectorSelectedContainerColor.hashCode()
        result = 31 * result + timeSelectorUnselectedContainerColor.hashCode()
        result = 31 * result + timeSelectorSelectedContentColor.hashCode()
        result = 31 * result + timeSelectorUnselectedContentColor.hashCode()
        return result
    }
}

/**
 * Creates a [TimePickerState] for a time picker that is remembered across compositions and
 * configuration changes.
 *
 * @param initialHour starting hour for this state, will be displayed in the time picker when
 *   launched. Ranges from 0 to 23
 * @param is24Hour The format for this time picker. `false` for 12 hour format with an AM/PM toggle
 *   or `true` for 24 hour format without toggle. Defaults to follow system setting.
 */
@Composable
@ExperimentalMaterial3Api
fun rememberTimePickerState(
    initialHour: Int = 0,
    is24Hour: Boolean = is24HourFormat(LocalContext.current),
): TimePickerState {
    val state: TimePickerStateImpl =
        rememberSaveable(saver = TimePickerStateImpl.Saver()) {
            TimePickerStateImpl(
                initialHour = initialHour,
                is24Hour = is24Hour,
            )
        }

    return state
}

/**
 * A state object that can be hoisted to observe the time picker state. It holds the current values
 * and allows for directly setting those values.
 *
 * @see rememberTimePickerState to construct the default implementation.
 */
interface TimePickerState {

    /** The currently selected hour (0-23). */
    @get:IntRange(from = 0, to = 23)
    @setparam:IntRange(from = 0, to = 23)
    var hour: Int

    /**
     * Indicates whether the time picker uses 24-hour format (`true`) or 12-hour format with AM/PM
     * (`false`).
     */
    var is24hour: Boolean

    /** Indicates whether the selected time falls within the afternoon period (12 PM - 12 AM). */
    var isAfternoon: Boolean
}

private class TimePickerStateImpl(
    initialHour: Int,
    is24Hour: Boolean,
) : TimePickerState {
    init {
        require(initialHour in 0..23) { "initialHour should in [0..23] range" }
    }

    override var is24hour: Boolean = is24Hour

    override var isAfternoon by mutableStateOf(initialHour >= 12)

    val hourState = mutableIntStateOf(initialHour % 12)

    override var hour: Int
        get() = hourState.intValue + if (isAfternoon) 12 else 0
        set(value) {
            isAfternoon = value >= 12
            hourState.intValue = value % 12
        }

    companion object {
        /** The default [Saver] implementation for [TimePickerState]. */
        fun Saver(): Saver<TimePickerStateImpl, *> =
            Saver(
                save = { listOf(it.hour, it.is24hour) },
                restore = { value ->
                    TimePickerStateImpl(
                        initialHour = value[0] as Int,
                        is24Hour = value[2] as Boolean
                    )
                }
            )
    }
}

private class AnalogTimePickerState(val state: TimePickerState) : TimePickerState by state {

    val currentAngle: Float
        get() = anim.value

    private var hourAngle = RadiansPerHour * (state.hour % 12) - FullCircle / 4

    val clockFaceValues: IntList
        get() = Hours

    private fun endValueForAnimation(new: Float): Float {
        // Calculate the absolute angular difference
        var diff = anim.value - new
        // Normalize the angular difference to be between -π and π
        while (diff > HalfCircle) {
            diff -= FullCircle
        }
        while (diff <= -HalfCircle) {
            diff += FullCircle
        }

        return anim.value - diff
    }

    private var anim = Animatable(hourAngle)

    suspend fun onGestureEnd() {
        val end =
            endValueForAnimation(hourAngle)

        mutex.mutate(priority = PreventUserInput) { anim.animateTo(end, spring()) }
    }

    suspend fun rotateTo(angle: Float, animate: Boolean = false) {
        mutex.mutate(MutatePriority.UserInput) {
            hourAngle = angle.toHour() % 12 * RadiansPerHour
            state.hour = hourAngle.toHour() % 12 + if (isAfternoon) 12 else 0

            if (!animate) {
                anim.snapTo(offsetAngle(angle))
            } else {
                val endAngle = endValueForAnimation(offsetAngle(angle))
                anim.animateTo(endAngle, spring(dampingRatio = 1f, stiffness = 700f))
            }
        }
    }

    override var hour: Int
        get() = state.hour
        set(value) {
            hourAngle = RadiansPerHour * (value % 12) - FullCircle / 4
            state.hour = value
            anim = Animatable(hourAngle)
        }

    private val mutex = MutatorMutex()

    private fun Float.toHour(): Int {
        val hourOffset: Float = RadiansPerHour / 2
        val totalOffset = hourOffset + QuarterCircle
        return ((this + totalOffset) / RadiansPerHour).toInt() % 12
    }

    private fun offsetAngle(angle: Float): Float {
        val ret = angle + QuarterCircle.toFloat()
        return if (ret < 0) ret + FullCircle else ret
    }
}

@OptIn(ExperimentalMaterial3Api::class)
private fun TimePickerState.moveSelector(x: Float, y: Float, maxDist: Float, center: IntOffset) {
    if (is24hour) {
        isAfternoon = dist(x, y, center.x, center.y) < maxDist
    }
}

private suspend fun AnalogTimePickerState.onTap(
    x: Float,
    y: Float,
    maxDist: Float,
    center: IntOffset,
) {
    var angle = atan(y - center.y, x - center.x)

    angle = round(angle / RadiansPerHour) * RadiansPerHour

    moveSelector(x, y, maxDist, center)
    rotateTo(angle, animate = true)

    delay(100)
}

@OptIn(ExperimentalMaterial3Api::class)
private val AnalogTimePickerState.selectorPos: DpOffset
    get() {
        val handleRadiusPx = TimePickerTokens.ClockDialSelectorHandleContainerSize / 2
        val selectorLength =
            if (is24hour && this.isAfternoon) {
                InnerCircleRadius
            } else {
                OuterCircleSizeRadius
            }
                .minus(handleRadiusPx)

        val length = selectorLength + handleRadiusPx
        val offsetX = length * cos(currentAngle) + TimePickerTokens.ClockDialContainerSize / 2
        val offsetY = length * sin(currentAngle) + TimePickerTokens.ClockDialContainerSize / 2

        return DpOffset(offsetX, offsetY)
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePicker(
    state: AnalogTimePickerState,
    modifier: Modifier = Modifier,
    colors: TimePickerColors = TimePickerDefaults.colors()
) {
    if (state.state.is24hour) {
        Box(
            modifier = modifier.padding(bottom = ClockFaceBottomMargin),
            contentAlignment = Alignment.Center
        ) {
            ClockFace(state, colors)
        }
    } else {
        Column(
            modifier = modifier.padding(bottom = ClockFaceBottomMargin),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HorizontalPeriodToggle(
                modifier =
                Modifier.size(
                    PeriodSelectorHorizontalContainerWidth,
                    PeriodSelectorHorizontalContainerHeight
                ),
                state = state,
                colors = colors,
            )
            Spacer(modifier = Modifier.height(ClockDisplayBottomMargin))
            ClockFace(state, colors)
        }
    }
}


private data class ClockDialModifier @OptIn(ExperimentalMaterial3Api::class) constructor(
    private val state: AnalogTimePickerState
) : ModifierNodeElement<ClockDialNode>() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun create(): ClockDialNode =
        ClockDialNode(
            state = state
        )

    @OptIn(ExperimentalMaterial3Api::class)
    override fun update(node: ClockDialNode) {
        node.updateNode(
            state = state
        )
    }

    override fun InspectorInfo.inspectableProperties() {
        // Show nothing in the inspector.
    }
}

private class ClockDialNode(
    private var state: AnalogTimePickerState
) :
    DelegatingNode(),
    PointerInputModifierNode,
    CompositionLocalConsumerModifierNode,
    LayoutAwareModifierNode {

    private var offsetX = 0f
    private var offsetY = 0f
    private var center: IntOffset = IntOffset.Zero
    private val maxDist
        get() = with(requireDensity()) { MaxDistance.toPx() }

    private val pointerInputTapNode =
        delegate(
            SuspendingPointerInputModifierNode {
                detectTapGestures(
                    onPress = {
                        offsetX = it.x
                        offsetY = it.y
                    },
                    onTap = {
                        coroutineScope.launch {
                            state.onTap(it.x, it.y, maxDist, center)
                        }
                    },
                )
            }
        )

    private val pointerInputDragNode =
        delegate(
            SuspendingPointerInputModifierNode {
                detectDragGestures(
                    onDragEnd = {
                        coroutineScope.launch {
                            state.onGestureEnd()
                        }
                    }
                ) { _, dragAmount ->
                    coroutineScope.launch {
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                        state.rotateTo(atan(offsetY - center.y, offsetX - center.x))
                    }
                    state.moveSelector(offsetX, offsetY, maxDist, center)
                }
            }
        )

    override fun onRemeasured(size: IntSize) {
        center = size.center
    }

    override fun onPointerEvent(
        pointerEvent: PointerEvent,
        pass: PointerEventPass,
        bounds: IntSize
    ) {
        pointerInputTapNode.onPointerEvent(pointerEvent, pass, bounds)
        pointerInputDragNode.onPointerEvent(pointerEvent, pass, bounds)
    }

    override fun onCancelPointerInput() {
        pointerInputTapNode.onCancelPointerInput()
        pointerInputDragNode.onCancelPointerInput()
    }

    fun updateNode(
        state: AnalogTimePickerState
    ) {
        this.state = state
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClockFace(
    state: AnalogTimePickerState,
    colors: TimePickerColors
) {
    Crossfade(
        modifier =
        Modifier
            .background(shape = CircleShape, color = colors.clockDialColor)
            .then(ClockDialModifier(state))
            .size(TimePickerTokens.ClockDialContainerSize)
            .drawSelector(state, colors),
        targetState = state.clockFaceValues,
        animationSpec = tween(durationMillis = 200)
    ) { screen ->
        CircularLayout(
            modifier = Modifier.Companion
                .size(TimePickerTokens.ClockDialContainerSize)
                .semantics { selectableGroup() },
            radius = OuterCircleSizeRadius,
        ) {
            CompositionLocalProvider(
                LocalContentColor provides colors.clockDialContentColor(false)
            ) {
                repeat(screen.size) { index ->
                    val outerValue =
                        if (!state.is24hour) {
                            screen[index]
                        } else {
                            screen[index] % 12
                        }
                    ClockText(
                        modifier = Modifier.semantics { traversalIndex = index.toFloat() },
                        state = state,
                        value = outerValue,
                    )
                }

                if (state.is24hour) {
                    CircularLayout(
                        modifier =
                        Modifier
                            .layoutId(LayoutId.InnerCircle)
                            .size(TimePickerTokens.ClockDialContainerSize)
                            .background(shape = CircleShape, color = Color.Transparent),
                        radius = InnerCircleRadius
                    ) {
                        repeat(ExtraHours.size) { index ->
                            val innerValue = ExtraHours[index]
                            ClockText(
                                modifier =
                                Modifier.semantics { traversalIndex = 12 + index.toFloat() },
                                state = state,
                                value = innerValue
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
private fun Modifier.drawSelector(
    state: AnalogTimePickerState,
    colors: TimePickerColors,
): Modifier =
    this.drawWithContent {
        val selectorOffsetPx = Offset(state.selectorPos.x.toPx(), state.selectorPos.y.toPx())

        val selectorRadius = TimePickerTokens.ClockDialSelectorHandleContainerSize.toPx() / 2
        val selectorColor = colors.selectorColor

        // clear out the selector section
        drawCircle(
            radius = selectorRadius,
            center = selectorOffsetPx,
            color = Color.Black,
            blendMode = BlendMode.Clear,
        )

        // draw the text composable
        drawContent()

        // draw the selector and clear out the numbers overlapping
        drawCircle(
            radius = selectorRadius,
            center = selectorOffsetPx,
            color = selectorColor,
            blendMode = BlendMode.Xor
        )

        val strokeWidth = TimePickerTokens.ClockDialSelectorTrackContainerWidth.toPx()
        val lineLength =
            selectorOffsetPx.minus(
                Offset(
                    (selectorRadius * cos(state.currentAngle)),
                    (selectorRadius * sin(state.currentAngle))
                )
            )

        // draw the selector line
        drawLine(
            start = size.center,
            strokeWidth = strokeWidth,
            end = lineLength,
            color = selectorColor,
            blendMode = BlendMode.SrcOver
        )

        // draw the selector small dot
        drawCircle(
            radius = TimePickerTokens.ClockDialSelectorCenterContainerSize.toPx() / 2,
            center = size.center,
            color = selectorColor,
        )

        // draw the portion of the number that was overlapping
        drawCircle(
            radius = selectorRadius,
            center = selectorOffsetPx,
            color = colors.clockDialContentColor(selected = true),
            blendMode = BlendMode.DstOver
        )
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClockText(
    modifier: Modifier,
    state: AnalogTimePickerState,
    value: Int
) {
    val style = TimePickerTokens.ClockDialLabelTextFont.value
    val maxDist = with(LocalDensity.current) { MaxDistance.toPx() }
    var center by remember { mutableStateOf(Offset.Zero) }
    var parentCenter by remember { mutableStateOf(IntOffset.Zero) }
    val scope = rememberCoroutineScope()

    val text = value.toLocalString()
    val selected = state.hour.toLocalString() == text

    Box(
        contentAlignment = Alignment.Center,
        modifier =
        modifier
            .minimumInteractiveComponentSize()
            .size(MinimumInteractiveSize)
            .onGloballyPositioned {
                parentCenter = it.parentCoordinates?.size?.center ?: IntOffset.Zero
                center = it.boundsInParent().center
            }
            .focusable()
            .semantics(mergeDescendants = true) {
                onClick {
                    scope.launch {
                        state.onTap(
                            center.x,
                            center.y,
                            maxDist,
                            parentCenter
                        )
                    }
                    true
                }
                this.selected = selected
            }
    ) {
        Text(
            modifier =
            Modifier.clearAndSetSemantics { this.contentDescription = contentDescription },
            text = text,
            style = style,
        )
    }
}

/** Distribute elements evenly on a circle of [radius] */
@Composable
private fun CircularLayout(
    modifier: Modifier = Modifier,
    radius: Dp,
    content: @Composable () -> Unit,
) {
    Layout(modifier = modifier, content = content) { measurables, constraints ->
        val radiusPx = radius.toPx()
        val itemConstraints = constraints.copy(minWidth = 0, minHeight = 0)
        val placeables =
            measurables
                .fastFilter {
                    it.layoutId != LayoutId.Selector && it.layoutId != LayoutId.InnerCircle
                }
                .fastMap { measurable -> measurable.measure(itemConstraints) }
        val selectorMeasurable = measurables.fastFirstOrNull { it.layoutId == LayoutId.Selector }
        val innerMeasurable = measurables.fastFirstOrNull { it.layoutId == LayoutId.InnerCircle }
        val theta = FullCircle / (placeables.count())
        val selectorPlaceable = selectorMeasurable?.measure(itemConstraints)
        val innerCirclePlaceable = innerMeasurable?.measure(itemConstraints)

        layout(
            width = constraints.minWidth,
            height = constraints.minHeight,
        ) {
            selectorPlaceable?.place(0, 0)

            placeables.fastForEachIndexed { i, it ->
                val centerOffsetX = constraints.maxWidth / 2 - it.width / 2
                val centerOffsetY = constraints.maxHeight / 2 - it.height / 2
                val offsetX = radiusPx * cos(theta * i - QuarterCircle) + centerOffsetX
                val offsetY = radiusPx * sin(theta * i - QuarterCircle) + centerOffsetY
                it.place(x = offsetX.roundToInt(), y = offsetY.roundToInt())
            }

            innerCirclePlaceable?.place(
                (constraints.minWidth - innerCirclePlaceable.width) / 2,
                (constraints.minHeight - innerCirclePlaceable.height) / 2
            )
        }
    }
}

private fun dist(x1: Float, y1: Float, x2: Int, y2: Int): Float {
    val x = x2 - x1
    val y = y2 - y1
    return hypot(x.toDouble(), y.toDouble()).toFloat()
}

private fun atan(y: Float, x: Float): Float {
    val ret = atan2(y, x) - QuarterCircle.toFloat()
    return if (ret < 0) ret + FullCircle else ret
}

private enum class LayoutId {
    Selector,
    InnerCircle,
}

private const val FullCircle: Float = (PI * 2).toFloat()
private const val HalfCircle: Float = FullCircle / 2f
private const val QuarterCircle = PI / 2
private const val RadiansPerHour: Float = FullCircle / 12f
private const val SeparatorZIndex = 2f

private val OuterCircleSizeRadius = 101.dp
private val InnerCircleRadius = 69.dp
private val ClockFaceBottomMargin = 24.dp
private val ClockDisplayBottomMargin = 24.dp

private val MaxDistance = 74.dp
private val MinimumInteractiveSize = 48.dp
private val Hours = intListOf(12, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11)
private val ExtraHours: IntList =
    MutableIntList(Hours.size).apply { Hours.forEach { add((it % 12 + 12)) } }

private class VisibleModifier(val visible: Boolean, inspectorInfo: InspectorInfo.() -> Unit) :
    LayoutModifier, InspectorValueInfo(inspectorInfo) {

    override fun MeasureScope.measure(
        measurable: Measurable,
        constraints: Constraints
    ): MeasureResult {
        val placeable = measurable.measure(constraints)

        if (!visible) {
            return layout(0, 0) {}
        }
        return layout(placeable.width, placeable.height) { placeable.place(0, 0) }
    }

    override fun hashCode(): Int = visible.hashCode()

    override fun equals(other: Any?): Boolean {
        val otherModifier = other as? VisibleModifier ?: return false
        return visible == otherModifier.visible
    }
}

private fun Int.toLocalString(
    minDigits: Int = 1,
    maxDigits: Int = 40,
    isGroupingUsed: Boolean = false
): String {
    return getCachedDateTimeFormatter(
        minDigits = minDigits,
        maxDigits = maxDigits,
        isGroupingUsed = isGroupingUsed
    )
        .format(this)
}

private val cachedFormatters = WeakHashMap<String, NumberFormat>()

private fun getCachedDateTimeFormatter(
    minDigits: Int,
    maxDigits: Int,
    isGroupingUsed: Boolean
): NumberFormat {
    // Note: Using Locale.getDefault() as a best effort to obtain a unique key and keeping this
    // function non-composable.
    val key = "$minDigits.$maxDigits.$isGroupingUsed.${Locale.getDefault().toLanguageTag()}"
    return cachedFormatters.getOrPut(key) {
        NumberFormat.getIntegerInstance().apply {
            this.isGroupingUsed = isGroupingUsed
            this.minimumIntegerDigits = minDigits
            this.maximumIntegerDigits = maxDigits
        }
    }
}

@Stable
private fun ColorScheme.fromToken(value: ColorSchemeKeyTokens): Color {
    return when (value) {
        ColorSchemeKeyTokens.OnPrimary -> onPrimary
        ColorSchemeKeyTokens.OnPrimaryContainer -> onPrimaryContainer
        ColorSchemeKeyTokens.OnSurface -> onSurface
        ColorSchemeKeyTokens.OnSurfaceVariant -> onSurfaceVariant
        ColorSchemeKeyTokens.OnTertiaryContainer -> onTertiaryContainer
        ColorSchemeKeyTokens.Outline -> outline
        ColorSchemeKeyTokens.Primary -> primary
        ColorSchemeKeyTokens.PrimaryContainer -> primaryContainer
        ColorSchemeKeyTokens.SurfaceContainerHigh -> surfaceContainerHigh
        ColorSchemeKeyTokens.SurfaceContainerHighest -> surfaceContainerHighest
        ColorSchemeKeyTokens.TertiaryContainer -> tertiaryContainer
        else -> Color.Unspecified
    }
}

private enum class ShapeKeyTokens {
    CornerSmall
}

private fun Shapes.fromToken(value: ShapeKeyTokens): Shape {
    return when (value) {
        ShapeKeyTokens.CornerSmall -> small
    }
}

private val ShapeKeyTokens.value: Shape
    @Composable @ReadOnlyComposable get() = MaterialTheme.shapes.fromToken(this)

internal fun CornerBasedShape.start(): CornerBasedShape {
    return copy(topEnd = CornerSize(0.0.dp), bottomEnd = CornerSize(0.0.dp))
}

/** Helper function for component shape tokens. Used to grab the end values of a shape parameter. */
internal fun CornerBasedShape.end(): CornerBasedShape {
    return copy(topStart = CornerSize(0.0.dp), bottomStart = CornerSize(0.0.dp))
}

private object TimePickerTokens {
    val ClockDialColor = ColorSchemeKeyTokens.SurfaceContainerHighest
    val ClockDialContainerSize = 256.0.dp
    val ClockDialLabelTextFont = TypographyKeyTokens.BodyLarge
    val ClockDialSelectedLabelTextColor = ColorSchemeKeyTokens.OnPrimary
    val ClockDialSelectorCenterContainerSize = 8.0.dp
    val ClockDialSelectorHandleContainerColor = ColorSchemeKeyTokens.Primary
    val ClockDialSelectorHandleContainerSize = 48.0.dp
    val ClockDialSelectorTrackContainerWidth = 2.0.dp
    val ClockDialUnselectedLabelTextColor = ColorSchemeKeyTokens.OnSurface
    val ContainerColor = ColorSchemeKeyTokens.SurfaceContainerHigh
    val PeriodSelectorHorizontalContainerHeight = 38.0.dp
    val PeriodSelectorHorizontalContainerWidth = 216.0.dp
    val PeriodSelectorOutlineColor = ColorSchemeKeyTokens.Outline
    val PeriodSelectorOutlineWidth = 1.0.dp
    val PeriodSelectorContainerShape = ShapeKeyTokens.CornerSmall
    val PeriodSelectorSelectedContainerColor = ColorSchemeKeyTokens.TertiaryContainer
    val PeriodSelectorSelectedLabelTextColor = ColorSchemeKeyTokens.OnTertiaryContainer
    val PeriodSelectorUnselectedLabelTextColor = ColorSchemeKeyTokens.OnSurfaceVariant
    val TimeSelectorSelectedContainerColor = ColorSchemeKeyTokens.PrimaryContainer
    val TimeSelectorSelectedLabelTextColor = ColorSchemeKeyTokens.OnPrimaryContainer
    val TimeSelectorUnselectedContainerColor = ColorSchemeKeyTokens.SurfaceContainerHighest
    val TimeSelectorUnselectedLabelTextColor = ColorSchemeKeyTokens.OnSurface
}

private enum class ColorSchemeKeyTokens {
    OnPrimary,
    OnPrimaryContainer,
    OnSurface,
    OnSurfaceVariant,
    OnTertiaryContainer,
    Outline,
    Primary,
    PrimaryContainer,
    SurfaceContainerHigh,
    SurfaceContainerHighest,
    TertiaryContainer,
}

internal enum class TypographyKeyTokens {
    BodyLarge,
    DisplayLarge,
    LabelMedium,
    TitleMedium,
}

private fun Typography.fromToken(value: TypographyKeyTokens): TextStyle {
    return when (value) {
        TypographyKeyTokens.DisplayLarge -> displayLarge
        TypographyKeyTokens.TitleMedium -> titleMedium
        TypographyKeyTokens.BodyLarge -> bodyLarge
        TypographyKeyTokens.LabelMedium -> labelMedium
    }
}

private val TypographyKeyTokens.value: TextStyle
    @Composable @ReadOnlyComposable get() = MaterialTheme.typography.fromToken(this)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HorizontalPeriodToggle(
    modifier: Modifier,
    state: TimePickerState,
    colors: TimePickerColors,
) {
    val measurePolicy = remember {
        MeasurePolicy { measurables, constraints ->
            val spacer = measurables.fastFirst { it.layoutId == "Spacer" }
            val spacerPlaceable =
                spacer.measure(
                    constraints.copy(
                        minWidth = 0,
                        maxWidth = TimePickerTokens.PeriodSelectorOutlineWidth.roundToPx(),
                    )
                )

            val items =
                measurables
                    .fastFilter { it.layoutId != "Spacer" }
                    .fastMap { item ->
                        item.measure(
                            constraints.copy(minWidth = 0, maxWidth = constraints.maxWidth / 2)
                        )
                    }

            layout(constraints.maxWidth, constraints.maxHeight) {
                items[0].place(0, 0)
                items[1].place(items[0].width, 0)
                spacerPlaceable.place(items[0].width - spacerPlaceable.width / 2, 0)
            }
        }
    }

    val shape = PeriodSelectorContainerShape.value as CornerBasedShape

    PeriodToggleImpl(
        modifier = modifier,
        state = state,
        colors = colors,
        measurePolicy = measurePolicy,
        startShape = shape.start(),
        endShape = shape.end()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PeriodToggleImpl(
    modifier: Modifier,
    state: TimePickerState,
    colors: TimePickerColors,
    measurePolicy: MeasurePolicy,
    startShape: Shape,
    endShape: Shape,
) {
    val borderStroke =
        BorderStroke(TimePickerTokens.PeriodSelectorOutlineWidth, colors.periodSelectorBorderColor)

    val shape = PeriodSelectorContainerShape.value as CornerBasedShape
    Layout(
        modifier =
        modifier
            .semantics {
                isTraversalGroup = true
                this.contentDescription = contentDescription
            }
            .selectableGroup()
            .border(border = borderStroke, shape = shape),
        measurePolicy = measurePolicy,
        content = {
            ToggleItem(
                checked = !state.isAfternoon,
                shape = startShape,
                onClick = { state.isAfternoon = false },
                colors = colors,
            ) {
                Text(text = "AM")
            }
            Spacer(
                Modifier
                    .layoutId("Spacer")
                    .zIndex(SeparatorZIndex)
                    .fillMaxSize()
                    .background(color = colors.periodSelectorBorderColor)
            )
            ToggleItem(
                checked = state.isAfternoon,
                shape = endShape,
                onClick = { state.isAfternoon = true },
                colors = colors,
            ) {
                Text("PM")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ToggleItem(
    checked: Boolean,
    shape: Shape,
    onClick: () -> Unit,
    colors: TimePickerColors,
    content: @Composable RowScope.() -> Unit,
) {
    val contentColor = colors.periodSelectorContentColor(checked)
    val containerColor = colors.periodSelectorContainerColor(checked)

    TextButton(
        modifier =
        Modifier
            .zIndex(if (checked) 0f else 1f)
            .fillMaxSize()
            .semantics { selected = checked },
        contentPadding = PaddingValues(0.dp),
        shape = shape,
        onClick = onClick,
        content = content,
        colors =
        ButtonDefaults.textButtonColors(
            contentColor = contentColor,
            containerColor = containerColor
        )
    )
}