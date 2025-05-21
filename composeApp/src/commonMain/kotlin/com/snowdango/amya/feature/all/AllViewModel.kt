package com.snowdango.amya.feature.all

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snowdango.amya.model.AppsModel
import com.snowdango.amya.platform.SubProcessBuilder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AllViewModel: ViewModel(), KoinComponent {

    val appsModel: AppsModel by inject()

    val appsData: Flow<List<AppsModel.AppData>> = appsModel.getAll()

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