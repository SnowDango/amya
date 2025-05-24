package com.snowdango.amya.feature.all

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snowdango.amya.feature.tag.TagViewModel
import com.snowdango.amya.model.AppsModel
import com.snowdango.amya.platform.SubProcessBuilder
import compose.icons.TablerIcons
import compose.icons.tablericons.SortAscending
import compose.icons.tablericons.SortAscending2
import compose.icons.tablericons.SortDescending
import compose.icons.tablericons.SortDescending2
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AllViewModel: ViewModel(), KoinComponent {

    val appsModel: AppsModel by inject()

    private val _appsData: Flow<List<AppsModel.AppData>> = appsModel.getAll()
    private val _orderType: MutableStateFlow<TagViewModel.OrderType> = MutableStateFlow(TagViewModel.OrderType.ID_ASC)
    val orderType: StateFlow<TagViewModel.OrderType> = _orderType.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        initialValue = TagViewModel.OrderType.ID_ASC
    )
    private val _sortedAppsData: Flow<List<AppsModel.AppData>> = combine(_appsData, _orderType) { apps, type ->
        when(type) {
            TagViewModel.OrderType.ID_ASC -> apps.sortedByDescending { it.id }
            TagViewModel.OrderType.ID_DESC -> apps.sortedBy { it.id }
            TagViewModel.OrderType.NAME_ASC -> apps.sortedBy { it.name }
            TagViewModel.OrderType.NAME_DESC -> apps.sortedByDescending { it.name }
        }
    }
    val sortedAppsData = _sortedAppsData.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    fun exec(path: String) {
        SubProcessBuilder.execBuilder(
            path = path
        ).spawn()
    }

    fun setOrderType(type: TagViewModel.OrderType) {
        viewModelScope.launch {
            _orderType.emit(type)
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