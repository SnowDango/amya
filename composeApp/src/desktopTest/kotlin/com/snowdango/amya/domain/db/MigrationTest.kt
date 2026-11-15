package com.snowdango.amya.domain.db

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import com.snowdango.amya.domain.db.entity.AppsEntity
import com.snowdango.amya.domain.db.entity.SteamExcludedEntity
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertNull

class MigrationTest {

    @get:Rule
    val temp = TemporaryFolder()

    /** version 4 時点のスキーマ (Roomの生成コードから取得) */
    private fun createVersion4Database(file: File) {
        val connection = BundledSQLiteDriver().open(file.absolutePath)
        try {
            connection.execSQL(
                "CREATE TABLE IF NOT EXISTS `apps` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, " +
                    "`imageUrl` TEXT NOT NULL, `tagId` INTEGER NOT NULL, `subTagId` INTEGER, `path` TEXT NOT NULL, `args` TEXT, " +
                    "`root` INTEGER NOT NULL, FOREIGN KEY(`tagId`) REFERENCES `tags`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , " +
                    "FOREIGN KEY(`subTagId`) REFERENCES `sub_tags`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL )"
            )
            connection.execSQL("CREATE INDEX IF NOT EXISTS `index_apps_tagId` ON `apps` (`tagId`)")
            connection.execSQL("CREATE INDEX IF NOT EXISTS `index_apps_subTagId` ON `apps` (`subTagId`)")
            connection.execSQL(
                "CREATE TABLE IF NOT EXISTS `tags` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `icon` TEXT NOT NULL)"
            )
            connection.execSQL("CREATE INDEX IF NOT EXISTS `index_tags_id` ON `tags` (`id`)")
            connection.execSQL(
                "CREATE TABLE IF NOT EXISTS `sub_tags` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, " +
                    "`tagId` INTEGER NOT NULL, `icon` TEXT NOT NULL)"
            )
            connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
            connection.execSQL(
                "INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '837b2aa0b709e8d079b4b691dffd6328')"
            )
            connection.execSQL("INSERT INTO tags (id, name, icon) VALUES (1, 'Games', 'DeviceGamepad')")
            connection.execSQL(
                "INSERT INTO apps (id, name, imageUrl, tagId, subTagId, path, args, root) " +
                    "VALUES (1, 'Manual App', 'https://example.com/a.png', 1, NULL, 'C:\\a.exe', NULL, 0)"
            )
            connection.execSQL("PRAGMA user_version = 4")
        } finally {
            connection.close()
        }
    }

    @Test
    fun migrate4To5() = runBlocking<Unit> {
        val file = File(temp.root, "amya_apps.db")
        createVersion4Database(file)

        val database = Room.databaseBuilder<AppsDatabase>(name = file.absolutePath)
            .addCommonOptions()
            .build()
        try {
            val appsDao = database.appsDao()
            // 既存データは手動登録として残る
            val existing = appsDao.getById(1)!!
            assertEquals("Manual App", existing.name)
            assertNull(existing.steamAppId)
            assertEquals(emptyList(), appsDao.getSteamApps())

            val steamApp = AppsEntity(
                name = "House Flipper",
                imageUrl = "x",
                tagId = 1,
                subTagId = null,
                path = "steam.exe",
                args = "-applaunch 613100",
                steamAppId = 613100,
            )
            appsDao.insert(steamApp)
            assertEquals(listOf(613100L), appsDao.getSteamApps().map { it.steamAppId })
            // 同じappIdは二重登録できない
            assertFails { appsDao.insert(steamApp) }

            val excludedDao = database.steamExcludedDao()
            excludedDao.insert(SteamExcludedEntity(10))
            excludedDao.insert(SteamExcludedEntity(10))
            assertEquals(listOf(10L), excludedDao.getAllAppIds())
        } finally {
            database.close()
        }
    }
}
