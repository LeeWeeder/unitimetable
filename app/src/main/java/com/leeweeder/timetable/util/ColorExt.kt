package com.leeweeder.timetable.util

import android.annotation.SuppressLint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.google.android.material.color.utilities.Scheme

fun Int.toColor() = Color(this)

@SuppressLint("RestrictedApi")
fun createScheme(color: Color, isDarkTheme: Boolean): Scheme {
    val argb = color.toArgb()
    return if (isDarkTheme) {
        Scheme.dark(argb)
    } else {
        Scheme.light(argb)
    }
}