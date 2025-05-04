package com.snowdango.amya.domain.db.entity.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.snowdango.amya.domain.db.entity.SubTagEntity
import com.snowdango.amya.domain.db.entity.TagEntity

data class TagGroupEntity(
    @Embedded val parentTag: TagEntity,

    @Relation(
        parentColumn = TagEntity.COLUMN_ID,
        entityColumn = SubTagEntity.COLUMN_TAG_ID,
        entity = SubTagEntity::class
    )
    val subTags: List<SubTagEntity>?
)
