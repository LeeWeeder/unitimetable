package com.leeweeder.timetable.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.leeweeder.timetable.R

@Composable
fun BaseSelectionField(
    icon: @Composable () -> Unit,
    label: String,
    headlineContent: @Composable () -> Unit,
    overline: String? = null,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(modifier = modifier) {
        val interactionSource = remember { MutableInteractionSource() }
        OutlinedCard(
            shape = MaterialTheme.shapes.large,
            colors = OutlinedTextFieldDefaults.colors().let {
                CardDefaults.outlinedCardColors(
                    containerColor = it.unfocusedContainerColor,
                    contentColor = it.unfocusedTextColor
                )
            },
            border = BorderStroke(
                width = OutlinedTextFieldDefaults.UnfocusedBorderThickness,
                color = OutlinedTextFieldDefaults.colors().unfocusedIndicatorColor
            ),
            modifier = Modifier.height(72.dp)
        ) {
            ListItem(
                headlineContent = headlineContent,
                overlineContent = if (overline == null) {
                    null
                } else {
                    {
                        Text(
                            overline,
                            color = OutlinedTextFieldDefaults.colors().unfocusedTextColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                trailingContent = {
                    androidx.compose.material3.IconButton(
                        interactionSource = interactionSource,
                        onClick = onClick
                    ) {
                        Icon(R.drawable.arrow_drop_down_24px, contentDescription = null)
                    }
                },
                leadingContent = icon,
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                modifier = Modifier
                    .fillMaxHeight()
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
            )
        }
        Box(
            modifier = Modifier
                .offset(x = 14.dp, y = -(10).dp)
                .background(color = MaterialTheme.colorScheme.surface)
        ) {
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                color = OutlinedTextFieldDefaults.colors().unfocusedLabelColor,
                modifier = Modifier.padding(horizontal = 6.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SelectionField(
    @DrawableRes iconId: Int,
    label: String,
    placeholder: String? = null,
    overline: String? = null,
    value: String?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    BaseSelectionField(
        icon = {
            Icon(iconId, null)
        },
        label = label,
        headlineContent = {
            val color = if (value == null) {
                OutlinedTextFieldDefaults.colors().unfocusedPlaceholderColor
            } else {
                OutlinedTextFieldDefaults.colors().unfocusedTextColor
            }

            Text(
                value ?: placeholder ?: "",
                color = color,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        },
        overline = overline, onClick = onClick,
        modifier = modifier
    )
}