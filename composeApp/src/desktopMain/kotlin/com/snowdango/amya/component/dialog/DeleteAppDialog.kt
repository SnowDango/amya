package com.snowdango.amya.component.dialog

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp
import com.snowdango.amya.component.button.PrimaryTextButton

@Composable
fun DeleteAppDialog(
    appName: String,
    onDelete: () -> Unit,
    onDismissRequest: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            PrimaryTextButton(
                onClick = {
                    onDelete.invoke()
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
                text = "Delete $appName?",
                color = MaterialTheme.colorScheme.onBackground,
            )
        },
    )
}
