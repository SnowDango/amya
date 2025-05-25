package com.snowdango.amya.component.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snowdango.amya.component.button.PrimaryTextButton
import com.snowdango.amya.model.TagModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferAppDialog(
    appName: String,
    tagId: Long,
    subTagId: Long?,
    tagList: List<TagModel.ParentTag>,
    onDismissRequest: () -> Unit,
    onTransferApp: (tagId: Long, subTagId: Long?) -> Unit,
) {
    var parentTag by remember { mutableStateOf(tagList.first { it.id == tagId }) }
    var isExpandedParentTag by remember { mutableStateOf(false) }
    var childTag: TagModel.ParentTag.ChildTag? by remember {
        mutableStateOf(
            parentTag.childTag.firstOrNull { it.id == subTagId }
        )
    }
    var isExpandedSubTag by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            PrimaryTextButton(
                onClick = {
                    onTransferApp.invoke(parentTag.id, childTag?.id)
                    onDismissRequest.invoke()
                },
            ) {
                Text(
                    text = "Transfer",
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
        title = {
            Text(
                text = "Transfer $appName",
                color = MaterialTheme.colorScheme.onBackground,
            )
        },
        text = {
            Column(
                modifier = Modifier,
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(
                    space = 20.dp,
                    alignment = Alignment.CenterVertically,
                ),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                ) {
                    Text(
                        "Parent Tag",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                    )
                    ExposedDropdownMenuBox(
                        expanded = isExpandedParentTag,
                        onExpandedChange = { isExpandedParentTag = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextField(
                            value = parentTag.name,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryEditable),
                        )
                        DropdownMenu(
                            expanded = isExpandedParentTag,
                            onDismissRequest = { isExpandedParentTag = false },
                        ) {
                            tagList.forEach { tag ->
                                DropdownMenuItem(
                                    text = { Text(tag.name) },
                                    onClick = {
                                        parentTag = tag
                                        childTag = null
                                        isExpandedParentTag = false
                                    }
                                )
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                ) {
                    Text(
                        "Child Tag",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                    )
                    ExposedDropdownMenuBox(
                        expanded = isExpandedSubTag,
                        onExpandedChange = { isExpandedSubTag = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextField(
                            value = childTag?.name ?: "No subtag",
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryEditable),
                        )
                        DropdownMenu(
                            expanded = isExpandedSubTag,
                            onDismissRequest = { isExpandedSubTag = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text("No subtag") },
                                onClick = {
                                    childTag = null
                                    isExpandedSubTag = false
                                }
                            )
                            parentTag.childTag.forEach { tag ->
                                DropdownMenuItem(
                                    text = { Text(tag.name) },
                                    onClick = {
                                        childTag = tag
                                        isExpandedSubTag = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    )
}
