package com.snowdango.amya.feature.tag

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snowdango.amya.model.AppsModel
import com.snowdango.amya.platform.Log
import com.snowdango.amya.platform.SubProcessBuilder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class TagViewModel(
    private val parentTagId: Long,
    private val childTagId: Long?
): ViewModel(), KoinComponent {

    val appsModel: AppsModel by inject()

    val appsData: Flow<List<AppsModel.AppData>> = if (childTagId != null) {
        Log.d("parentTagId: $parentTagId, childTagId: $childTagId")
        appsModel.getAppsBySubTagId(parentTagId, childTagId)
    } else {
        Log.d("parentTagId: $parentTagId, childTagId: $childTagId")
        appsModel.getAppsByTagId(parentTagId)
    }

    fun exec(path: String) {
        SubProcessBuilder.execBuilder(
            path = path
        ).spawn()
    }

    fun deleteApp(id: Long) {
        viewModelScope.launch {
            appsModel.delete(id)
        }
    }

}