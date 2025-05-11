package com.snowdango.amya.feature.setting


import amya.composeapp.generated.resources.Res
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.entity.Library
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
class LicensesViewModel: ViewModel() {


    private val _libraries: MutableStateFlow<ImmutableList<Library>> = MutableStateFlow(persistentListOf())
    val libraries = _libraries.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        initialValue = persistentListOf(),
    )

    init {
        viewModelScope.launch {
            val libs = Libs.Builder().withJson(
                Res.readBytes("files/aboutlibraries.json").decodeToString()
            ).build()
            _libraries.emit(libs.libraries)
        }
    }


}