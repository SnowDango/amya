package com.snowdango.amya.component.dialog

import androidx.compose.material.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp
import com.snowdango.amya.component.button.PrimaryTextButton

@Composable
fun DeleteParentTagDialog(
    id: Long,
    tagName: String,
    onDelete: (id: Long) -> Unit,
    onDismissRequest: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        backgroundColor = MaterialTheme.colorScheme.background,
        confirmButton = {
            PrimaryTextButton(
                onClick = {
                    onDelete.invoke(id)
                    onDismissRequest.invoke()
                },
            ) {
                Text(
                    text = "Delete",
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
                text = "Delete $tagName?",
                color = MaterialTheme.colorScheme.onBackground,
            )
        },
        text = {
            Text(
                text = "If the parent tag is deleted, \nthe associated application will also be deleted.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground,
            )
        },
    )
}
