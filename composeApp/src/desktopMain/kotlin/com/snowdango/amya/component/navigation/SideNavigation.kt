package com.snowdango.amya.component.navigation

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import compose.icons.TablerIcons
import compose.icons.tablericons.Menu2
import compose.icons.tablericons.Settings

@Composable
fun SideNavigation(
    modifier: Modifier = Modifier,
    onClickSetting: () -> Unit,
    content: @Composable () -> Unit,
) {
    val maxWidth = with(LocalDensity.current) { 200.toDp() }
    var isExpanded by remember { mutableStateOf(true) }
    val width by animateDpAsState(if (isExpanded) maxWidth else 48.dp)
    Column(
        modifier = modifier
            .padding(
                top = 32.dp,
                start = 16.dp,
                end = 8.dp,
                bottom = 16.dp,
            )
            .fillMaxHeight()
            .width(width)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable(
                        onClick = { isExpanded = !isExpanded },
                        indication = ripple(
                            color = MaterialTheme.colorScheme.primary,
                        ),
                        interactionSource = remember { MutableInteractionSource() }
                    )
            ) {
                Image(
                    imageVector = TablerIcons.Menu2,
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.onBackground),
                    modifier = Modifier
                        .padding(start = 4.dp, end = 4.dp)
                        .size(24.dp),
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            content()
        }
        Box(
            modifier = Modifier
                .padding(vertical = 12.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .clickable(
                    onClick = { onClickSetting.invoke() },
                    indication = ripple(
                        color = MaterialTheme.colorScheme.primary,
                    ),
                    interactionSource = remember { MutableInteractionSource() }
                )
        ) {
            Row(
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .fillMaxWidth()
                    .height(28.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                )
                Row(
                    modifier = Modifier
                        .padding(top = 4.dp, bottom = 4.dp)
                ) {
                    Text(
                        text = "Settings",
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.End,
                        fontSize = 13.sp,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .fillMaxWidth()
                            .weight(2f)
                    )
                    Image(
                        imageVector = TablerIcons.Settings,
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.onBackground),
                        modifier = Modifier
                            .padding(start = 20.dp, end = 8.dp)
                            .size(20.dp)
                    )
                }
            }
        }
    }
}
