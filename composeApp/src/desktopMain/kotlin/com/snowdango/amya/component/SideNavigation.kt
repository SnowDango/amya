package com.snowdango.amya.component

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import compose.icons.TablerIcons
import compose.icons.tablericons.Menu
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
        ){
            Image(
                imageVector = TablerIcons.Menu2,
                contentDescription = null,
                colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.onBackground),
                modifier = Modifier
                    .padding(start = 4.dp, end = 4.dp)
                    .size(24.dp)
                    .clickable { isExpanded = !isExpanded }
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ){
            content()
        }
        Row(
            modifier = Modifier
                .padding(top = 12.dp, bottom = 12.dp)
                .fillMaxWidth()
                .height(28.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ){
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            )
            Row(
                modifier = Modifier
                    .clickable { onClickSetting.invoke() }
                    .padding(top = 4.dp, bottom = 4.dp)
            ){
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