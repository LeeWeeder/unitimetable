package com.leeweeder.timetable.util

import android.annotation.SuppressLint
import androidx.annotation.IntRange
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.glance.text.FontFamily
import androidx.glance.text.FontStyle
import androidx.glance.text.FontWeight
import androidx.glance.unit.ColorProvider

fun formatTime(@IntRange(from = 0L, to = 23L) time: Int): String {
    return "$time:00"
}

@SuppressLint("RestrictedApi")
fun TextStyle.toGlanceTextStyle(
    color: Color = this.color
): androidx.glance.text.TextStyle {
    return androidx.glance.text.TextStyle(
        color = ColorProvider(color),
        fontSize = this.fontSize,
        fontWeight = if (this.fontWeight == null) {
            null
        } else if (this.fontWeight!!.weight <= FontWeight.Normal.value) {
            FontWeight.Normal
        } else if (this.fontWeight!!.weight <= FontWeight.Medium.value) {
            FontWeight.Medium
        } else {
            FontWeight.Bold
        },
        fontStyle = if (this.fontStyle == null) {
            null
        } else if (this.fontStyle == androidx.compose.ui.text.font.FontStyle.Normal) {
            FontStyle.Normal
        } else {
            FontStyle.Italic
        },
        textAlign = if (this.textAlign == TextAlign.Center) {
            androidx.glance.text.TextAlign.Center
        } else if (this.textAlign == TextAlign.Right) {
            androidx.glance.text.TextAlign.Right
        } else if (this.textAlign == TextAlign.End) {
            androidx.glance.text.TextAlign.End
        } else if (this.textAlign == TextAlign.Start) {
            androidx.glance.text.TextAlign.Start
        } else if (this.textAlign == TextAlign.Unspecified) {
            null
        } else {
            androidx.glance.text.TextAlign.Left
        },
        textDecoration = if (this.textDecoration == TextDecoration.LineThrough) {
            androidx.glance.text.TextDecoration.LineThrough
        } else if (this.textDecoration == TextDecoration.Underline) {
            androidx.glance.text.TextDecoration.Underline
        } else {
            androidx.glance.text.TextDecoration.None
        },
        fontFamily = if (this.fontFamily == androidx.compose.ui.text.font.FontFamily.Serif) {
            FontFamily.Serif
        } else if (this.fontFamily == androidx.compose.ui.text.font.FontFamily.Monospace) {
            FontFamily.Monospace
        } else if (this.fontFamily == androidx.compose.ui.text.font.FontFamily.Cursive) {
            FontFamily.Cursive
        } else if (this.fontFamily == androidx.compose.ui.text.font.FontFamily.Default) {
            null
        } else {
            FontFamily.SansSerif
        }
    )
}