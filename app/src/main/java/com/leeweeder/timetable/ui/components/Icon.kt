package com.leeweeder.timetable.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource

@Composable
fun Icon(
    @DrawableRes iconId: Int,
    contentDescription: String?,
    tint: Color = LocalContentColor.current
) {
    Icon(
        painter = painterResource(iconId),
        contentDescription = contentDescription,
        tint = tint
    )
}