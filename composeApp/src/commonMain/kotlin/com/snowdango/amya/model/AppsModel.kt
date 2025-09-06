package com.snowdango.amya.model

import com.snowdango.amya.domain.db.entity.AppsEntity
import com.snowdango.amya.repository.apps.AppsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AppsModel : KoinComponent {

    private val repository by inject<AppsRepository>()

    suspend fun insert(
        name: String,
        imageUrl: String,
        tagId: Long,
        subTagId: Long?,
        path: String,
        args: String?,
    ) {
        repository.insert(
            AppsEntity(
                name = name,
                imageUrl = imageUrl,
                tagId = tagId,
                subTagId = subTagId,
                path = path,
                args = args,
            )
        )
    }

    fun getAll(): Flow<List<AppData>> {
        return repository.getAll().map { apps ->
            apps.map { app ->
                AppData(
                    id = app.id,
                    name = app.name,
                    imageUrl = app.imageUrl,
                    path = app.path,
                    args = app.args,
                    tagId = app.tagId,
                    subTagId = app.subTagId,
                )
            }
        }
    }

    fun getAppsByTagId(tagId: Long): Flow<List<AppData>> {
        return repository.getAppsByTagId(tagId).map { apps ->
            apps.map { app ->
                AppData(
                    id = app.id,
                    name = app.name,
                    imageUrl = app.imageUrl,
                    path = app.path,
                    args = app.args,
                    tagId = app.tagId,
                    subTagId = app.subTagId,
                )
            }
        }
    }

    fun getAppsBySubTagId(tagId: Long, subTagId: Long): Flow<List<AppData>> {
        return repository.getAppsBySubTagId(tagId, subTagId).map { apps ->
            apps.map { app ->
                AppData(
                    id = app.id,
                    name = app.name,
                    imageUrl = app.imageUrl,
                    path = app.path,
                    args = app.args,
                    tagId = app.tagId,
                    subTagId = app.subTagId,
                )
            }
        }
    }

    suspend fun updateApp(id: Long, name: String, path: String, args: String?, imageUrl: String) {
        repository.updateApp(id, name, path, args, imageUrl)
    }

    suspend fun transferApp(id: Long, tagId: Long, subTagId: Long?) {
        repository.transferApp(id, tagId, subTagId)
    }

    suspend fun delete(id: Long) {
        repository.delete(id)
    }

    data class AppData(
        val id: Long,
        val name: String,
        val imageUrl: String,
        val path: String,
        val args: String?,
        val tagId: Long,
        val subTagId: Long?,
    )
}
