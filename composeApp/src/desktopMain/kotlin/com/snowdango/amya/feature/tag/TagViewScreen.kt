package com.snowdango.amya.feature.tag

import androidx.compose.foundation.Image
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import com.snowdango.amya.component.button.PrimaryTextButton
import compose.icons.TablerIcons
import compose.icons.tablericons.Plus
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun TagViewScreen(
    tagId: Long,
    subTagId: Long?,
    viewModel: TagViewModel = koinViewModel { parametersOf(tagId, subTagId) },
) {

    val appsData = viewModel.appsData.collectAsStateWithLifecycle()

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
                    onClick = {},
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
                columns = GridCells.Adaptive(minSize = 270.dp),
                modifier = Modifier.fillMaxSize()
                    .weight(1f),
            ) {
                items(appsData.value) {
                    Box(
                        modifier = Modifier
                            .padding(all = 10.dp)
                            .width(250.dp)
                            .height(350.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(color = MaterialTheme.colorScheme.surfaceContainerHigh)
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalPlatformContext.current)
                                .data(it.imageUrl)
                                .memoryCachePolicy(CachePolicy.ENABLED)
                                .networkCachePolicy(CachePolicy.ENABLED)
                                .build(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
             }
        }
    }
}