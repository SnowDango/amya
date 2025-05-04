package com.snowdango.amya.route

import androidx.compose.material.icons.Icons
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snowdango.amya.model.TagModel
import compose.icons.AllIcons
import compose.icons.TablerIcons
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.logging.Level
import java.util.logging.Logger

class RouteViewModel: ViewModel(), KoinComponent {

    private val tagModel: TagModel by inject()

    private val _tagGroup = tagModel.getAllGroup()
    val tagGroup = _tagGroup.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )


}