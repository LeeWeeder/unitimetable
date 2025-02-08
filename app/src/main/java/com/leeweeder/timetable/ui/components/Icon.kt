package com.leeweeder.timetable.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource

@Composable
fun Icon(@DrawableRes iconId: Int, contentDescription: String?) {
    Icon(
        painter = painterResource(iconId),
        contentDescription = contentDescription,
    )
}