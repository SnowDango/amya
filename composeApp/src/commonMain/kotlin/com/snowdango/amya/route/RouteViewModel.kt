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
): ViewModel(), KoinComponent {

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
            }catch (ce: CancellationException){
                throw ce
            }catch (th: Throwable) {
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
            }catch (ce: CancellationException){
                throw ce
            }catch (th: Throwable) {
                Log.e(th.message.toString())
            }
        }
    }

    fun deleteParentTag(tagId: Long) {
        viewModelScope.launch {
            val route = currentRoute.value
            try {
                if (currentRoute.value is Route.TagView) {
                    if ((currentRoute.value as Route.TagView).tagId == tagId) {
                        navController.navigate(Route.AllView)
                    }
                } else if (currentRoute.value is Route.AddTagView) {
                    if ((currentRoute.value as Route.AddTagView).tagId == tagId) {
                        navController.navigate(Route.AllView)
                    }
                } else if (currentRoute.value is Route.AddAppView) {
                    if ((currentRoute.value as Route.AddAppView).tagId == tagId) {
                        navController.navigate(Route.AllView)
                    }
                }
                tagModel.deleteParentTag(tagId)
            }catch (ce: CancellationException){
                throw ce
            }catch (th: Throwable) {
                Log.e(th.message.toString())
                route?.let {
                    navController.navigate(route)
                }
            }
        }
    }

    fun deleteChildTag(tagId: Long) {
        viewModelScope.launch {
            val route = currentRoute.value
            try {
                if (currentRoute.value is Route.TagView) {
                    if ((currentRoute.value as Route.TagView).subTagId == tagId) {
                        navController.navigate(
                            (currentRoute.value as Route.TagView).copy(subTagId = null)
                        )
                    }
                } else if (currentRoute.value is Route.AddAppView) {
                    if ((currentRoute.value as Route.AddAppView).subTagId == tagId) {
                        navController.navigate(
                            Route.TagView(
                                tagId = (currentRoute.value as Route.AddAppView).tagId,
                                subTagId = null
                            )
                        )
                    }
                }
                tagModel.deleteChildTag(tagId)
            }catch (ce: CancellationException) {
                throw ce
            }catch (th: Throwable) {
                Log.e(th.message.toString())
                route?.let {
                    navController.navigate(route)
                }
            }
        }
    }

}