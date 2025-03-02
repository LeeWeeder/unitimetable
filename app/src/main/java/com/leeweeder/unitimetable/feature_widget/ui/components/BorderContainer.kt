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

package com.leeweeder.unitimetable.feature_widget.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.width
import com.leeweeder.unitimetable.ui.CellBorderDirection

data class BorderProperty(
    val width: Dp = 0.dp,
    val color: Color = Color.Transparent
)

@ConsistentCopyVisibility
data class BorderProperties private constructor(
    val top: BorderProperty,
    val bottom: BorderProperty,
    val start: BorderProperty,
    val end: BorderProperty
) {
    companion object {
        fun of(all: BorderProperty): BorderProperties {
            return BorderProperties(all, all, all, all)
        }

        fun of(
            horizontal: BorderProperty = BorderProperty(),
            vertical: BorderProperty = BorderProperty()
        ): BorderProperties {
            return BorderProperties(
                start = horizontal,
                end = horizontal,
                top = vertical,
                bottom = vertical
            )
        }

        fun of(
            top: BorderProperty = BorderProperty(),
            bottom: BorderProperty = BorderProperty(),
            start: BorderProperty = BorderProperty(),
            end: BorderProperty = BorderProperty()
        ): BorderProperties {
            return BorderProperties(
                start = start,
                end = end,
                top = top,
                bottom = bottom
            )
        }
    }
}

object BorderContainerDefaults {
    val color: Color
        @Composable
        get() = GlanceTheme.colors.outline.getColor(LocalContext.current)
}

@Composable
internal fun BorderContainer(
    width: BorderProperties = BorderProperties.of(
        bottom = BorderProperty(
            1.dp,
            BorderContainerDefaults.color
        )
    ),
    contentAlignment: Alignment = Alignment.Companion.TopStart,
    modifier: GlanceModifier = GlanceModifier.Companion,
    content: (@Composable () -> Unit)? = null
) {
    Row(modifier) {
        if (width.start.width.value != 0f) CellBorder(
            CellBorderDirection.Vertical,
            thickness = width.start.width,
            color = width.start.color
        )

        Column(modifier = GlanceModifier.Companion.defaultWeight().fillMaxHeight()) {
            if (width.top.width.value != 0f) {
                CellBorder(
                    CellBorderDirection.Horizontal,
                    thickness = width.top.width,
                    color = width.top.color
                )
            }

            content?.let {
                Box(
                    modifier = GlanceModifier.Companion.defaultWeight(),
                    contentAlignment = contentAlignment
                ) {
                    it()
                }
            }

            if (width.bottom.width.value != 0f) {
                CellBorder(
                    CellBorderDirection.Horizontal,
                    thickness = width.bottom.width,
                    color = width.bottom.color
                )
            }
        }

        if (width.end.width.value != 0f) {
            CellBorder(
                CellBorderDirection.Vertical,
                thickness = width.end.width,
                color = width.end.color
            )
        }
    }
}

@Composable
private fun CellBorder(
    borderDirection: CellBorderDirection,
    thickness: Dp,
    color: Color = BorderContainerDefaults.color
) {
    Box(
        modifier =
        when (borderDirection) {
            CellBorderDirection.Horizontal -> GlanceModifier.Companion.fillMaxWidth()
                .height(thickness)

            CellBorderDirection.Vertical -> GlanceModifier.Companion.fillMaxHeight()
                .width(thickness)
        }.background(color)
    ) {}
}