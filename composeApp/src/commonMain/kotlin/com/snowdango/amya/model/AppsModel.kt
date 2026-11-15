package com.snowdango.amya.model

import com.snowdango.amya.domain.db.entity.AppsEntity
import com.snowdango.amya.repository.apps.AppsRepository
import com.snowdango.amya.repository.settings.SettingsRepository
import com.snowdango.amya.repository.steam.SteamExcludedRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AppsModel : KoinComponent {

    private val repository by inject<AppsRepository>()
    private val settingsRepository by inject<SettingsRepository>()
    private val steamExcludedRepository by inject<SteamExcludedRepository>()

    suspend fun insert(
        name: String,
        imageUrl: String,
        tagId: Long,
        subTagId: Long?,
        path: String,
        args: String?,
        root: Boolean,
    ) {
        repository.insert(
            AppsEntity(
                name = name,
                imageUrl = imageUrl,
                tagId = tagId,
                subTagId = subTagId,
                path = path,
                args = args,
                root = root,
            )
        )
    }

    fun getAll(): Flow<List<AppData>> {
        return repository.getAll().toVisibleAppData()
    }

    fun getAppsByTagId(tagId: Long): Flow<List<AppData>> {
        return repository.getAppsByTagId(tagId).toVisibleAppData()
    }

    fun getAppsBySubTagId(tagId: Long, subTagId: Long): Flow<List<AppData>> {
        return repository.getAppsBySubTagId(tagId, subTagId).toVisibleAppData()
    }

    suspend fun updateApp(id: Long, name: String, path: String, args: String?, imageUrl: String, root: Boolean) {
        repository.updateApp(id, name, path, args, imageUrl, root)
    }

    suspend fun transferApp(id: Long, tagId: Long, subTagId: Long?) {
        repository.transferApp(id, tagId, subTagId)
    }

    /**
     * アプリを削除する。Steam同期で登録したアプリの場合は、次回以降の同期で再登録しないよう除外リストに記録する
     */
    suspend fun delete(id: Long) {
        repository.getById(id)?.steamAppId?.let { steamExcludedRepository.insert(it) }
        repository.delete(id)
    }

    /**
     * Steam同期がOFFの時は、Steam同期で登録したアプリを表示しない
     */
    private fun Flow<List<AppsEntity>>.toVisibleAppData(): Flow<List<AppData>> {
        return combine(this, settingsRepository.getIsSteamSyncEnabled()) { apps, isSteamSyncEnabled ->
            apps.filter { isSteamSyncEnabled || it.steamAppId == null }
                .map { it.toAppData() }
        }
    }

    private fun AppsEntity.toAppData() = AppData(
        id = id,
        name = name,
        imageUrl = imageUrl,
        path = path,
        args = args,
        tagId = tagId,
        subTagId = subTagId,
        root = root,
        steamAppId = steamAppId,
    )

    data class AppData(
        val id: Long,
        val name: String,
        val imageUrl: String,
        val path: String,
        val args: String?,
        val tagId: Long,
        val subTagId: Long?,
        val root: Boolean,
        val steamAppId: Long? = null,
    )
}
