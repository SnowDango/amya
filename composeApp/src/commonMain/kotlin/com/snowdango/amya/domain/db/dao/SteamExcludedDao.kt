package com.snowdango.amya.domain.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.snowdango.amya.domain.db.entity.SteamExcludedEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SteamExcludedDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: SteamExcludedEntity)

    @Query("select ${SteamExcludedEntity.COLUMN_APP_ID} from ${SteamExcludedEntity.TABLE_NAME}")
    suspend fun getAllAppIds(): List<Long>

    @Query("select count(*) from ${SteamExcludedEntity.TABLE_NAME}")
    fun count(): Flow<Int>

    @Query("delete from ${SteamExcludedEntity.TABLE_NAME}")
    suspend fun deleteAll()
}
