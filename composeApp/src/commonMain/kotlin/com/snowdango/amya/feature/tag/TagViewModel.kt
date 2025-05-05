package com.snowdango.amya.feature.tag

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snowdango.amya.model.AppsModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class TagViewModel(
    private val parentTagId: Long,
    private val childTagId: Long?
): ViewModel(), KoinComponent {

    val appsModel: AppsModel by inject()

    val appsData = if (childTagId != null) {
        appsModel.getAppsBySubTagId(parentTagId, childTagId)
    } else {
        appsModel.getAppsByTagId(parentTagId)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList(),
    )



}