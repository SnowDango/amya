package com.snowdango.amya.component.app

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.compose.LocalPlatformContext
import com.snowdango.amya.model.AppsModel
import com.snowdango.amya.platform.ImageRequestProvider
import com.snowdango.amya.track.Log

@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    appData: AppsModel.AppData,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onTransferClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var isZoom: Boolean by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (isZoom) 1.05f else 1f)
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .width(175.dp)
                .height(265.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(color = MaterialTheme.colorScheme.surfaceContainerHigh)
                .onPointerEvent(PointerEventType.Enter) {
                    isZoom = true
                }
                .onPointerEvent(PointerEventType.Exit) {
                    isZoom = false
                }
                .clickable { onClick.invoke() }
                .onClick(
                    matcher = PointerMatcher.mouse(PointerButton.Secondary),
                    onClick = {
                        menuExpanded = true
                    }
                ),
            contentAlignment = Alignment.Center,
        ) {
            TooltipArea(
                tooltip = {
                    Text(
                        text = appData.name,
                        color = MaterialTheme.colorScheme.onTertiary,
                        fontSize = 12.sp,
                        lineHeight = 12.sp,
                        modifier = Modifier
                            .background(color = MaterialTheme.colorScheme.tertiary)
                    )
                }
            ) {
                AsyncImage(
                    model = ImageRequestProvider.getImageRequest(
                        context = LocalPlatformContext.current,
                        model = appData.imageUrl,
                    ),
                    onState = {
                        if (it is AsyncImagePainter.State.Error) {
                            Log.e("Image loading error: ${it.result.throwable.message}")
                        }
                    },
                    contentDescription = appData.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .scale(scale)
                )
            }

            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                DropdownMenuItem(
                    text = { Text(text = "Edit") },
                    onClick = {
                        onEditClick.invoke()
                        menuExpanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text(text = "Transfer") },
                    onClick = {
                        onTransferClick.invoke()
                        menuExpanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text(text = "Delete") },
                    onClick = {
                        onDeleteClick.invoke()
                        menuExpanded = false
                    }
                )
            }
        }
    }
}
