package com.snowdango.amya.domain.db

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import com.snowdango.amya.domain.db.dao.AppsDao
import com.snowdango.amya.domain.db.dao.SubTagDao
import com.snowdango.amya.domain.db.dao.TagDao
import com.snowdango.amya.domain.db.entity.AppsEntity
import com.snowdango.amya.domain.db.entity.SubTagEntity
import com.snowdango.amya.domain.db.entity.TagEntity
import kotlinx.coroutines.Dispatchers

@Database(entities = [AppsEntity::class, TagEntity::class, SubTagEntity::class], version = 4, exportSchema = false)
@ConstructedBy(AppsDatabaseConstructor::class)
abstract class AppsDatabase : RoomDatabase() {

    abstract fun appsDao(): AppsDao
    abstract fun tagDao(): TagDao
    abstract fun subTagDao(): SubTagDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AppsDatabaseConstructor : RoomDatabaseConstructor<AppsDatabase> {
    override fun initialize(): AppsDatabase
}

fun RoomDatabase.Builder<AppsDatabase>.addCommonOptions(): RoomDatabase.Builder<AppsDatabase> {
    return this
        .addMigrations(MIGRATION_2_3)
        .addMigrations(MIGRATION_3_4)
        .fallbackToDestructiveMigrationOnDowngrade(true)
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL(
            "ALTER TABLE ${AppsEntity.TABLE_NAME} ADD COLUMN ${AppsEntity.COLUMN_ARGS} TEXT DEFAULT NULL"
        )
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL(
            "ALTER TABLE ${AppsEntity.TABLE_NAME} ADD COLUMN ${AppsEntity.COLUMN_ROOT} INTEGER NOT NULL DEFAULT(0)"
        )
    }
}
