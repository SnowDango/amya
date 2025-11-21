package com.snowdango.amya.feature.tag

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snowdango.amya.model.AppsModel
import com.snowdango.amya.model.TagModel
import com.snowdango.amya.platform.SubProcessBuilder
import com.snowdango.amya.track.Log
import compose.icons.TablerIcons
import compose.icons.tablericons.SortAscending
import compose.icons.tablericons.SortAscending2
import compose.icons.tablericons.SortDescending
import compose.icons.tablericons.SortDescending2
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class TagViewModel(
    private val parentTagId: Long,
    private val childTagId: Long?
) : ViewModel(), KoinComponent {

    val appsModel: AppsModel by inject()
    val tagModel: TagModel by inject()

    private val _appsData: Flow<List<AppsModel.AppData>> = if (childTagId != null) {
        Log.d("parentTagId: $parentTagId, childTagId: $childTagId")
        appsModel.getAppsBySubTagId(parentTagId, childTagId)
    } else {
        Log.d("parentTagId: $parentTagId, childTagId: $childTagId")
        appsModel.getAppsByTagId(parentTagId)
    }
    private val _orderType: MutableStateFlow<OrderType> = MutableStateFlow(OrderType.ID_ASC)
    val orderType: StateFlow<OrderType> = _orderType.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        initialValue = OrderType.ID_ASC
    )
    private val _sortedAppsData: Flow<List<AppsModel.AppData>> = combine(_appsData, _orderType) { apps, type ->
        when (type) {
            OrderType.ID_ASC -> apps.sortedByDescending { it.id }
            OrderType.ID_DESC -> apps.sortedBy { it.id }
            OrderType.NAME_ASC -> apps.sortedBy { it.name }
            OrderType.NAME_DESC -> apps.sortedByDescending { it.name }
        }
    }
    val sortedAppsData = _sortedAppsData.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )
    private val _tagList = tagModel.getAllGroup()
    val tagList = _tagList.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    fun exec(isRoot: Boolean, path: String, args: String?) {
        SubProcessBuilder.execBuilder(
            isRoot = isRoot,
            path = path,
            args = args,
        ).spawn()
    }

    fun setOrderType(type: OrderType) {
        viewModelScope.launch {
            _orderType.emit(type)
        }
    }

    fun updateApp(id: Long, name: String, path: String, args: String?, imageUrl: String, root: Boolean) {
        viewModelScope.launch {
            appsModel.updateApp(id, name, path, args, imageUrl, root)
        }
    }

    fun transferApp(id: Long, tagId: Long, subTagId: Long?) {
        viewModelScope.launch {
            appsModel.transferApp(id, tagId, subTagId)
        }
    }

    fun deleteApp(id: Long) {
        viewModelScope.launch {
            appsModel.delete(id)
        }
    }

    enum class OrderType(val value: String, val icon: ImageVector) {
        ID_ASC("Newest", TablerIcons.SortDescending),
        ID_DESC("Oldest", TablerIcons.SortAscending),
        NAME_ASC("Name ascending", TablerIcons.SortDescending2),
        NAME_DESC("Name descending", TablerIcons.SortAscending2),
    }
}
