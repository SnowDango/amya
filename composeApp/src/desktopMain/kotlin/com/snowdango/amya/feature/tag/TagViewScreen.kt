package com.snowdango.amya.feature.tag

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.snowdango.amya.component.app.AppCard
import com.snowdango.amya.component.button.PrimaryTextButton
import com.snowdango.amya.component.dialog.DeleteAppDialog
import com.snowdango.amya.component.dialog.EditAppDialog
import com.snowdango.amya.component.dialog.TransferAppDialog
import com.snowdango.amya.model.AppsModel
import compose.icons.TablerIcons
import compose.icons.tablericons.Plus
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TagViewScreen(
    tagId: Long,
    subTagId: Long?,
    isWindows: Boolean,
    navigateAddApp: (tagId: Long, subTagId: Long?) -> Unit,
    viewModel: TagViewModel = koinViewModel { parametersOf(tagId, subTagId) },
) {
    val appsData = viewModel.sortedAppsData.collectAsStateWithLifecycle()
    val orderType = viewModel.orderType.collectAsStateWithLifecycle()
    val tagList = viewModel.tagList.collectAsStateWithLifecycle()
    var isOrderMenuExpanded by remember { mutableStateOf(false) }
    var wantDeleteApp: AppsModel.AppData? by remember { mutableStateOf(null) }
    var wantEditApp: AppsModel.AppData? by remember { mutableStateOf(null) }
    var wantTransferApp: AppsModel.AppData? by remember { mutableStateOf(null) }

    Box(
        modifier = Modifier
            .padding(all = 16.dp)
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            Row(
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { isOrderMenuExpanded = true }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(all = 8.dp)
                        ) {
                            Image(
                                imageVector = orderType.value.icon,
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
                                modifier = Modifier.padding(end = 8.dp),
                            )
                            Text(
                                text = orderType.value.value,
                                fontSize = 16.sp,
                                maxLines = 1,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                        }
                    }
                    DropdownMenu(
                        expanded = isOrderMenuExpanded,
                        onDismissRequest = { isOrderMenuExpanded = false },
                    ) {
                        TagViewModel.OrderType.entries.forEach {
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Image(
                                            imageVector = it.icon,
                                            contentDescription = null,
                                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
                                            modifier = Modifier.padding(end = 8.dp),
                                        )
                                        Text(
                                            text = it.value,
                                            fontSize = 16.sp,
                                            maxLines = 1,
                                            color = MaterialTheme.colorScheme.onBackground,
                                        )
                                    }
                                },
                                onClick = {
                                    viewModel.setOrderType(it)
                                    isOrderMenuExpanded = false
                                },
                            )
                        }
                    }
                }
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                )
                PrimaryTextButton(
                    onClick = {
                        navigateAddApp.invoke(tagId, subTagId)
                    },
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
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
                            viewModel.exec(it.root, it.path, it.args)
                        },
                        modifier = Modifier
                            .padding(all = 8.dp),
                        onEditClick = {
                            wantEditApp = it
                        },
                        onTransferClick = {
                            wantTransferApp = it
                        },
                        onDeleteClick = {
                            wantDeleteApp = it
                        }
                    )
                }
            }
        }
        if (wantDeleteApp != null) {
            DeleteAppDialog(
                appName = wantDeleteApp!!.name,
                onDelete = {
                    viewModel.deleteApp(wantDeleteApp!!.id)
                },
                onDismissRequest = {
                    wantDeleteApp = null
                }
            )
        }
        if (wantEditApp != null) {
            EditAppDialog(
                isWindows = isWindows,
                appName = wantEditApp!!.name,
                filePath = wantEditApp!!.path,
                imageUrl = wantEditApp!!.imageUrl,
                args = wantEditApp!!.args,
                root = wantEditApp!!.root,
                onSaveApp = { editAppName, editFilePath, editArgs, editImageUrl, editRoot ->
                    viewModel.updateApp(wantEditApp!!.id, editAppName, editFilePath, editArgs, editImageUrl, editRoot)
                },
                onDismissRequest = {
                    wantEditApp = null
                },
            )
        }
        if (wantTransferApp != null) {
            TransferAppDialog(
                appName = wantTransferApp!!.name,
                tagId = wantTransferApp!!.tagId,
                subTagId = wantTransferApp!!.subTagId,
                tagList = tagList.value,
                onTransferApp = { newTagId, newSubTagId ->
                    viewModel.transferApp(wantTransferApp!!.id, newTagId, newSubTagId)
                },
                onDismissRequest = {
                    wantTransferApp = null
                }
            )
        }
    }
}
