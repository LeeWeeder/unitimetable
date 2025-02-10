package com.leeweeder.timetable.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import androidx.compose.ui.graphics.Color
import com.google.android.material.color.utilities.Hct
import com.google.android.material.color.utilities.Scheme
import kotlin.random.Random

data class Hue(
    val value: Int
) {
    @SuppressLint("RestrictedApi")
    fun createScheme(isDarkTheme: Boolean): Scheme {
        return createScheme(
            hctToArgb(value),
            isDarkTheme
        )
    }

    init {
        require(value == UNSPECIFIED_VALUE || value in 0..MAX_HUE_DEGREES) {
            "Color value is $value. Must be between 0 and $MAX_HUE_DEGREES, or UNSPECIFIED"
        }
    }

    companion object {
        private const val UNSPECIFIED_VALUE = -1
        val UNSPECIFIED = Hue(UNSPECIFIED_VALUE)
        const val MAX_HUE_DEGREES = 360
    }
}

fun Int.toColor() = Color(this)

private const val DEFAULT_TONE = 40.0
private const val DEFAULT_CHROMA = 48.0

@SuppressLint("RestrictedApi")
private fun createScheme(color: Int, isDarkTheme: Boolean): Scheme {
    return if (isDarkTheme) {
        Scheme.dark(color)
    } else {
        Scheme.light(color)
    }
}

@SuppressLint("RestrictedApi")
private fun hctToArgb(hue: Int): Int {
    return Hct.from(hue.toDouble(), DEFAULT_CHROMA, DEFAULT_TONE).toInt()
}

@SuppressLint("RestrictedApi")
fun randomHue(): Hue {
    val random = Random(System.currentTimeMillis())
    val randomHue = random.nextInt(until = Hue.MAX_HUE_DEGREES)
    return Hue(randomHue)
}

fun isSystemInDarkTheme(context: Context): Boolean {
    val uiMode = context.theme.resources.configuration.uiMode
    return (uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
}