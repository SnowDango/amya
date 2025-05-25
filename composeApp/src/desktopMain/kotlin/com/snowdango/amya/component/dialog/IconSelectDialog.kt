package com.snowdango.amya.component.dialog

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import compose.icons.AllIcons
import compose.icons.TablerIcons

@Composable
fun IconSelectDialog(
    onDismissRequest: () -> Unit,
    onSelectIcon: (ImageVector) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {},
        dismissButton = {},
        title = {
            Text(
                "Select Icon",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 20.sp,
            )
        },
        text = {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(60.dp),
                modifier = Modifier
                    .padding(top = 16.dp)
            ) {
                items(TablerIcons.AllIcons) {
                    Box(
                        modifier = Modifier
                            .padding(5.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, MaterialTheme.colorScheme.onBackground, RoundedCornerShape(16.dp))
                            .clickable {
                                onSelectIcon.invoke(it)
                                onDismissRequest()
                            },
                    ) {
                        Image(
                            imageVector = it,
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.onBackground),
                            modifier = Modifier
                                .padding(5.dp)
                                .size(50.dp)
                        )
                    }
                }
            }
        },
    )
}
