package com.snowdango.amya.component.dialog

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.LocalPlatformContext
import coil3.compose.SubcomposeAsyncImage
import coil3.request.ImageRequest
import com.snowdango.amya.component.button.PrimaryTextButton
import com.snowdango.amya.component.button.SecondaryTextButton
import compose.icons.TablerIcons
import compose.icons.tablericons.AlertCircle
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.path


@Composable
fun EditAppDialog(
    onDismissRequest: () -> Unit,
    appName: String,
    filePath: String,
    imageUrl: String,
    onSaveApp: (appName: String, filePath: String, imageUrl: String) -> Unit,
) {
    var editAppName by remember { mutableStateOf(appName) }
    var editFilePath by remember { mutableStateOf(filePath) }
    var editImageUrl by remember { mutableStateOf(imageUrl) }

    val launcher = rememberFilePickerLauncher { file ->
        editFilePath = file?.path ?: ""
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            PrimaryTextButton(
                onClick = {
                    onSaveApp.invoke(editAppName, editFilePath, editImageUrl)
                    onDismissRequest.invoke()
                },
            ) {
                Text(
                    text = "Save",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        },
        dismissButton = {
            PrimaryTextButton(
                onClick = {
                    onDismissRequest.invoke()
                },
            ) {
                Text(
                    text = "Cancel",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        },
        title = {
            Text(
                text = "Edit Application",
                color = MaterialTheme.colorScheme.onBackground,
            )
        },
        text = {
            Column(
                modifier = Modifier,
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(
                    space = 20.dp,
                    alignment = Alignment.CenterVertically,
                ),
            ) {
                // appName
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(
                        "App Name",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                    )
                    TextField(
                        value = editAppName,
                        onValueChange = { editAppName = it },
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }

                // filePath
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
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
                        value = editFilePath,
                        onValueChange = { },
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }

                // imageUrl
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(
                        "Image URL",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                    )
                    TextField(
                        value = editImageUrl,
                        onValueChange = { editImageUrl = it },
                        modifier = Modifier
                            .padding(bottom = 16.dp)
                            .fillMaxWidth()
                    )
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        SubcomposeAsyncImage(
                            model = ImageRequest.Builder(LocalPlatformContext.current)
                                .data(editImageUrl)
                                .build(),
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
        }
    )
}