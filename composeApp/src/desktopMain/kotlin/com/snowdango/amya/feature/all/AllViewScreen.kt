package com.snowdango.amya.feature.all

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.snowdango.amya.component.app.AppCard
import com.snowdango.amya.component.button.PrimaryTextButton
import com.snowdango.amya.component.dialog.DeleteAppDialog
import com.snowdango.amya.model.AppsModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
fun AllViewScreen(
    viewModel: AllViewModel = koinViewModel()
) {
    val appsData = viewModel.sortedAppsData.collectAsStateWithLifecycle()
    val orderType = viewModel.orderType.collectAsStateWithLifecycle()
    var isOrderMenuExpanded by remember { mutableStateOf(false) }
    var wantDeleteApp: AppsModel.AppData? by remember { mutableStateOf(null) }
    Column(
        modifier = Modifier
            .padding(all = 16.dp)
            .fillMaxSize()
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
                    AllViewModel.OrderType.entries.forEach {
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
            PrimaryTextButton( // fix height tag view
                onClick = {},
                content = {},
                modifier = Modifier
                    .alpha(0f)
            )
        }
        LazyVerticalGrid(
            columns = GridCells.FixedSize(size = 191.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(appsData.value) {
                AppCard(
                    appData = it,
                    onClick = {
                        viewModel.exec(it.path)
                    },
                    onEditClick = {},
                    onTransferClick = {},
                    onDeleteClick = {
                        wantDeleteApp = it
                    },
                    modifier = Modifier
                        .padding(all = 8.dp),
                )
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
    }
}
