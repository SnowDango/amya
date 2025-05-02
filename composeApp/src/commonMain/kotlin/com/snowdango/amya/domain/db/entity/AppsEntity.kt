package com.snowdango.amya.domain.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity(tableName = AppsEntity.TABLE_NAME, indices = [Index(value = [AppsEntity.COLUMN_TAG_ID, AppsEntity.COLUMN_SUB_TAG_ID])])
data class AppsEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = COLUMN_ID) val id: Long = 0,
    @ColumnInfo(name = COLUMN_NAME) val name: String,
    @ColumnInfo(name = COLUMN_IMAGE_URL) val imageUrl: String,
    @ColumnInfo(name = COLUMN_TAG_ID) val tagId: Long,
    @ColumnInfo(name = COLUMN_SUB_TAG_ID) val subTagId: Long?,
    @ColumnInfo(name = COLUMN_PATH) val path: String,
) {
    companion object {
        const val TABLE_NAME = "apps"
        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"
        const val COLUMN_IMAGE_URL = "imageUrl"
        const val COLUMN_TAG_ID = "tagId"
        const val COLUMN_SUB_TAG_ID = "subTagId"
        const val COLUMN_PATH = "path"
    }
}
