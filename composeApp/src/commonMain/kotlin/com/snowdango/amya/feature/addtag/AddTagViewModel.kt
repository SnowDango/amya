package com.snowdango.amya.feature.addtag

import androidx.compose.runtime.MutableState
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snowdango.amya.model.TagModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AddTagViewModel : ViewModel(), KoinComponent {

    private val tagModel: TagModel by inject()

    private val _result: MutableStateFlow<CreateTagState> = MutableStateFlow(CreateTagState.None)
    val result: StateFlow<CreateTagState> = _result.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        _result.value
    )

    private suspend fun createTag(
        parentId: Long?,
        name: String,
        imageVector: ImageVector
    ) {
        try {
            if (parentId != null) {
                val tagId = tagModel.insertChildTag(name, parentId, imageVector)
                _result.emit(CreateTagState.Success(parentId, tagId))
            } else {
                val tagId = tagModel.insertParentTag(name, imageVector)
                _result.emit(CreateTagState.Success(tagId, null))
            }
        } catch (ce: CancellationException) {
            throw ce
        } catch (_: Throwable) {
            _result.emit(CreateTagState.Failure)
        }
    }

    fun checkAndCreateTag(
        parentId: Long?,
        name: String,
        imageVector: ImageVector?,
    ) {
        viewModelScope.launch {
            if (name.isBlank()) {
                _result.emit(CreateTagState.ValidationError("Name is empty"))
            }else if (imageVector == null){
                _result.emit(CreateTagState.ValidationError("Icon is empty"))
            }else{
                createTag(parentId, name, imageVector)
            }
        }
    }

    fun resetResult() {
        viewModelScope.launch {
            _result.emit(CreateTagState.None)
        }
    }


    sealed class CreateTagState {
        object None : CreateTagState()
        data class Success(val parentId: Long, val childId: Long?) : CreateTagState()
        object Failure : CreateTagState()
        data class ValidationError(val error: String) : CreateTagState()
    }

}