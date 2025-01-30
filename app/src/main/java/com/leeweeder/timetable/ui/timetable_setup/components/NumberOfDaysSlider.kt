package com.leeweeder.timetable.ui.timetable_setup.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidViewBinding
import com.leeweeder.timetable.databinding.NumberOfDaysSliderBinding
import java.time.DayOfWeek
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NumberOfDaysSlider(
    value: Int,
    onValueChange: (newValue: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    AndroidViewBinding(NumberOfDaysSliderBinding::inflate, modifier = modifier) {
        numberOfDaysSlider.value = value.toFloat()
        numberOfDaysSlider.valueFrom = 1f
        numberOfDaysSlider.valueTo = DayOfWeek.entries.size.toFloat()
        numberOfDaysSlider.stepSize = 1f
        numberOfDaysSlider.addOnChangeListener { slider, value, fromUser ->
            onValueChange(value.roundToInt())
        }
    }
}