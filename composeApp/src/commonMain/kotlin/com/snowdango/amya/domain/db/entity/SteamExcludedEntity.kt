package com.snowdango.amya.domain.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Steam同期で自動追加しないappId。
 * Steam同期で登録されたアプリをユーザーが手動で削除した場合に記録する。
 */
@Entity(tableName = SteamExcludedEntity.TABLE_NAME)
data class SteamExcludedEntity(
    @PrimaryKey @ColumnInfo(name = COLUMN_APP_ID) val appId: Long,
) {
    companion object {
        const val TABLE_NAME = "steam_excluded"
        const val COLUMN_APP_ID = "appId"
    }
}
