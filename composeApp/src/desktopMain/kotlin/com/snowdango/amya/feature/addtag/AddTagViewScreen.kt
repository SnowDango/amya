package com.snowdango.amya.feature.addtag

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.snowdango.amya.Log
import com.snowdango.amya.component.dialog.CreateErrorDialog
import com.snowdango.amya.component.button.PrimaryTextButton
import com.snowdango.amya.component.button.SecondaryTextButton
import org.koin.compose.viewmodel.koinViewModel


@Composable
fun AddTagViewScreen(
    parentId: Long?,
    navigateTag: (parentId: Long, childId: Long?) -> Unit,
    viewModel: AddTagViewModel = koinViewModel(),
) {
    val resultState = viewModel.result.collectAsStateWithLifecycle()
    var tagName by remember { mutableStateOf("") }
    var icon: ImageVector? by remember { mutableStateOf(null) }
    var isShowIconSelectDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isShowCreateErrorDialog by remember { mutableStateOf(false) }

    LaunchedEffect(resultState.value) {
        if(resultState.value is AddTagViewModel.CreateTagState.Success) {
            val result = resultState.value as AddTagViewModel.CreateTagState.Success
            navigateTag.invoke(result.parentId, result.childId)
        }else if (resultState.value is AddTagViewModel.CreateTagState.ValidationError) {
            val result = resultState.value as AddTagViewModel.CreateTagState.ValidationError
            errorMessage = result.error
            isShowCreateErrorDialog = true
        }else if (resultState.value is AddTagViewModel.CreateTagState.Failure) {
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
        ){

            Text(
                text = if (parentId == null) "Add Parent Tag" else "Add Child Tag",
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

                Column(
                    modifier = Modifier
                        .padding(bottom = 50.dp)
                        .defaultMinSize(minWidth = 300.dp)
                        .fillMaxWidth(0.5f)
                ) {
                    Text(
                        "Tag Name",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                    )
                    TextField(
                        value = tagName,
                        onValueChange = { tagName = it },
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }

               Column(
                    modifier = Modifier
                        .padding(top = 50.dp)
                        .defaultMinSize(minWidth = 300.dp)
                        .fillMaxWidth(0.5f)
                ) {
                    Text(
                        "Choose Icon",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                    )
                   Column(
                       modifier = Modifier
                           .fillMaxWidth(),
                       horizontalAlignment = Alignment.CenterHorizontally
                   ) {
                       SecondaryTextButton(
                           onClick = {
                                 isShowIconSelectDialog = true
                            },
                            modifier = Modifier
                                 .padding(bottom = 8.dp)
                       ) {
                           Text(
                               text = "Choose Icon",
                               fontSize = 16.sp
                           )
                       }
                       if(icon != null) {
                           Image(
                               imageVector = icon!!,
                               contentDescription = null,
                               colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.onBackground),
                               modifier = Modifier
                                   .size(50.dp)
                           )
                       }else {
                           Spacer(
                               modifier = Modifier
                                   .size(50.dp)
                           )
                       }
                   }
                }
            }

            PrimaryTextButton(
                onClick = {
                    Log.d("CreateTag: tagName: $tagName parentId: $parentId icon: ${icon?.name}")
                    viewModel.checkAndCreateTag(parentId, tagName, icon)
                },
            ) {
                Text(
                    text = "Create",
                    fontSize = 18.sp,
                )
            }
        }
    }

    if(isShowIconSelectDialog) {
        IconSelectDialog(
            onDismissRequest = {
                isShowIconSelectDialog = false
            },
            onSelectIcon = {
                icon = it
            }
        )
    }

    if (isShowCreateErrorDialog){
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