package com.snowdango.amya.route

import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.snowdango.amya.platform.Log
import com.snowdango.amya.component.navigation.AddTagNavigateItem
import com.snowdango.amya.component.navigation.ChildNavigateItem
import com.snowdango.amya.component.navigation.ParentNavigateItem
import com.snowdango.amya.component.navigation.SideNavigation
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
}