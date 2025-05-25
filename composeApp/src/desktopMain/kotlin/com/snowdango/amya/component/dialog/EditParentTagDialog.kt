package com.snowdango.amya.component.dialog

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snowdango.amya.component.button.PrimaryTextButton
import com.snowdango.amya.component.button.SecondaryTextButton

@Composable
fun EditParentTagDialog(
    tagId: Long,
    tagName: String,
    icon: ImageVector,
    onDismissRequest: () -> Unit,
    onSaveClick: (id: Long, name: String, icon: ImageVector) -> Unit,
) {
    var tagName by remember { mutableStateOf(tagName) }
    var icon by remember { mutableStateOf(icon) }
    var isShowIconSelectDialog by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                text = "Edit $tagName",
                color = MaterialTheme.colorScheme.onBackground,
            )
        },
        confirmButton = {
            PrimaryTextButton(
                onClick = {
                    onSaveClick.invoke(tagId, tagName, icon)
                    onDismissRequest.invoke()
                },
            ) {
                Text(
                    text = "Save",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        },
        dismissButton = {
            PrimaryTextButton(
                onClick = {
                    onDismissRequest.invoke()
                },
            ) {
                Text(
                    text = "Cancel",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .fillMaxWidth(0.75f)
                ) {
                    Text(
                        "Tag Name",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                    )
                    TextField(
                        value = tagName,
                        onValueChange = { tagName = it },
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }

                Column(
                    modifier = Modifier
                        .defaultMinSize(minWidth = 300.dp)
                        .fillMaxWidth(0.75f)
                ) {
                    Text(
                        "Choose Icon",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        SecondaryTextButton(
                            onClick = {
                                isShowIconSelectDialog = true
                            },
                            modifier = Modifier
                                .padding(bottom = 8.dp)
                        ) {
                            Text(
                                text = "Choose Icon",
                                fontSize = 16.sp
                            )
                        }
                        Image(
                            imageVector = icon,
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.onBackground),
                            modifier = Modifier
                                .size(50.dp)
                        )
                    }
                }
            }
            if (isShowIconSelectDialog) {
                IconSelectDialog(
                    onDismissRequest = {
                        isShowIconSelectDialog = false
                    },
                    onSelectIcon = {
                        icon = it
                    }
                )
            }
        }
    )
}
