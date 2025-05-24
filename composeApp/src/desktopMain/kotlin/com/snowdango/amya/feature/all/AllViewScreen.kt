package com.snowdango.amya.feature.all

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.snowdango.amya.component.app.AppCard
import com.snowdango.amya.component.dialog.DeleteAppDialog
import com.snowdango.amya.model.AppsModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
fun AllViewScreen(
    viewModel: AllViewModel = koinViewModel()
) {
    val appsData = viewModel.appsData.collectAsState(initial = emptyList())
    var wantDeleteApp: AppsModel.AppData? by remember { mutableStateOf(null) }
    Box(
        modifier = Modifier
            .padding(all = 16.dp)
            .fillMaxSize()
    ) {
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
