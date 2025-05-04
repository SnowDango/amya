package com.snowdango.amya.model

import com.snowdango.amya.domain.db.entity.AppsEntity
import com.snowdango.amya.repository.apps.AppsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AppsModel: KoinComponent {

    private val repository by inject<AppsRepository>()

    suspend fun insert(
        name: String,
        imageUrl: String,
        tagId: Long,
        subTagId: Long,
        path: String,
    ) {
        repository.insert(
            AppsEntity(
                name = name,
                imageUrl = imageUrl,
                tagId = tagId,
                subTagId = subTagId,
                path = path,
            )
        )
    }

    suspend fun getAll(): Flow<List<AppData>> {
        return repository.getAll().map { apps ->
            apps.map { app ->
                AppData(
                    id = app.id,
                    name = app.name,
                    imageUrl = app.imageUrl,
                    path = app.path,
                )
            }
        }
    }

    suspend fun getAppsByTagId(tagId: Long): Flow<List<AppData>> {
        return repository.getAppsByTagId(tagId).map { apps ->
            apps.map { app ->
                AppData(
                    id = app.id,
                    name = app.name,
                    imageUrl = app.imageUrl,
                    path = app.path,
                )
            }
        }
    }

    suspend fun getAppsBySubTagId(tagId: Long, subTagId: Long): Flow<List<AppData>> {
        return repository.getAppsBySubTagId(tagId, subTagId).map { apps ->
            apps.map { app ->
                AppData(
                    id = app.id,
                    name = app.name,
                    imageUrl = app.imageUrl,
                    path = app.path,
                )
            }
        }
    }


    data class AppData(
        val id: Long,
        val name: String,
        val imageUrl: String,
        val path: String,
    )

}