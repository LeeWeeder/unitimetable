package com.leeweeder.timetable.util

import androidx.compose.ui.graphics.Color

fun Color.toLong() = this.value.toLong()
fun Long.toColor() = Color(this)