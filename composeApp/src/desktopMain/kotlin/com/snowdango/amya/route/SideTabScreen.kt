package com.snowdango.amya.route

import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.snowdango.amya.Log
import com.snowdango.amya.component.AddTagNavigateItem
import com.snowdango.amya.component.ChildNavigateItem
import com.snowdango.amya.component.ParentNavigateItem
import com.snowdango.amya.component.SideNavigation
import compose.icons.TablerIcons
import compose.icons.tablericons.Home
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SideTabScreen(
    navController: NavController,
    viewModel: RouteViewModel = koinViewModel()
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = Route.fromNavBackStackEntry(backStackEntry)
    val tagGroups = viewModel.tagGroup.collectAsStateWithLifecycle()

    LaunchedEffect(currentRoute) {
        Log.d(currentRoute.toString())
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
            onClick = {
                navController.navigate(
                    Route.AllView
                )
            },
        ) {}
        tagGroups.value.forEach { group ->
            ParentNavigateItem(
                title = group.name,
                icon = group.icon,
                selected = when (currentRoute) {
                    is Route.TagView -> {
                        Log.d("${currentRoute.tagId} == ${group.id}")
                        currentRoute.tagId == group.id
                    }

                    is Route.AddTagView -> {
                        currentRoute.tagId == group.id
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
            ) {
                group.childTag.forEach { child ->
                    ChildNavigateItem(
                        title = child.name,
                        icon = child.icon,
                        selected = if (currentRoute is Route.TagView) {
                            currentRoute.subTagId == child.id
                        } else {
                            false
                        },
                        onClick = {
                            navController.navigate(
                                Route.TagView(
                                    tagId = group.id,
                                    subTagId = child.id,
                                )
                            )
                        },
                    )
                }
                AddTagNavigateItem(
                    isParent = false,
                    selected = if (currentRoute is Route.AddTagView) {
                        currentRoute.tagId == group.id
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
                currentRoute.tagId == null
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
}