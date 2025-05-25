package com.snowdango.amya.component.dialog

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import compose.icons.TablerIcons
import compose.icons.tablericons.AlertCircle

@Composable
fun CreateErrorDialog(
    error: String,
    onDismissRequest: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {},
        dismissButton = {},
        title = {
            Text(
                text = "Error",
                color = MaterialTheme.colorScheme.error,
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .padding(all = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    imageVector = TablerIcons.AlertCircle,
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.error),
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .size(80.dp)
                )
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 20.sp,
                )
            }
        }
    )
}
