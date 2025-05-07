package com.snowdango.amya.feature.tag

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import com.snowdango.amya.component.app.AppCard
import com.snowdango.amya.component.button.PrimaryTextButton
import compose.icons.TablerIcons
import compose.icons.tablericons.Plus
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
fun TagViewScreen(
    tagId: Long,
    subTagId: Long?,
    navigateAddApp: (tagId: Long, subTagId: Long?) -> Unit,
    viewModel: TagViewModel = koinViewModel { parametersOf(tagId, subTagId) },
) {

    val appsData = viewModel.appsData.collectAsState(initial = emptyList())

    Box(
        modifier = Modifier
            .padding(all = 16.dp)
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            Box(
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd,
            ) {
                PrimaryTextButton(
                    onClick = {
                        navigateAddApp.invoke(tagId, subTagId)
                    },
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ){
                        Image(
                            imageVector = TablerIcons.Plus,
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimary),
                        )
                        Text(
                            text = "Add Apps",
                            fontSize = 16.sp,
                        )
                    }
                }
            }
            LazyVerticalGrid(
                columns = GridCells.FixedSize(size = 191.dp),
                modifier = Modifier.fillMaxSize()
                    .weight(1f),
            ) {
                items(appsData.value) {
                    AppCard(
                        appData = it,
                        onClick = {
                            viewModel.exec(it.path)
                        },
                        modifier = Modifier
                            .padding(all = 8.dp),
                    )
                }
             }
        }
    }
}