package com.snowdango.amya.model

import androidx.compose.ui.graphics.vector.ImageVector
import com.snowdango.amya.domain.db.entity.SubTagEntity
import com.snowdango.amya.domain.db.entity.TagEntity
import com.snowdango.amya.repository.apps.AppsRepository
import com.snowdango.amya.repository.settings.SettingsRepository
import com.snowdango.amya.repository.steam.SteamExcludedRepository
import com.snowdango.amya.repository.tag.TagRepository
import com.snowdango.amya.steam.SteamSyncService
import compose.icons.AllIcons
import compose.icons.TablerIcons
import compose.icons.tablericons.FileAlert
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class TagModel : KoinComponent {

    val repository: TagRepository by inject()
    private val appsRepository: AppsRepository by inject()
    private val settingsRepository: SettingsRepository by inject()
    private val steamExcludedRepository: SteamExcludedRepository by inject()
    private val steamSyncService: SteamSyncService by inject()

    suspend fun insertParentTag(
        tagName: String,
        icon: ImageVector,
    ): Long {
        return repository.insert(
            TagEntity(
                name = tagName,
                icon = icon.name,
            )
        )
    }

    suspend fun insertChildTag(
        tagName: String,
        parentTagId: Long,
        icon: ImageVector,
    ): Long {
        return repository.insert(
            SubTagEntity(
                name = tagName,
                tagId = parentTagId,
                icon = icon.name,
            )
        )
    }

    fun getAllGroup(): Flow<List<ParentTag>> {
        return combine(
            repository.getAllGroup(),
            appsRepository.getAll(),
            settingsRepository.getIsSteamSyncEnabled(),
            settingsRepository.getSteamTagId(),
        ) { tagList, apps, isSteamSyncEnabled, steamTagId ->
            // Steam同期がOFFの時はSteamタグを表示しない (手動で登録したアプリが入っている場合は表示する)
            val hideSteamTag = !isSteamSyncEnabled &&
                steamTagId != null &&
                apps.none { it.tagId == steamTagId && it.steamAppId == null }
            tagList.filterNot { hideSteamTag && it.parentTag.id == steamTagId }.map {
                ParentTag(
                    id = it.parentTag.id,
                    name = it.parentTag.name,
                    icon = TablerIcons.AllIcons.find { icon ->
                        icon.name == it.parentTag.icon
                    } ?: TablerIcons.FileAlert,
                    childTag = it.subTags?.map { child ->
                        ParentTag.ChildTag(
                            id = child.id,
                            name = child.name,
                            icon = TablerIcons.AllIcons.find { icon ->
                                icon.name == child.icon
                            } ?: TablerIcons.FileAlert
                        )
                    } ?: emptyList()
                )
            }
        }
    }

    suspend fun updateParentTag(
        id: Long,
        tagName: String,
        icon: ImageVector,
    ) {
        repository.update(
            TagEntity(
                id = id,
                name = tagName,
                icon = icon.name,
            )
        )
    }

    suspend fun updateChildTag(
        id: Long,
        tagName: String,
        parentTagId: Long,
        icon: ImageVector,
    ) {
        repository.update(
            SubTagEntity(
                id = id,
                name = tagName,
                tagId = parentTagId,
                icon = icon.name,
            )
        )
    }

    suspend fun deleteParentTag(id: Long) {
        val isSteamTag = settingsRepository.getSteamTagId().first() == id
        if (!isSteamTag) {
            // タグと一緒に削除されるSteamのゲームも、手動で削除した時と同様に次回以降の同期で再登録しない
            // (Steamタグ自体の削除は同期OFFとして扱い、再度ONにした時は登録し直す)
            appsRepository.getSteamApps()
                .filter { it.tagId == id }
                .forEach { app -> app.steamAppId?.let { steamExcludedRepository.insert(it) } }
        }
        repository.deleteParentTag(id)
        if (isSteamTag) {
            steamSyncService.onSteamTagDeleted()
        }
    }

    suspend fun deleteChildTag(id: Long) {
        repository.deleteChildTag(id)
    }

    data class ParentTag(
        val id: Long,
        val name: String,
        val childTag: List<ChildTag>,
        val icon: ImageVector,
    ) {
        data class ChildTag(
            val id: Long,
            val name: String,
            val icon: ImageVector,
        )
    }
}
