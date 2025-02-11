package com.leeweeder.timetable.feature_widget.ui.theme

import android.os.Build
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.glance.GlanceTheme
import com.leeweeder.timetable.ui.theme.backgroundDark
import com.leeweeder.timetable.ui.theme.backgroundLight
import com.leeweeder.timetable.ui.theme.errorContainerDark
import com.leeweeder.timetable.ui.theme.errorContainerLight
import com.leeweeder.timetable.ui.theme.errorDark
import com.leeweeder.timetable.ui.theme.errorLight
import com.leeweeder.timetable.ui.theme.inverseOnSurfaceDark
import com.leeweeder.timetable.ui.theme.inverseOnSurfaceLight
import com.leeweeder.timetable.ui.theme.inversePrimaryDark
import com.leeweeder.timetable.ui.theme.inversePrimaryLight
import com.leeweeder.timetable.ui.theme.inverseSurfaceDark
import com.leeweeder.timetable.ui.theme.inverseSurfaceLight
import com.leeweeder.timetable.ui.theme.onBackgroundDark
import com.leeweeder.timetable.ui.theme.onBackgroundLight
import com.leeweeder.timetable.ui.theme.onErrorContainerDark
import com.leeweeder.timetable.ui.theme.onErrorContainerLight
import com.leeweeder.timetable.ui.theme.onErrorDark
import com.leeweeder.timetable.ui.theme.onErrorLight
import com.leeweeder.timetable.ui.theme.onPrimaryContainerDark
import com.leeweeder.timetable.ui.theme.onPrimaryContainerLight
import com.leeweeder.timetable.ui.theme.onPrimaryDark
import com.leeweeder.timetable.ui.theme.onPrimaryLight
import com.leeweeder.timetable.ui.theme.onSecondaryContainerDark
import com.leeweeder.timetable.ui.theme.onSecondaryContainerLight
import com.leeweeder.timetable.ui.theme.onSecondaryDark
import com.leeweeder.timetable.ui.theme.onSecondaryLight
import com.leeweeder.timetable.ui.theme.onSurfaceDark
import com.leeweeder.timetable.ui.theme.onSurfaceLight
import com.leeweeder.timetable.ui.theme.onSurfaceVariantDark
import com.leeweeder.timetable.ui.theme.onSurfaceVariantLight
import com.leeweeder.timetable.ui.theme.onTertiaryContainerDark
import com.leeweeder.timetable.ui.theme.onTertiaryContainerLight
import com.leeweeder.timetable.ui.theme.onTertiaryDark
import com.leeweeder.timetable.ui.theme.onTertiaryLight
import com.leeweeder.timetable.ui.theme.outlineDark
import com.leeweeder.timetable.ui.theme.outlineLight
import com.leeweeder.timetable.ui.theme.outlineVariantDark
import com.leeweeder.timetable.ui.theme.outlineVariantLight
import com.leeweeder.timetable.ui.theme.primaryContainerDark
import com.leeweeder.timetable.ui.theme.primaryContainerLight
import com.leeweeder.timetable.ui.theme.primaryDark
import com.leeweeder.timetable.ui.theme.primaryLight
import com.leeweeder.timetable.ui.theme.scrimDark
import com.leeweeder.timetable.ui.theme.scrimLight
import com.leeweeder.timetable.ui.theme.secondaryContainerDark
import com.leeweeder.timetable.ui.theme.secondaryContainerLight
import com.leeweeder.timetable.ui.theme.secondaryDark
import com.leeweeder.timetable.ui.theme.secondaryLight
import com.leeweeder.timetable.ui.theme.surfaceBrightDark
import com.leeweeder.timetable.ui.theme.surfaceBrightLight
import com.leeweeder.timetable.ui.theme.surfaceContainerDark
import com.leeweeder.timetable.ui.theme.surfaceContainerHighDark
import com.leeweeder.timetable.ui.theme.surfaceContainerHighLight
import com.leeweeder.timetable.ui.theme.surfaceContainerHighestDark
import com.leeweeder.timetable.ui.theme.surfaceContainerHighestLight
import com.leeweeder.timetable.ui.theme.surfaceContainerLight
import com.leeweeder.timetable.ui.theme.surfaceContainerLowDark
import com.leeweeder.timetable.ui.theme.surfaceContainerLowLight
import com.leeweeder.timetable.ui.theme.surfaceContainerLowestDark
import com.leeweeder.timetable.ui.theme.surfaceContainerLowestLight
import com.leeweeder.timetable.ui.theme.surfaceDark
import com.leeweeder.timetable.ui.theme.surfaceDimDark
import com.leeweeder.timetable.ui.theme.surfaceDimLight
import com.leeweeder.timetable.ui.theme.surfaceLight
import com.leeweeder.timetable.ui.theme.surfaceVariantDark
import com.leeweeder.timetable.ui.theme.surfaceVariantLight
import com.leeweeder.timetable.ui.theme.tertiaryContainerDark
import com.leeweeder.timetable.ui.theme.tertiaryContainerLight
import com.leeweeder.timetable.ui.theme.tertiaryDark
import com.leeweeder.timetable.ui.theme.tertiaryLight

private val lightScheme = lightColorScheme(
    primary = primaryLight,
    onPrimary = onPrimaryLight,
    primaryContainer = primaryContainerLight,
    onPrimaryContainer = onPrimaryContainerLight,
    secondary = secondaryLight,
    onSecondary = onSecondaryLight,
    secondaryContainer = secondaryContainerLight,
    onSecondaryContainer = onSecondaryContainerLight,
    tertiary = tertiaryLight,
    onTertiary = onTertiaryLight,
    tertiaryContainer = tertiaryContainerLight,
    onTertiaryContainer = onTertiaryContainerLight,
    error = errorLight,
    onError = onErrorLight,
    errorContainer = errorContainerLight,
    onErrorContainer = onErrorContainerLight,
    background = backgroundLight,
    onBackground = onBackgroundLight,
    surface = surfaceLight,
    onSurface = onSurfaceLight,
    surfaceVariant = surfaceVariantLight,
    onSurfaceVariant = onSurfaceVariantLight,
    outline = outlineLight,
    outlineVariant = outlineVariantLight,
    scrim = scrimLight,
    inverseSurface = inverseSurfaceLight,
    inverseOnSurface = inverseOnSurfaceLight,
    inversePrimary = inversePrimaryLight,
    surfaceDim = surfaceDimLight,
    surfaceBright = surfaceBrightLight,
    surfaceContainerLowest = surfaceContainerLowestLight,
    surfaceContainerLow = surfaceContainerLowLight,
    surfaceContainer = surfaceContainerLight,
    surfaceContainerHigh = surfaceContainerHighLight,
    surfaceContainerHighest = surfaceContainerHighestLight,
)

private val darkScheme = darkColorScheme(
    primary = primaryDark,
    onPrimary = onPrimaryDark,
    primaryContainer = primaryContainerDark,
    onPrimaryContainer = onPrimaryContainerDark,
    secondary = secondaryDark,
    onSecondary = onSecondaryDark,
    secondaryContainer = secondaryContainerDark,
    onSecondaryContainer = onSecondaryContainerDark,
    tertiary = tertiaryDark,
    onTertiary = onTertiaryDark,
    tertiaryContainer = tertiaryContainerDark,
    onTertiaryContainer = onTertiaryContainerDark,
    error = errorDark,
    onError = onErrorDark,
    errorContainer = errorContainerDark,
    onErrorContainer = onErrorContainerDark,
    background = backgroundDark,
    onBackground = onBackgroundDark,
    surface = surfaceDark,
    onSurface = onSurfaceDark,
    surfaceVariant = surfaceVariantDark,
    onSurfaceVariant = onSurfaceVariantDark,
    outline = outlineDark,
    outlineVariant = outlineVariantDark,
    scrim = scrimDark,
    inverseSurface = inverseSurfaceDark,
    inverseOnSurface = inverseOnSurfaceDark,
    inversePrimary = inversePrimaryDark,
    surfaceDim = surfaceDimDark,
    surfaceBright = surfaceBrightDark,
    surfaceContainerLowest = surfaceContainerLowestDark,
    surfaceContainerLow = surfaceContainerLowDark,
    surfaceContainer = surfaceContainerDark,
    surfaceContainerHigh = surfaceContainerHighDark,
    surfaceContainerHighest = surfaceContainerHighestDark,
)

private object UnitimetableWidgetTheme {
    val colors = androidx.glance.material3.ColorProviders(light = lightScheme, dark = darkScheme)
}

@Composable
fun WidgetTheme(
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable() () -> Unit
) {
    val colorScheme = if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        GlanceTheme.colors
    } else UnitimetableWidgetTheme.colors

    GlanceTheme(
        colors = colorScheme,
        content = content
    )
}

