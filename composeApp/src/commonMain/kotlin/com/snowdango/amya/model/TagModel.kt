package com.snowdango.amya.model

import androidx.compose.ui.graphics.vector.ImageVector
import com.snowdango.amya.domain.db.entity.SubTagEntity
import com.snowdango.amya.domain.db.entity.TagEntity
import com.snowdango.amya.repository.tag.TagRepository
import compose.icons.AllIcons
import compose.icons.TablerIcons
import compose.icons.tablericons.FileAlert
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class TagModel : KoinComponent {

    val repository: TagRepository by inject()

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
        return repository.getAllGroup().map { tagList ->
            tagList.map {
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
        repository.deleteParentTag(id)
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
