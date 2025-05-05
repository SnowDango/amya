package com.snowdango.amya


import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.snowdango.amya.feature.addtag.AddTagViewScreen
import com.snowdango.amya.feature.all.AllViewScreen
import com.snowdango.amya.feature.setting.SettingViewScreen
import com.snowdango.amya.feature.tag.TagViewScreen
import com.snowdango.amya.route.Route
import com.snowdango.amya.route.SideTabScreen
import com.snowdango.amya.ui.AmyaTheme

@Composable
@Preview
fun App() {
    AmyaTheme {
        val navController = rememberNavController()
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            SideTabScreen(
                navController = navController,
            )
            Scaffold(
                modifier = Modifier.fillMaxSize()
                    .weight(1f),
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .padding(paddingValues)
                        .padding(all = 16.dp)
                        .fillMaxSize()
                        .background(
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            shape = RoundedCornerShape(16.dp),
                        ),
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = Route.AllView,
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        composable<Route.AllView>{
                            AllViewScreen()
                        }
                        composable<Route.TagView> { backStackEntry ->
                            val route = backStackEntry.toRoute<Route.TagView>()
                            TagViewScreen(
                                tagId = route.tagId,
                                subTagId = route.subTagId,
                            )
                        }
                        composable<Route.AddTagView> { backStackEntry ->
                            val route = backStackEntry.toRoute<Route.AddTagView>()
                            AddTagViewScreen(
                                parentId = route.tagId,
                                navigateTag = { parentId, subTagId ->
                                    navController.navigate(
                                        Route.TagView(
                                            tagId = parentId,
                                            subTagId = subTagId,
                                        )
                                    )
                                }
                            )
                        }
                        composable<Route.SettingView> {
                            SettingViewScreen()
                        }
                    }
                }
            }
        }
    }
}