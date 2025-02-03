package com.leeweeder.timetable.ui.timetable_setup.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource

@Composable
fun TextButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    color: Color = if (enabled) ButtonDefaults.textButtonColors().contentColor else ButtonDefaults.textButtonColors().disabledContentColor
) {
    TextButton(onClick = onClick, enabled = enabled) {
        Text(text, color = color)
    }
}

@Composable
fun TextButtonWithIcon(onClick: () -> Unit, @DrawableRes iconId: Int, label: String) {
    TextButton(
        onClick = onClick,
        contentPadding = ButtonDefaults.TextButtonWithIconContentPadding
    ) {
        Icon(
            painter = painterResource(iconId),
            contentDescription = null,
            modifier = Modifier.size(ButtonDefaults.IconSize)
        )
        Spacer(Modifier.width(ButtonDefaults.IconSpacing))
        Text(label)
    }
}

@Composable
fun CancelTextButton(onClick: () -> Unit, enabled: Boolean = true) {
    TextButton("Cancel", onClick = onClick, enabled = enabled)
}