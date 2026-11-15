package com.snowdango.amya.route

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.snowdango.amya.model.TagModel
import com.snowdango.amya.track.Log
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.coroutines.cancellation.CancellationException

class RouteViewModel(
    private val navController: NavController,
) : ViewModel(), KoinComponent {

    private val tagModel: TagModel by inject()

    private val _tagGroup = tagModel.getAllGroup()
    val tagGroup = _tagGroup.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    private val _currentRoute = navController.currentBackStackEntryFlow.map {
        Route.fromNavBackStackEntry(it)
    }
    val currentRoute = _currentRoute.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = Route.fromNavBackStackEntry(navController.currentBackStackEntry),
    )

    fun updateParentTag(
        tagId: Long,
        name: String,
        icon: ImageVector,
    ) {
        viewModelScope.launch {
            try {
                tagModel.updateParentTag(tagId, name, icon)
            } catch (ce: CancellationException) {
                throw ce
            } catch (th: Throwable) {
                Log.e(th.message.toString())
            }
        }
    }

    fun updateChildTag(
        tagId: Long,
        name: String,
        parentTagId: Long,
        icon: ImageVector,
    ) {
        viewModelScope.launch {
            try {
                tagModel.updateChildTag(tagId, name, parentTagId, icon)
            } catch (ce: CancellationException) {
                throw ce
            } catch (th: Throwable) {
                Log.e(th.message.toString())
            }
        }
    }

    fun deleteParentTag(tagId: Long) {
        viewModelScope.launch {
            try {
                // 削除するタグを表示中の場合は先に別の画面へ移動する
                val isShowingTag = when (val route = currentRoute.value) {
                    is Route.TagView -> route.tagId == tagId
                    is Route.AddTagView -> route.tagId == tagId
                    is Route.AddAppView -> route.tagId == tagId
                    else -> false
                }
                if (isShowingTag) {
                    navController.navigateTopLevel(Route.AllView)
                }
                tagModel.deleteParentTag(tagId)
            } catch (ce: CancellationException) {
                throw ce
            } catch (th: Throwable) {
                Log.e(th.message.toString())
            }
        }
    }

    fun deleteChildTag(tagId: Long) {
        viewModelScope.launch {
            try {
                // 削除するタグを表示中の場合は先に親タグの画面へ移動する
                when (val route = currentRoute.value) {
                    is Route.TagView -> if (route.subTagId == tagId) {
                        navController.navigateTopLevel(route.copy(subTagId = null))
                    }

                    is Route.AddAppView -> if (route.subTagId == tagId) {
                        navController.navigateTopLevel(Route.TagView(tagId = route.tagId, subTagId = null))
                    }

                    else -> Unit
                }
                tagModel.deleteChildTag(tagId)
            } catch (ce: CancellationException) {
                throw ce
            } catch (th: Throwable) {
                Log.e(th.message.toString())
            }
        }
    }
}
