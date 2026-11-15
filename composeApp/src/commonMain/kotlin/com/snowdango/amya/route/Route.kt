package com.snowdango.amya.route

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable

/**
 * サイドナビゲーションからの画面遷移。
 * 開始画面(AllView)より上を置き換えるため、遷移を繰り返してもバックスタックが積み上がらない
 */
fun NavController.navigateTopLevel(route: Route) {
    navigate(route) {
        popUpTo<Route.AllView>()
        launchSingleTop = true
    }
}

sealed class Route {
    @Serializable
    data object AllView : Route()

    @Serializable
    data class TagView(val tagId: Long, val subTagId: Long?) : Route()

    @Serializable
    data object SettingView : Route()

    @Serializable
    data class AddTagView(val tagId: Long?) : Route()

    @Serializable
    data class AddAppView(val tagId: Long, val subTagId: Long?) : Route()

    @Serializable
    data object LicenseView : Route()

    companion object {

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

                AddAppView.serializer().descriptor.serialName in route -> {
                    navBackStackEntry.toRoute<AddAppView>()
                }
                LicenseView.serializer().descriptor.serialName in route -> {
                    navBackStackEntry.toRoute<LicenseView>()
                }
                else -> null
            }
        }
    }
}
