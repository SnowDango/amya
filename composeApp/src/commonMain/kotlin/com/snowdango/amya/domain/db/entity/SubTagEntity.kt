package com.snowdango.amya.domain.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = SubTagEntity.TABLE_NAME)
data class SubTagEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = COLUMN_ID) val id: Long = 0,
    @ColumnInfo(name = COLUMN_NAME) val name: String,
    @ColumnInfo(name = COLUMN_TAG_ID) val tagId: Long,
) {
    companion object {
        const val TABLE_NAME = "sub_tags"
        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"
        const val COLUMN_TAG_ID = "tagId"
    }
}
