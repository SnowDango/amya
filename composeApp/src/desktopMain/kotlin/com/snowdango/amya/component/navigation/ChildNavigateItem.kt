package com.snowdango.amya.component.navigation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.PointerMatcher
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.onClick
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChildNavigateItem(
    modifier: Modifier = Modifier,
    title: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .padding(vertical = 4.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .clickable(
                onClick = onClick,
                enabled = true,
                indication = ripple(
                    color = MaterialTheme.colorScheme.primary,
                ),
                interactionSource = remember { MutableInteractionSource() },
            )
            .onClick(
                matcher = PointerMatcher.mouse(PointerButton.Secondary),
                onClick = {
                    menuExpanded = true
                },
            )
    ){
        Row(
            modifier = Modifier
                .padding(vertical = 4.dp)
                .fillMaxWidth()
                .height(28.dp)
                .alpha(0.7f),
            verticalAlignment = Alignment.CenterVertically,
        ){
            Spacer(
                modifier = Modifier
                    .padding(end = 16.dp)
                    .fillMaxHeight()
                    .width(4.dp)
                    .then(if (selected) {
                        Modifier.background(MaterialTheme.colorScheme.secondary)
                    } else {
                        Modifier.background(MaterialTheme.colorScheme.background)
                    })
            )
            Image(
                imageVector = icon,
                contentDescription = null,
                colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.onBackground),
                modifier = Modifier
                    .padding(start = 2.dp, end = 10.dp)
                    .size(16.dp)
            )
            Text(
                text = title,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            )
        }

        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false },
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
        ) {
            DropdownMenuItem(
                text = { Text(text = "Delete") },
                onClick = {
                    onDeleteClick.invoke()
                    menuExpanded = false
                }
            )
            DropdownMenuItem(
                text = { Text(text = "Edit") },
                onClick = {
                    // Handle edit action
                    menuExpanded = false
                }
            )
        }
    }
}