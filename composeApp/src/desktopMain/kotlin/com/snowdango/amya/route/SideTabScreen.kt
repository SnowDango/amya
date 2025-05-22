package com.snowdango.amya.route

import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.snowdango.amya.component.dialog.DeleteChildTagDialog
import com.snowdango.amya.component.dialog.DeleteParentTagDialog
import com.snowdango.amya.component.dialog.EditChildTagDialog
import com.snowdango.amya.component.dialog.EditParentTagDialog
import com.snowdango.amya.component.navigation.AddTagNavigateItem
import com.snowdango.amya.component.navigation.ChildNavigateItem
import com.snowdango.amya.component.navigation.ParentNavigateItem
import com.snowdango.amya.component.navigation.SideNavigation
import com.snowdango.amya.model.TagModel
import com.snowdango.amya.track.Log
import compose.icons.TablerIcons
import compose.icons.tablericons.Home
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun SideTabScreen(
    navController: NavController,
    viewModel: RouteViewModel = koinViewModel(
        parameters = { parametersOf(navController) },
    )
) {
    val currentRoute by viewModel.currentRoute.collectAsStateWithLifecycle(
        initialValue = Route.fromNavBackStackEntry(navController.currentBackStackEntry),
    )
    val tagGroups = viewModel.tagGroup.collectAsStateWithLifecycle()

    var wantDeleteParentTag: TagModel.ParentTag? by remember { mutableStateOf(null) }
    var wantDeleteChildTag: TagModel.ParentTag.ChildTag? by remember { mutableStateOf(null) }

    var wantEditParentTag: TagModel.ParentTag? by remember { mutableStateOf(null) }
    var wantEditChildTag: Pair<TagModel.ParentTag.ChildTag, Long>? by remember { mutableStateOf(null) }

    LaunchedEffect(currentRoute) {
        Log.i(currentRoute.toString())
    }

    SideNavigation(
        modifier = Modifier
            .background(color = MaterialTheme.colorScheme.background),
        onClickSetting = {
            navController.navigate(
                Route.SettingView
            )
        }
    ) {
        ParentNavigateItem(
            title = "All View",
            icon = TablerIcons.Home,
            selected = currentRoute is Route.AllView,
            isMenuEnable = false,
            onClick = {
                navController.navigate(
                    Route.AllView
                )
            },
            onDeleteClick = {},
            onEditClick = {},
        ) {}
        tagGroups.value.forEach { group ->
            ParentNavigateItem(
                title = group.name,
                icon = group.icon,
                selected = when (currentRoute) {
                    is Route.TagView -> {
                        Log.d("${(currentRoute as Route.TagView).tagId} == ${group.id}")
                        (currentRoute as Route.TagView).tagId == group.id
                    }

                    is Route.AddTagView -> {
                        (currentRoute as Route.AddTagView).tagId == group.id
                    }

                    is Route.AddAppView -> {
                        (currentRoute as Route.AddAppView).tagId == group.id
                    }

                    else -> {
                        false
                    }
                },
                onClick = {
                    navController.navigate(
                        Route.TagView(
                            tagId = group.id,
                            subTagId = null,
                        )
                    )
                },
                onDeleteClick = {
                    wantDeleteParentTag = group
                },
                onEditClick = {
                    wantEditParentTag = group
                }
            ) {
                group.childTag.forEach { child ->
                    ChildNavigateItem(
                        title = child.name,
                        icon = child.icon,
                        selected = when (currentRoute) {
                            is Route.TagView -> {
                                (currentRoute as Route.TagView).subTagId == child.id
                            }
                            is Route.AddAppView -> {
                                (currentRoute as Route.AddAppView).subTagId == child.id
                            }
                            else -> {
                                false
                            }
                        },
                        onClick = {
                            navController.navigate(
                                Route.TagView(
                                    tagId = group.id,
                                    subTagId = child.id,
                                )
                            )
                        },
                        onDeleteClick = {
                            wantDeleteChildTag = child
                        },
                        onEditClick = {
                            wantEditChildTag = child to group.id
                        }
                    )
                }
                AddTagNavigateItem(
                    isParent = false,
                    selected = if (currentRoute is Route.AddTagView) {
                        (currentRoute as Route.AddTagView).tagId == group.id
                    } else {
                        false
                    },
                    onClick = {
                        navController.navigate(
                            Route.AddTagView(
                                tagId = group.id,
                            )
                        )
                    },
                )
            }
        }
        AddTagNavigateItem(
            isParent = true,
            selected = if (currentRoute is Route.AddTagView) {
                (currentRoute as Route.AddTagView).tagId == null
            } else {
                false
            },
            onClick = {
                navController.navigate(
                    Route.AddTagView(
                        tagId = null,
                    )
                )
            },
        )
    }

    if (wantDeleteParentTag != null) {
        DeleteParentTagDialog(
            id = wantDeleteParentTag!!.id,
            tagName = wantDeleteParentTag!!.name,
            onDismissRequest = {
                wantDeleteParentTag = null
            },
            onDelete = {
                viewModel.deleteParentTag(it)
            }
        )
    }

    if (wantDeleteChildTag != null) {
        DeleteChildTagDialog(
            id = wantDeleteChildTag!!.id,
            tagName = wantDeleteChildTag!!.name,
            onDismissRequest = {
                wantDeleteChildTag = null
            },
            onDelete = {
                viewModel.deleteChildTag(it)
            }
        )
    }
    if (wantEditParentTag != null) {
        EditParentTagDialog(
            tagId = wantEditParentTag!!.id,
            tagName = wantEditParentTag!!.name,
            icon = wantEditParentTag!!.icon,
            onDismissRequest = {
                wantEditParentTag = null
            },
            onSaveClick = { id, name, icon ->
                viewModel.updateParentTag(id, name, icon)
            }
        )
    }

    if (wantEditChildTag != null) {
        EditChildTagDialog(
            tagId = wantEditChildTag!!.first.id,
            tagName = wantEditChildTag!!.first.name,
            icon = wantEditChildTag!!.first.icon,
            onDismissRequest = {
                wantEditChildTag = null
            },
            onSaveClick = { id, name, icon ->
                viewModel.updateChildTag(id, name, wantEditChildTag!!.second, icon)
            }
        )
    }
}