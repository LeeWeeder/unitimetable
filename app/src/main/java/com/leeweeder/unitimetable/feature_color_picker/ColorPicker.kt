package com.leeweeder.unitimetable.feature_color_picker

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.leeweeder.unitimetable.R
import com.leeweeder.unitimetable.ui.timetable_setup.components.CancelTextButton
import com.leeweeder.unitimetable.ui.timetable_setup.components.TextButton
import com.leeweeder.unitimetable.util.Hue
import com.leeweeder.unitimetable.util.randomHue
import com.leeweeder.unitimetable.util.toColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@SuppressLint("RestrictedApi")
@Composable
fun ColorPickerDialog(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    initialHue: Hue,
    onConfirmClick: (Hue) -> Unit
) {
    var hue by remember(visible, initialHue) { mutableStateOf(initialHue) }

    AnimatedVisibility(visible) {
        AlertDialog(
            onDismissRequest = onDismissRequest,
            confirmButton = {
                TextButton(
                    "Confirm",
                    onClick = {
                        onConfirmClick(hue)
                    }
                )
            }, dismissButton = {
                CancelTextButton(onClick = onDismissRequest)
            }, title = {
                Text("Select color")
            }, text = {
                ColorPicker(hue = hue) {
                    hue = it
                }
            }, icon = {
                Icon(
                    painter = painterResource(R.drawable.palette_24px),
                    contentDescription = "Selected color",
                    tint = hue.createScheme(isSystemInDarkTheme()).primary.toColor(),
                    modifier = Modifier
                        .size(36.dp)
                )
            }
        )
    }
}

@Preview
@Composable
private fun ColorPickerDialogPreview() {
    ColorPickerDialog(
        visible = true,
        onDismissRequest = {},
        initialHue = randomHue(),
        onConfirmClick = {},
    )
}

@SuppressLint("RestrictedApi")
@Composable
private fun ColorPicker(hue: Hue, onHueChange: (Hue) -> Unit) {
    val isDarkTheme = isSystemInDarkTheme()

    val colors = List(Hue.MAX_HUE_DEGREES) {
        Hue(it).createScheme(isDarkTheme).primary.toColor()
    }

    var value by remember { mutableFloatStateOf(0f) }

    val density = LocalDensity.current

    val pickerRadius = with(density) { PickerSize.toPx() / 2 }

    var maxWidth by remember { mutableFloatStateOf(0f) }

    var offsetX by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(maxWidth) {
        if (maxWidth > 0 && offsetX == 0f) {
            offsetX = (hue.value.toFloat() / Hue.MAX_HUE_DEGREES * maxWidth) - pickerRadius
        }
    }

    val interactionSource = remember { MutableInteractionSource() }

    val scope = rememberCoroutineScope()

    fun onHueChange() {
        onHueChange(
            Hue(
                (offsetX / maxWidth * colors.size.toFloat()).coerceIn(
                    0f,
                    colors.size.toFloat() - 1
                ).roundToInt()
            )
        )
    }

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
                        onHueChange()
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

        Picker(
            selectorBoxWidth = this.maxWidth,
            offsetX = offsetX,
            onDrag = { delta ->
                offsetX = delta.coerceIn(-pickerRadius, maxWidth - pickerRadius)
                onHueChange()
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

@Preview
@Composable
private fun ColorPickerPreview() {
    ColorPicker(hue = Hue(1), onHueChange = {})
}


