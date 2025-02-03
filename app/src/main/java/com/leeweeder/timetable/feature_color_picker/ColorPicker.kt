package com.leeweeder.timetable.feature_color_picker

import android.annotation.SuppressLint
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.google.android.material.color.utilities.Hct
import com.leeweeder.timetable.R
import com.leeweeder.timetable.ui.timetable_setup.components.CancelTextButton
import com.leeweeder.timetable.ui.timetable_setup.components.TextButton
import com.leeweeder.timetable.util.createScheme
import com.leeweeder.timetable.util.toColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun ColorPickerDialog(
    onDismissRequest: () -> Unit,
    initialColor: Color,
    onConfirmClick: (Color) -> Unit,
    title: String
) {
    var color by remember { mutableStateOf(initialColor) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                "Confirm",
                onClick = {
                    onConfirmClick(color)
                }
            )
        }, dismissButton = {
            CancelTextButton(onDismissRequest)
        }, title = {
            Text(title)
        }, text = {
            ColorPicker(initialColor = color) {
                color = it
            }
        }
    )
}

@SuppressLint("RestrictedApi")
@Composable
fun ColorPicker(initialColor: Color, onColorChange: (Color) -> Unit) {
    val initialHctColor = Hct.fromInt(initialColor.toArgb())

    var hue by remember { mutableIntStateOf(initialHctColor.hue.roundToInt()) }
    var chroma by remember { mutableIntStateOf(initialHctColor.chroma.roundToInt()) }
    var tone by remember { mutableIntStateOf(initialHctColor.tone.roundToInt()) }

    val color by remember {
        derivedStateOf {
            hctToColor(hue, chroma, tone)
        }
    }

    val isDarkMode = isSystemInDarkTheme()

    val displayColor by remember {
        derivedStateOf {
            val scheme = createScheme(color = color, isDarkTheme = isDarkMode)
            scheme.primary.toColor()
        }
    }

    LaunchedEffect(color) {
        onColorChange(color)
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(R.drawable.palette_24px),
            contentDescription = "Selected color",
            tint = displayColor,
            modifier = Modifier
                .size(36.dp)
        )

        HueSelectionBox(
            initialHueDegrees = hue,
            chroma = Hct.fromInt(displayColor.toArgb()).chroma.roundToInt(),
            tone = Hct.fromInt(displayColor.toArgb()).tone.roundToInt(),
            onHueChange = {
                hue = it
            }
        )
    }
}

@SuppressLint("RestrictedApi")
fun hctToColor(hue: Int, chroma: Int, tone: Int): Color {
    return Hct.from(hue.toDouble(), chroma.toDouble(), tone.toDouble()).toInt().toColor()
}

@SuppressLint("RestrictedApi")
@Composable
private fun HueSelectionBox(
    initialHueDegrees: Int,
    onHueChange: (Int) -> Unit,
    chroma: Int,
    tone: Int
) {
    SelectionBox(colors = List(MaxHueDegrees) {
        hctToColor(it, chroma, tone)
    }, initialPositionPx = { maxWidth ->
        (initialHueDegrees.toFloat() / MaxHueDegrees * maxWidth)
    }) {
        onHueChange(it.roundToInt())
    }
}

@Composable
private fun SelectionBox(
    colors: List<Color>,
    initialPositionPx: (maxWidth: Float) -> Float = { 0f },
    onValueChange: (Float) -> Unit
) {
    var value by remember { mutableFloatStateOf(0f) }

    val density = LocalDensity.current

    val pickerRadius = with(density) { PickerSize.toPx() / 2 }

    var maxWidth by remember { mutableFloatStateOf(0f) }

    var offsetX by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(maxWidth) {
        if (maxWidth > 0 && offsetX == 0f) {
            offsetX = initialPositionPx(maxWidth) - pickerRadius
        }
    }

    LaunchedEffect(value) {
        onValueChange(value)
    }

    val interactionSource = remember { MutableInteractionSource() }

    val scope = rememberCoroutineScope()

    BoxWithConstraints(
        modifier = Modifier
            .height(SelectionBoxHeight)
            .fillMaxWidth()
            .drawBehind {
                drawRect(brush = Brush.linearGradient(colors = colors))
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { offset ->
                        val newOffsetX = (offset.x - pickerRadius).coerceIn(
                            -pickerRadius,
                            maxWidth - pickerRadius
                        )

                        offsetX = newOffsetX
                        scope.launch {
                            val pressInteraction = PressInteraction.Press(offset)
                            interactionSource.emit(pressInteraction)
                            delay(200L)
                            interactionSource.emit(PressInteraction.Release(pressInteraction))
                        }
                    })
            }
    ) {
        maxWidth = with(density) {
            this@BoxWithConstraints.maxWidth.toPx()
        }

        value = (offsetX / maxWidth * colors.size.toFloat()).coerceIn(
            0f,
            colors.size.toFloat() - 1
        )

        Picker(
            selectorBoxWidth = this.maxWidth,
            offsetX = offsetX,
            onDrag = { delta ->
                offsetX = delta.coerceIn(-pickerRadius, maxWidth - pickerRadius)
            },
            interactionSource = interactionSource
        )
    }
}

@Composable
private fun BoxWithConstraintsScope.Picker(
    selectorBoxWidth: Dp,
    offsetX: Float,
    onDrag: (Float) -> Unit,
    interactionSource: MutableInteractionSource
) {
    val interactions = remember { mutableStateListOf<Interaction>() }

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is DragInteraction.Start -> {
                    interactions.add(interaction)
                }

                is DragInteraction.Stop -> {
                    interactions.remove(interaction.start)
                }

                is DragInteraction.Cancel -> {
                    interactions.remove(interaction.start)
                }

                is PressInteraction.Press -> {
                    interactions.add(interaction)
                }

                is PressInteraction.Cancel -> {
                    interactions.remove(interaction.press)
                }

                is PressInteraction.Release -> {
                    interactions.remove(interaction.press)
                }
            }
        }
    }

    val isInteracting by remember(interactions) {
        derivedStateOf { interactions.isNotEmpty() }
    }

    val transition = updateTransition(isInteracting)

    val dragIndicatorAnimatedColor by transition.animateColor { interacting ->
        if (interacting) {
            MaterialTheme.colorScheme.scrim.copy(alpha = 0.1f)
        } else Color.Transparent
    }

    val dragIndicatorWidth by transition.animateDp { interacting ->
        if (interacting) PickerSize else 0.dp
    }

    Box(
        modifier = Modifier
            .size(PickerSize)
            .offset {
                val width = selectorBoxWidth.toPx()
                val pickerRadius = PickerSize.toPx() / 2
                IntOffset(
                    offsetX.coerceIn(-pickerRadius, width - pickerRadius).roundToInt(), 0
                )
            }
            .drawBehind {
                drawCircle(color = dragIndicatorAnimatedColor, radius = dragIndicatorWidth.toPx())
            }
            .align(Alignment.CenterStart)
            .minimumInteractiveComponentSize()
            .draggable(
                orientation = Orientation.Horizontal,
                state = rememberDraggableState { delta ->
                    onDrag(offsetX + delta)
                },
                startDragImmediately = true,
                interactionSource = interactionSource
            ),
        contentAlignment = Alignment.Center
    ) {
        Surface(shape = CircleShape, color = Color.White, modifier = Modifier.size(PickerSize)) { }
    }
}

private val PickerSize = 24.dp
private val SelectionBoxHeight = 56.dp

const val DefaultToneValue = 40
const val DefaultChromaValue = 48

const val MaxHueDegrees = 360

@Preview
@Composable
private fun HueSelectionPreview() {
    HueSelectionBox(
        onHueChange = {},
        initialHueDegrees = 0,
        chroma = DefaultChromaValue,
        tone = DefaultToneValue
    )
}

@Preview
@Composable
private fun ColorPickerPreview() {
    ColorPicker(initialColor = MaterialTheme.colorScheme.primary, onColorChange = {})
}


