package com.snowdango.amya.route

import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.toRoute
import com.snowdango.amya.Log
import com.snowdango.amya.feature.all.AllViewScreen
import kotlinx.serialization.Serializable

sealed class Route {
    @Serializable
    data object AllView:  Route()
    @Serializable
    data class TagView(val tagId: Long, val subTagId: Long?): Route()
    @Serializable
    data object SettingView: Route()
    @Serializable
    data class AddTagView(val tagId: Long?): Route()

    companion object {
        @Composable
        fun fromNavBackStackEntry(navBackStackEntry: NavBackStackEntry?): Route? {
            if (navBackStackEntry?.destination?.route == null) return null
            val route = navBackStackEntry.destination.route!!
            return when {
                AllView.serializer().descriptor.serialName in route -> {
                    navBackStackEntry.toRoute<AllView>()
                }
                TagView.serializer().descriptor.serialName in route -> {
                    navBackStackEntry.toRoute<TagView>()
                }
                SettingView.serializer().descriptor.serialName in route -> {
                    navBackStackEntry.toRoute<SettingView>()
                }
                AddTagView.serializer().descriptor.serialName in route -> {
                    navBackStackEntry.toRoute<AddTagView>()
                }
                else -> null
            }
        }
    }

}