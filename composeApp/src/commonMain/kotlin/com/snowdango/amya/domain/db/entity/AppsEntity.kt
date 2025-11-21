package com.snowdango.amya.domain.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = AppsEntity.TABLE_NAME,
    indices = [
        Index(value = [AppsEntity.COLUMN_TAG_ID]),
        Index(value = [AppsEntity.COLUMN_SUB_TAG_ID]),
    ],
    foreignKeys = [
        ForeignKey(
            entity = TagEntity::class,
            parentColumns = arrayOf(TagEntity.COLUMN_ID),
            childColumns = arrayOf(AppsEntity.COLUMN_TAG_ID),
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = SubTagEntity::class,
            parentColumns = arrayOf(SubTagEntity.COLUMN_ID),
            childColumns = arrayOf(AppsEntity.COLUMN_SUB_TAG_ID),
            onDelete = ForeignKey.SET_NULL,
        )
    ]
)
data class AppsEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = COLUMN_ID) val id: Long = 0,
    @ColumnInfo(name = COLUMN_NAME) val name: String,
    @ColumnInfo(name = COLUMN_IMAGE_URL) val imageUrl: String,
    @ColumnInfo(name = COLUMN_TAG_ID) val tagId: Long,
    @ColumnInfo(name = COLUMN_SUB_TAG_ID) val subTagId: Long?,
    @ColumnInfo(name = COLUMN_PATH) val path: String,
    @ColumnInfo(name = COLUMN_ARGS) val args: String? = null,
    @ColumnInfo(name = COLUMN_ROOT) val root: Boolean = false,
) {
    companion object {
        const val TABLE_NAME = "apps"
        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"
        const val COLUMN_IMAGE_URL = "imageUrl"
        const val COLUMN_TAG_ID = "tagId"
        const val COLUMN_SUB_TAG_ID = "subTagId"
        const val COLUMN_PATH = "path"
        const val COLUMN_ARGS = "args"
        const val COLUMN_ROOT = "root"
    }
}
