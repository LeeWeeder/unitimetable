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

package com.leeweeder.unitimetable.ui.timetable_setup.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidViewBinding
import com.leeweeder.unitimetable.databinding.NumberOfDaysSliderBinding
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