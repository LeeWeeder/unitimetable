package com.leeweeder.unitimetable.feature_widget.ui.theme

import android.os.Build
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.glance.GlanceTheme
import com.leeweeder.unitimetable.ui.theme.backgroundDark
import com.leeweeder.unitimetable.ui.theme.backgroundLight
import com.leeweeder.unitimetable.ui.theme.errorContainerDark
import com.leeweeder.unitimetable.ui.theme.errorContainerLight
import com.leeweeder.unitimetable.ui.theme.errorDark
import com.leeweeder.unitimetable.ui.theme.errorLight
import com.leeweeder.unitimetable.ui.theme.inverseOnSurfaceDark
import com.leeweeder.unitimetable.ui.theme.inverseOnSurfaceLight
import com.leeweeder.unitimetable.ui.theme.inversePrimaryDark
import com.leeweeder.unitimetable.ui.theme.inversePrimaryLight
import com.leeweeder.unitimetable.ui.theme.inverseSurfaceDark
import com.leeweeder.unitimetable.ui.theme.inverseSurfaceLight
import com.leeweeder.unitimetable.ui.theme.onBackgroundDark
import com.leeweeder.unitimetable.ui.theme.onBackgroundLight
import com.leeweeder.unitimetable.ui.theme.onErrorContainerDark
import com.leeweeder.unitimetable.ui.theme.onErrorContainerLight
import com.leeweeder.unitimetable.ui.theme.onErrorDark
import com.leeweeder.unitimetable.ui.theme.onErrorLight
import com.leeweeder.unitimetable.ui.theme.onPrimaryContainerDark
import com.leeweeder.unitimetable.ui.theme.onPrimaryContainerLight
import com.leeweeder.unitimetable.ui.theme.onPrimaryDark
import com.leeweeder.unitimetable.ui.theme.onPrimaryLight
import com.leeweeder.unitimetable.ui.theme.onSecondaryContainerDark
import com.leeweeder.unitimetable.ui.theme.onSecondaryContainerLight
import com.leeweeder.unitimetable.ui.theme.onSecondaryDark
import com.leeweeder.unitimetable.ui.theme.onSecondaryLight
import com.leeweeder.unitimetable.ui.theme.onSurfaceDark
import com.leeweeder.unitimetable.ui.theme.onSurfaceLight
import com.leeweeder.unitimetable.ui.theme.onSurfaceVariantDark
import com.leeweeder.unitimetable.ui.theme.onSurfaceVariantLight
import com.leeweeder.unitimetable.ui.theme.onTertiaryContainerDark
import com.leeweeder.unitimetable.ui.theme.onTertiaryContainerLight
import com.leeweeder.unitimetable.ui.theme.onTertiaryDark
import com.leeweeder.unitimetable.ui.theme.onTertiaryLight
import com.leeweeder.unitimetable.ui.theme.outlineDark
import com.leeweeder.unitimetable.ui.theme.outlineLight
import com.leeweeder.unitimetable.ui.theme.outlineVariantDark
import com.leeweeder.unitimetable.ui.theme.outlineVariantLight
import com.leeweeder.unitimetable.ui.theme.primaryContainerDark
import com.leeweeder.unitimetable.ui.theme.primaryContainerLight
import com.leeweeder.unitimetable.ui.theme.primaryDark
import com.leeweeder.unitimetable.ui.theme.primaryLight
import com.leeweeder.unitimetable.ui.theme.scrimDark
import com.leeweeder.unitimetable.ui.theme.scrimLight
import com.leeweeder.unitimetable.ui.theme.secondaryContainerDark
import com.leeweeder.unitimetable.ui.theme.secondaryContainerLight
import com.leeweeder.unitimetable.ui.theme.secondaryDark
import com.leeweeder.unitimetable.ui.theme.secondaryLight
import com.leeweeder.unitimetable.ui.theme.surfaceBrightDark
import com.leeweeder.unitimetable.ui.theme.surfaceBrightLight
import com.leeweeder.unitimetable.ui.theme.surfaceContainerDark
import com.leeweeder.unitimetable.ui.theme.surfaceContainerHighDark
import com.leeweeder.unitimetable.ui.theme.surfaceContainerHighLight
import com.leeweeder.unitimetable.ui.theme.surfaceContainerHighestDark
import com.leeweeder.unitimetable.ui.theme.surfaceContainerHighestLight
import com.leeweeder.unitimetable.ui.theme.surfaceContainerLight
import com.leeweeder.unitimetable.ui.theme.surfaceContainerLowDark
import com.leeweeder.unitimetable.ui.theme.surfaceContainerLowLight
import com.leeweeder.unitimetable.ui.theme.surfaceContainerLowestDark
import com.leeweeder.unitimetable.ui.theme.surfaceContainerLowestLight
import com.leeweeder.unitimetable.ui.theme.surfaceDark
import com.leeweeder.unitimetable.ui.theme.surfaceDimDark
import com.leeweeder.unitimetable.ui.theme.surfaceDimLight
import com.leeweeder.unitimetable.ui.theme.surfaceLight
import com.leeweeder.unitimetable.ui.theme.surfaceVariantDark
import com.leeweeder.unitimetable.ui.theme.surfaceVariantLight
import com.leeweeder.unitimetable.ui.theme.tertiaryContainerDark
import com.leeweeder.unitimetable.ui.theme.tertiaryContainerLight
import com.leeweeder.unitimetable.ui.theme.tertiaryDark
import com.leeweeder.unitimetable.ui.theme.tertiaryLight

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

