package com.snowdango.amya.feature.addapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snowdango.amya.model.AppsModel
import com.snowdango.amya.track.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AddAppViewModel : ViewModel(), KoinComponent {

    private val appsModel: AppsModel by inject()

    private val _result: MutableStateFlow<CreateAppState> = MutableStateFlow(CreateAppState.None)
    val result: StateFlow<CreateAppState> = _result.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        _result.value
    )

    private suspend fun createApp(
        parentId: Long,
        childId: Long?,
        name: String,
        filePath: String,
        args: String?,
        imageUrl: String,
        root: Boolean,
    ): Boolean {
        return try {
            appsModel.insert(
                name = name,
                imageUrl = imageUrl,
                tagId = parentId,
                subTagId = childId,
                path = filePath,
                args = args,
                root = root,
            )
            true
        } catch (ce: CancellationException) {
            throw ce
        } catch (th: Throwable) {
            Log.e("Failed to create app: ${th.message}")
            false
        }
    }

    fun checkAndCreateApp(
        parentId: Long,
        childId: Long?,
        name: String,
        filePath: String,
        args: String?,
        imageUrl: String,
        root: Boolean,
    ) {
        // 作成中・作成済みの場合は連打による二重登録を防ぐ
        if (createJob?.isActive == true || _result.value is CreateAppState.Success) return
        createJob = viewModelScope.launch {
            if (name.isBlank()) {
                _result.emit(CreateAppState.ValidationError("Name is empty"))
            } else if (filePath.isBlank()) {
                _result.emit(CreateAppState.ValidationError("File path is empty"))
            } else if (imageUrl.isBlank()) {
                _result.emit(CreateAppState.ValidationError("Image URL is empty"))
            } else {
                val isSuccess = createApp(
                    parentId = parentId,
                    childId = childId,
                    name = name,
                    filePath = filePath,
                    args = args,
                    imageUrl = imageUrl,
                    root = root,
                )
                _result.emit(if (isSuccess) CreateAppState.Success(parentId, childId) else CreateAppState.Failure)
            }
        }
    }

    private var createJob: Job? = null

    fun resetResult() {
        viewModelScope.launch {
            _result.emit(CreateAppState.None)
        }
    }

    sealed class CreateAppState {
        object None : CreateAppState()
        data class Success(val parentId: Long, val childId: Long?) : CreateAppState()
        object Failure : CreateAppState()
        data class ValidationError(val message: String) : CreateAppState()
    }
}
