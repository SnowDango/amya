package com.snowdango.amya.domain.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.snowdango.amya.domain.db.entity.AppsEntity.Companion.COLUMN_ID

@Entity(tableName = TagEntity.TABLE_NAME, indices = [Index(value = [TagEntity.COLUMN_ID])])
data class TagEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = COLUMN_ID) val id: Long = 0,
    @ColumnInfo(name = COLUMN_NAME) val name: String,
) {
    companion object {
        const val TABLE_NAME = "tags"
        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"
    }
}