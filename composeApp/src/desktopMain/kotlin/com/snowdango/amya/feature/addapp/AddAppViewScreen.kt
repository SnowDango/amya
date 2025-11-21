package com.snowdango.amya.feature.addapp

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.LocalPlatformContext
import coil3.compose.SubcomposeAsyncImage
import com.snowdango.amya.component.button.PrimaryTextButton
import com.snowdango.amya.component.button.SecondaryTextButton
import com.snowdango.amya.component.dialog.CreateErrorDialog
import com.snowdango.amya.platform.ImageRequestProvider
import com.snowdango.amya.track.Log
import compose.icons.TablerIcons
import compose.icons.tablericons.AlertCircle
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.path
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AddAppViewScreen(
    parentId: Long,
    childId: Long?,
    isWindows: Boolean,
    navigateBackTag: () -> Unit,
    viewModel: AddAppViewModel = koinViewModel(),
) {
    val resultState = viewModel.result.collectAsStateWithLifecycle()

    var appName by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var filePath by remember { mutableStateOf("") }
    var args by remember { mutableStateOf("") }
    var root by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf("") }
    var isShowCreateErrorDialog by remember { mutableStateOf(false) }

    val launcher = rememberFilePickerLauncher { file ->
        filePath = file?.path ?: ""
    }

    LaunchedEffect(resultState.value) {
        if (resultState.value is AddAppViewModel.CreateAppState.Success) {
            navigateBackTag.invoke()
        } else if (resultState.value is AddAppViewModel.CreateAppState.ValidationError) {
            val result = resultState.value as AddAppViewModel.CreateAppState.ValidationError
            errorMessage = result.message
            isShowCreateErrorDialog = true
        } else if (resultState.value is AddAppViewModel.CreateAppState.Failure) {
            errorMessage = "Failed for whatever reason"
            isShowCreateErrorDialog = true
        }
    }

    Box(
        modifier = Modifier
            .padding(all = 16.dp)
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 32.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Add App",
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onBackground,
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // appName
                Column(
                    modifier = Modifier
                        .padding(bottom = 50.dp)
                        .defaultMinSize(minWidth = 300.dp)
                        .fillMaxWidth(0.5f)
                ) {
                    Text(
                        "App Name",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                    )
                    TextField(
                        value = appName,
                        onValueChange = { appName = it },
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }

                // path
                Column(
                    modifier = Modifier
                        .padding(bottom = 50.dp)
                        .defaultMinSize(minWidth = 300.dp)
                        .fillMaxWidth(0.5f)
                ) {
                    Text(
                        "File Path",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                    )
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        SecondaryTextButton(
                            onClick = {
                                launcher.launch()
                            },
                            modifier = Modifier
                                .padding(bottom = 8.dp)
                        ) {
                            Text(
                                text = "Select File",
                                fontSize = 16.sp
                            )
                        }
                    }
                    TextField(
                        value = filePath,
                        onValueChange = {
                            filePath = it
                        },
                        readOnly = false,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }

                // args
                Column(
                    modifier = Modifier
                        .padding(bottom = 50.dp)
                        .defaultMinSize(minWidth = 300.dp)
                        .fillMaxWidth(0.5f)
                ) {
                    Text(
                        "Args (Optional)",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                    )
                    TextField(
                        value = args,
                        onValueChange = { args = it },
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }

                if (isWindows) {
                    Column(
                        modifier = Modifier
                            .defaultMinSize(minWidth = 300.dp)
                            .fillMaxWidth(0.5f)
                    ) {
                        Text(
                            "Root (Optional)",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier
                                .padding(bottom = 8.dp)
                        )
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterEnd,
                        ) {
                            Switch(
                                checked = root,
                                onCheckedChange = { root = it },
                            )
                        }
                    }
                }


                // imageUrl
                Column(
                    modifier = Modifier
                        .padding(bottom = 50.dp)
                        .defaultMinSize(minWidth = 300.dp)
                        .fillMaxWidth(0.5f)
                ) {
                    Text(
                        "Image URL",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                    )
                    TextField(
                        value = imageUrl,
                        onValueChange = { imageUrl = it },
                        modifier = Modifier
                            .padding(bottom = 16.dp)
                            .fillMaxWidth()
                    )
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        SubcomposeAsyncImage(
                            model = ImageRequestProvider.getImageRequest(
                                context = LocalPlatformContext.current,
                                model = imageUrl,
                            ),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            loading = {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    CircularProgressIndicator(
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(88.dp),
                                    )
                                }
                            },
                            error = {
                                Log.e("Image loading error: ${it.result.throwable.message}")
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Image(
                                        imageVector = TablerIcons.AlertCircle,
                                        contentDescription = null,
                                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.error),
                                        modifier = Modifier.size(88.dp),
                                    )
                                }
                            },
                            success = {
                                Image(
                                    painter = it.painter,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                )
                            },
                            modifier = Modifier
                                .height(265.dp)
                                .width(175.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(color = MaterialTheme.colorScheme.surfaceContainerHigh)
                        )
                    }
                }
            }

            PrimaryTextButton(
                onClick = {
                    viewModel.checkAndCreateApp(parentId, childId, appName, filePath, args.ifBlank { null }, imageUrl, root)
                },
            ) {
                Text(
                    text = "Create",
                    fontSize = 18.sp,
                )
            }
        }

        if (isShowCreateErrorDialog) {
            CreateErrorDialog(
                error = errorMessage,
                onDismissRequest = {
                    errorMessage = ""
                    viewModel.resetResult()
                    isShowCreateErrorDialog = false
                },
            )
        }
    }
}
