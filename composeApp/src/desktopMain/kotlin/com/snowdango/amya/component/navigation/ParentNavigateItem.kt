package com.snowdango.amya.component.navigation

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalFoundationApi::class)
@Preview
@Composable
fun ParentNavigateItem(
    modifier: Modifier = Modifier,
    title: String,
    icon: ImageVector,
    selected: Boolean,
    isMenuEnable: Boolean = true,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onEditClick: () -> Unit,
    childContent: @Composable ColumnScope.() -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier,
    ) {
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
                    enabled = isMenuEnable,
                    matcher = PointerMatcher.mouse(PointerButton.Secondary),
                    onClick = {
                        menuExpanded = true
                    }
                ),
        ) {
            Row(
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .fillMaxWidth()
                    .height(28.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Spacer(
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .fillMaxHeight()
                        .width(4.dp)
                        .then(
                            if (selected) {
                                Modifier.background(MaterialTheme.colorScheme.primary)
                            } else {
                                Modifier.background(MaterialTheme.colorScheme.background)
                            }
                        )
                )
                Image(
                    imageVector = icon,
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.onBackground),
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .size(20.dp)
                )
                Text(
                    text = title,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                )

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
                            onEditClick.invoke()
                            menuExpanded = false
                        }
                    )
                }
            }
        }
        if (selected) {
            childContent()
        }
    }
}
