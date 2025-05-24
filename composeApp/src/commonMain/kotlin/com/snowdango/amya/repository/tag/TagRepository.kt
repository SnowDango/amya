package com.snowdango.amya.repository.tag

import com.snowdango.amya.domain.db.entity.SubTagEntity
import com.snowdango.amya.domain.db.entity.TagEntity
import com.snowdango.amya.domain.db.entity.relation.TagGroupEntity
import kotlinx.coroutines.flow.Flow

class TagRepository(
    private val tagDataStore: TagDataStore,
) {

    suspend fun insert(tagEntity: TagEntity): Long {
        return tagDataStore.insert(tagEntity)
    }

    suspend fun insert(subTagEntity: SubTagEntity): Long {
        return tagDataStore.insert(subTagEntity)
    }

    fun getAllGroup(): Flow<List<TagGroupEntity>> {
        return tagDataStore.getAllGroup()
    }

    suspend fun update(tagEntity: TagEntity) {
        tagDataStore.update(tagEntity)
    }

    suspend fun update(subTagEntity: SubTagEntity) {
        tagDataStore.update(subTagEntity)
    }

    suspend fun deleteParentTag(id: Long) {
        tagDataStore.deleteParentTag(id)
    }

    suspend fun deleteChildTag(id: Long) {
        tagDataStore.deleteChildTag(id)
    }
}
