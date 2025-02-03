package com.leeweeder.timetable.ui.components

import android.annotation.SuppressLint
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.leeweeder.timetable.R
import com.leeweeder.timetable.data.source.instructor.Instructor
import com.leeweeder.timetable.data.source.subject.Subject
import com.leeweeder.timetable.feature_color_picker.ColorPickerDialog
import com.leeweeder.timetable.feature_color_picker.DefaultChromaValue
import com.leeweeder.timetable.feature_color_picker.DefaultToneValue
import com.leeweeder.timetable.feature_color_picker.MaxHueDegrees
import com.leeweeder.timetable.feature_color_picker.hctToColor
import com.leeweeder.timetable.ui.timetable_setup.components.CancelTextButton
import com.leeweeder.timetable.ui.timetable_setup.components.TextButton
import com.leeweeder.timetable.util.createScheme
import com.leeweeder.timetable.util.toColor
import kotlinx.coroutines.FlowPreview
import kotlin.random.Random

@Composable
fun NewSubjectDialog(
    state: SubjectDialogState,
    instructors: List<Instructor>,
    onConfirmClick: (Subject, Instructor) -> Unit
) {
    BaseSubjectDialog(
        state = state,
        title = {
            Text("New subject")
        },
        instructors = instructors,
        actionButtons = { isError ->
            CancelTextButton(onClick = {
                state.hide()
            })
            OkayTextButton(onClick = {
                onConfirmClick(
                    Subject(
                        id = state.id,
                        description = state.description,
                        code = state.code,
                        color = state.color.toArgb(),
                        instructorId = if (state.instructor.id == 0) null else state.instructor.id
                    ),
                    state.instructor
                )
            }, enabled = !state.isFormInvalid)
        }
    )
}

@Composable
fun EditScheduleDialog(
    state: SubjectDialogState,
    instructors: List<Instructor>,
    onConfirmClick: (Subject, Instructor) -> Unit,
    onDeleteSubjectClick: (Subject) -> Unit,
    onScheduleClick: (Subject) -> Unit
) {
    var isDeleteConfirmationDialogVisible by remember { mutableStateOf(false) }

    AnimatedVisibility(isDeleteConfirmationDialogVisible) {
        AlertDialog(onDismissRequest = {
            isDeleteConfirmationDialogVisible = false
        }, confirmButton = {
            TextButton("Delete", color = MaterialTheme.colorScheme.error, onClick = {
                onDeleteSubjectClick(
                    Subject(
                        id = state.id,
                        description = state.description,
                        code = state.code,
                        color = state.color.toArgb(),
                        instructorId = if (state.instructor.id == 0) null else state.instructor.id
                    )
                )
            })
        }, dismissButton = {
            CancelTextButton(onClick = {
                isDeleteConfirmationDialogVisible = false
            })
        }, title = {
            Text("Delete subject?")
        }, icon = {
            Icon(
                painter = painterResource(R.drawable.delete_24px),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        }, text = {
            Text("You can still undo th")
        })
    }
    BaseSubjectDialog(
        state = state,
        title = {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Edit subject")
                Box {
                    var expanded by remember { mutableStateOf(false) }

                    IconButton(R.drawable.more_vert_24px, contentDescription = "More options") {
                        expanded = true
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = {
                            expanded = false
                        }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text("Delete subject")
                            },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(R.drawable.delete_24px),
                                    contentDescription = null
                                )
                            },
                            onClick = {
                                isDeleteConfirmationDialogVisible = true
                            }
                        )
                    }
                }
            }
        },
        instructors = instructors,
        actionButtons = { isError ->
            Box(modifier = Modifier.weight(1f)) {
                OutlinedButton(onClick = {
                    onScheduleClick(
                        Subject(
                            id = state.id,
                            code = state.code,
                            description = state.description,
                            color = state.color.toArgb(),
                            instructorId = if (state.instructor.id == 0) null else state.instructor.id
                        ),
                    )
                }) {
                    Text("Schedule")
                }
            }

            CancelTextButton(
                onClick = {
                    state.hide()
                }
            )

            OkayTextButton(onClick = {
                onConfirmClick(
                    Subject(
                        id = state.id,
                        code = state.code,
                        description = state.description,
                        color = state.color.toArgb(),
                        instructorId = if (state.instructor.id == 0) null else state.instructor.id
                    ),
                    state.instructor
                )
            }, enabled = !state.isFormInvalid)
        }
    )
}

@SuppressLint("RestrictedApi")
@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
private fun BaseSubjectDialog(
    state: SubjectDialogState,
    instructors: List<Instructor>,
    title: @Composable () -> Unit,
    actionButtons: @Composable RowScope.(isError: Boolean) -> Unit
) {
    var isColorPickerDialogVisible by remember { mutableStateOf(false) }
    var color by remember { mutableStateOf(state.color) }

    val dismissColorPickerDialog = {
        isColorPickerDialogVisible = false
    }

    AnimatedVisibility(isColorPickerDialogVisible) {
        ColorPickerDialog(
            onDismissRequest = dismissColorPickerDialog,
            initialColor = color,
            onConfirmClick = {
                color = it
                dismissColorPickerDialog()
            },
            title = "Select subject color"
        )
    }

    LaunchedEffect(color) {
        state.setColor(color)
    }

    AnimatedVisibility(state.visible) {
        AlertDialog(
            onDismissRequest = {
                state.hide()
            },
            title = title,
            actionButtons = {
                this.actionButtons(state.isFormInvalid)
            }
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextField(
                    value = state.code,
                    onValueChange = {
                        state.setCode((it))
                        state.startCheckingForCodeError()
                    },
                    label = "Code",
                    iconId = R.drawable.book_24px,
                    isError = state.codeIsError,
                    errorSupportText = "Subject code can't be empty"
                )
                TextField(
                    value = state.description,
                    onValueChange = {
                        state.setDescription(it)
                        state.startCheckingForDescriptionError()
                    },
                    label = "Description",
                    iconId = R.drawable.description_24px,
                    isError = state.descriptionIsError,
                    errorSupportText = "Subject description can't be empty"
                )
                val instructorTextFieldState =
                    rememberTextFieldState(initialText = state.instructor.name)

                val options = instructors.filter { it.name.contains(instructorTextFieldState.text) }

                val (allowExpanded, setAllowExpanded) = remember { mutableStateOf(false) }

                val instructorListMenuExpanded = allowExpanded && options.isNotEmpty()

                LaunchedEffect(instructorTextFieldState.text) {
                    val newInstructorName = instructorTextFieldState.text.toString()

                    for (instructor in instructors) {
                        if (instructor.name == newInstructorName) {
                            state.setInstructor(instructor.copy(name = newInstructorName))
                            break
                        } else {
                            state.setInstructor(Instructor(name = newInstructorName))
                        }
                    }

                    if (instructorTextFieldState.text.isNotEmpty())
                        state.startCheckingForInstructorError()
                }

                ExposedDropdownMenuBox(
                    expanded = instructorListMenuExpanded,
                    onExpandedChange = setAllowExpanded
                ) {
                    OutlinedTextField(
                        state = instructorTextFieldState,
                        label = { Text("Instructor") },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.account_box_24px),
                                contentDescription = null
                            )
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded = instructorListMenuExpanded,
                                modifier = Modifier.menuAnchor(
                                    ExposedDropdownMenuAnchorType.SecondaryEditable
                                )
                            )
                        },
                        modifier = Modifier
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable)
                            .fillMaxWidth(),
                        lineLimits = TextFieldLineLimits.SingleLine,
                        isError = state.instructorIsError
                    )

                    ExposedDropdownMenu(expanded = instructorListMenuExpanded, onDismissRequest = {
                        setAllowExpanded(false)
                    }) {
                        options.forEach {
                            DropdownMenuItem(
                                text = { Text(it.name) },
                                onClick = {
                                    instructorTextFieldState.setTextAndPlaceCursorAtEnd(it.name)
                                    state.setInstructor(
                                        state.instructor.copy(
                                            id = it.id,
                                            name = it.name
                                        )
                                    )
                                    setAllowExpanded(false)
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                            )
                        }
                    }
                }
                HorizontalDivider(thickness = Dp.Hairline, modifier = Modifier.padding(top = 8.dp))
            }
            ListItem(
                headlineContent = {
                    Text("Color", modifier = Modifier.padding(start = 8.dp))
                },
                trailingContent = {
                    val color = createScheme(state.color, isSystemInDarkTheme()).primary.toColor()
                    Box(
                        modifier = Modifier
                            .background(
                                color = color,
                                shape = MaterialTheme.shapes.extraLarge
                            )
                            .fillMaxWidth(0.5f)
                            .height(36.dp)
                    )
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                modifier = Modifier
                    .clickable(onClick = {
                        isColorPickerDialogVisible = true
                    })
                    .padding(end = 8.dp)
            )
        }
    }
}

@Composable
fun rememberSubjectDialogState(
    visible: Boolean,
    initialCode: String = "",
    initialDescription: String = "",
    initialColor: Color = randomColor(isSystemInDarkTheme()),
    initialInstructor: Instructor = Instructor(name = "")
): SubjectDialogState {
    return rememberSaveable(saver = SubjectDialogState.Saver()) {
        SubjectDialogState(
            id = 0,
            initialVisibility = visible,
            initialCode = initialCode,
            initialDescription = initialDescription,
            initialColor = initialColor,
            initialInstructor = initialInstructor
        )
    }
}

@SuppressLint("RestrictedApi")
private fun randomColor(isDarkTheme: Boolean): Color {
    val random = Random(System.currentTimeMillis())
    val randomHue = random.nextInt(until = MaxHueDegrees)

    val color = hctToColor(randomHue, DefaultChromaValue, DefaultToneValue)

    return createScheme(isDarkTheme = isDarkTheme, color = color).primary.toColor()
}

class SubjectDialogState(
    initialVisibility: Boolean,
    id: Int,
    private val initialCode: String,
    private val initialDescription: String,
    private val initialColor: Color,
    private val initialInstructor: Instructor
) {
    val code: String
        get() = _code

    val description: String
        get() = _description

    val color: Color
        get() = _color

    val instructor: Instructor
        get() = _instructor

    val id: Int
        get() = _id

    val visible: Boolean
        get() = _visible

    private val codeIsEmpty: Boolean
        get() = code.isEmpty()

    private val descriptionIsEmpty: Boolean
        get() = description.isEmpty()

    private val instructorNameIsEmpty: Boolean
        get() = instructor.name.isEmpty()

    val codeIsError: Boolean
        get() = codeIsEmpty && shouldCheckingForCodeError

    val descriptionIsError: Boolean
        get() = descriptionIsEmpty && shouldCheckingForDescriptionError

    val instructorIsError: Boolean
        get() = instructorNameIsEmpty && shouldCheckingForInstructorError

    val isFormInvalid: Boolean
        get() = codeIsEmpty || descriptionIsEmpty || instructorNameIsEmpty

    fun show() {
        _visible = true
    }

    fun hide() {
        _visible = false
    }

    fun init(id: Int, code: String, description: String, color: Color, instructor: Instructor) {
        _id = id
        setCode(code)
        setDescription(description)
        setColor(color)
        setInstructor(instructor)
    }

    fun setColor(value: Color) {
        _color = value
    }

    fun setInstructor(value: Instructor) {
        _instructor = value
    }

    fun setDescription(value: String) {
        _description = value
    }

    fun setCode(value: String) {
        _code = value
    }

    fun startCheckingForCodeError() {
        shouldCheckingForCodeError = true
    }

    fun startCheckingForDescriptionError() {
        shouldCheckingForDescriptionError = true
    }

    fun startCheckingForInstructorError() {
        shouldCheckingForInstructorError = true
    }

    fun reset() {
        _code = initialCode
        _description = initialDescription
        _color = initialColor
        _instructor = initialInstructor
        shouldCheckingForCodeError = false
        shouldCheckingForDescriptionError = false
        shouldCheckingForInstructorError = false
    }

    companion object {
        fun Saver(): Saver<SubjectDialogState, *> {
            return Saver(save = {
                listOf(
                    it.visible,
                    it.id,
                    it.code,
                    it.description,
                    it.color.toArgb(),
                    it.instructor.id,
                    it.instructor.name
                )
            }, restore = {
                SubjectDialogState(
                    initialVisibility = it[0] as Boolean,
                    id = it[1] as Int,
                    initialCode = it[2] as String,
                    initialDescription = it[3] as String,
                    initialColor = (it[4] as Int).toColor(),
                    initialInstructor = Instructor(id = it[5] as Int, name = it[6] as String)
                )
            })
        }
    }

    private var _id by mutableIntStateOf(id)
    private var _code by mutableStateOf(initialCode)
    private var _description by mutableStateOf(initialDescription)
    private var _color by mutableStateOf(initialColor)
    private var _instructor by mutableStateOf(initialInstructor)
    private var _visible by mutableStateOf(initialVisibility)

    private var shouldCheckingForCodeError by mutableStateOf(false)
    private var shouldCheckingForDescriptionError by mutableStateOf(false)
    private var shouldCheckingForInstructorError by mutableStateOf(false)
}

@Composable
private fun TextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    @DrawableRes iconId: Int,
    modifier: Modifier = Modifier,
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors(),
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean,
    singleLine: Boolean = true,
    errorSupportText: String
) {
    OutlinedTextField(
        value = value,
        modifier = modifier.fillMaxWidth(),
        onValueChange = onValueChange,
        label = {
            Text(label)
        },
        leadingIcon = {
            Icon(painter = painterResource(iconId), contentDescription = null)
        },
        trailingIcon = trailingIcon,
        colors = colors,
        singleLine = singleLine,
        isError = isError,
        supportingText = {
            AnimatedVisibility(isError) {
                Text(errorSupportText)
            }
        }
    )
}

@Preview
@Composable
private fun NewSubjectDialogPreview() {
    NewSubjectDialog(
        state = rememberSubjectDialogState(true),
        onConfirmClick = { _, _ -> },
        instructors = emptyList()
    )
}

@Preview
@Composable
private fun EditSubjectDialogPreview() {
    EditScheduleDialog(
        onConfirmClick = { _, _ -> },
        onScheduleClick = {},
        state = rememberSubjectDialogState(true),
        onDeleteSubjectClick = {},
        instructors = emptyList()
    )
}