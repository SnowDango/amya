package com.snowdango.amya.component.navigation

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun AddTagNavigateItem(
    modifier: Modifier = Modifier,
    isParent: Boolean = false,
    selected: Boolean = false,
    onClick: () -> Unit,
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
    ){
        Row(
            modifier = Modifier
                .padding(vertical = 4.dp)
                .fillMaxWidth()
                .height(28.dp)
                .then(if (isParent) {
                    Modifier.alpha(1f)
                } else {
                    Modifier.alpha(0.7f)
                }),
            verticalAlignment = Alignment.CenterVertically,
        ){
            Spacer(
                modifier = Modifier
                    .padding(end = 16.dp)
                    .fillMaxHeight()
                    .width(4.dp)
                    .then(if (selected) {
                        if(isParent) {
                            Modifier.background(MaterialTheme.colorScheme.primary)
                        } else {
                            Modifier.background(MaterialTheme.colorScheme.secondary)
                        }
                    } else {
                        Modifier.background(MaterialTheme.colorScheme.background)
                    })
            )
            Image(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.onBackground),
                modifier = Modifier
                    .padding(start = 2.dp, end = 10.dp)
                    .size(16.dp)
            )
            Text(
                text = if (isParent) {
                    "Add Parent Tag"
                } else {
                    "Add Child Tag"
                },
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            )
        }
    }
}