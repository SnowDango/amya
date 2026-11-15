package com.snowdango.amya.steam

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.snowdango.amya.domain.db.AppsDatabase
import com.snowdango.amya.domain.db.SettingsPreferences
import com.snowdango.amya.domain.db.entity.AppsEntity
import com.snowdango.amya.domain.db.entity.TagEntity
import com.snowdango.amya.repository.apps.AppsDataStore
import com.snowdango.amya.repository.apps.AppsRepository
import com.snowdango.amya.repository.settings.SettingsDataStore
import com.snowdango.amya.repository.settings.SettingsRepository
import com.snowdango.amya.repository.steam.SteamExcludedDataStore
import com.snowdango.amya.repository.steam.SteamExcludedRepository
import com.snowdango.amya.repository.tag.TagDataStore
import com.snowdango.amya.repository.tag.TagRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SteamSyncServiceTest {

    @get:Rule
    val temp = TemporaryFolder()

    private class FakeSource : SteamLibrarySource {
        var available = true
        var games = mutableListOf<SteamGame>()
        var protected = mutableSetOf<Long>()
        var fingerprintSuffix = 0
        var scanCount = 0

        override fun isAvailable() = available
        override fun fingerprint(): String? =
            if (available) games.joinToString { "${it.appId}" } + protected + fingerprintSuffix else null

        override fun scan(): SteamScanResult? {
            if (!available) return null
            scanCount++
            return SteamScanResult(games.toList(), games.map { it.appId }.toSet() + protected)
        }
    }

    private lateinit var database: AppsDatabase
    private lateinit var scope: CoroutineScope
    private lateinit var source: FakeSource
    private lateinit var appsRepository: AppsRepository
    private lateinit var tagRepository: TagRepository
    private lateinit var excludedRepository: SteamExcludedRepository
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var service: SteamSyncService

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder<AppsDatabase>()
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
        scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        source = FakeSource()
        appsRepository = AppsRepository(AppsDataStore(database))
        tagRepository = TagRepository(TagDataStore(database))
        excludedRepository = SteamExcludedRepository(SteamExcludedDataStore(database))
        settingsRepository = SettingsRepository(
            SettingsDataStore(
                SettingsPreferences(
                    PreferenceDataStoreFactory.create(scope = scope) { File(temp.root, "settings.preferences_pb") }
                )
            )
        )
        service = SteamSyncService(
            source = source,
            appsRepository = appsRepository,
            tagRepository = tagRepository,
            excludedRepository = excludedRepository,
            settingsRepository = settingsRepository,
            scope = scope,
        )
    }

    @After
    fun tearDown() {
        scope.cancel()
        database.close()
    }

    private fun game(appId: Long, imageUrl: String = SteamGame.cdnImageUrl(appId)) =
        SteamGame(appId, "Game$appId", imageUrl, "C:\\Steam\\steam.exe", "-applaunch $appId")

    private suspend fun steamApps() = appsRepository.getSteamApps().sortedBy { it.steamAppId }

    @Test
    fun disabledByDefault() = runBlocking<Unit> {
        source.games += game(100)
        assertEquals(SteamSyncService.SyncResult.Disabled, service.sync())
        assertTrue(steamApps().isEmpty())
    }

    @Test
    fun firstSyncCreatesSteamTagAndAddsGames() = runBlocking<Unit> {
        settingsRepository.setIsSteamSyncEnabled(true)
        source.games += listOf(game(100), game(200))

        assertEquals(SteamSyncService.SyncResult.Synced(added = 2, deleted = 0, imageUpdated = 0), service.sync())

        val tagId = assertNotNull(settingsRepository.getSteamTagId().first())
        assertEquals(SteamSyncService.STEAM_TAG_NAME, tagRepository.getById(tagId)?.name)
        val apps = steamApps()
        assertEquals(listOf(100L, 200L), apps.map { it.steamAppId })
        assertTrue(apps.all { it.tagId == tagId })
        assertEquals("C:\\Steam\\steam.exe", apps.first().path)
        assertEquals("-applaunch 100", apps.first().args)
    }

    @Test
    fun unchangedFingerprintSkipsScan() = runBlocking<Unit> {
        settingsRepository.setIsSteamSyncEnabled(true)
        source.games += game(100)
        service.sync()
        assertEquals(1, source.scanCount)
        assertEquals(SteamSyncService.SyncResult.Unchanged, service.sync())
        assertEquals(1, source.scanCount)
    }

    @Test
    fun uninstalledGameIsDeletedOnSecondCheck() = runBlocking<Unit> {
        settingsRepository.setIsSteamSyncEnabled(true)
        source.games += listOf(game(100), game(200))
        service.sync()

        source.games.removeAll { it.appId == 200L }
        assertEquals(SteamSyncService.SyncResult.Unchanged, service.sync())
        assertEquals(listOf(100L, 200L), steamApps().map { it.steamAppId })

        // 指紋は変わっていないが、未確定の削除候補があるので再確認する
        assertEquals(SteamSyncService.SyncResult.Synced(0, 1, 0), service.sync())
        assertEquals(listOf(100L), steamApps().map { it.steamAppId })
        // 自動削除は除外リストに入らない (再インストールしたら再登録される)
        assertTrue(excludedRepository.getAllAppIds().isEmpty())
    }

    @Test
    fun unavailableLibraryDoesNotDelete() = runBlocking<Unit> {
        settingsRepository.setIsSteamSyncEnabled(true)
        source.games += game(100)
        service.sync()

        source.games.clear()
        source.protected += 100L
        repeat(3) { service.sync(force = true) }
        assertEquals(listOf(100L), steamApps().map { it.steamAppId })
    }

    @Test
    fun steamNotFoundDoesNothing() = runBlocking<Unit> {
        settingsRepository.setIsSteamSyncEnabled(true)
        source.games += game(100)
        service.sync()

        source.available = false
        repeat(3) { assertEquals(SteamSyncService.SyncResult.SteamNotFound, service.sync(force = true)) }
        assertEquals(listOf(100L), steamApps().map { it.steamAppId })
    }

    @Test
    fun excludedGameIsNotReAdded() = runBlocking<Unit> {
        settingsRepository.setIsSteamSyncEnabled(true)
        source.games += listOf(game(100), game(200))
        service.sync()

        // ユーザーが手動で削除 (AppsModel.delete 相当)
        val app200 = steamApps().first { it.steamAppId == 200L }
        excludedRepository.insert(200)
        appsRepository.delete(app200.id)

        source.fingerprintSuffix++
        service.sync()
        assertEquals(listOf(100L), steamApps().map { it.steamAppId })
    }

    @Test
    fun movedGameStaysInUserTag() = runBlocking<Unit> {
        settingsRepository.setIsSteamSyncEnabled(true)
        source.games += game(100)
        service.sync()

        val userTagId = tagRepository.insert(TagEntity(name = "Favorite", icon = "Star"))
        val app = steamApps().first()
        appsRepository.transferApp(app.id, userTagId, null)

        source.fingerprintSuffix++
        service.sync()
        val apps = steamApps()
        assertEquals(1, apps.size)
        assertEquals(userTagId, apps.first().tagId)
    }

    @Test
    fun deletedSteamTagDisablesSync() = runBlocking<Unit> {
        settingsRepository.setIsSteamSyncEnabled(true)
        source.games += game(100)
        service.sync()
        val tagId = assertNotNull(settingsRepository.getSteamTagId().first())

        tagRepository.deleteParentTag(tagId) // CASCADEでアプリも消える
        source.fingerprintSuffix++
        assertEquals(SteamSyncService.SyncResult.TagDeleted, service.sync())
        assertFalse(settingsRepository.getIsSteamSyncEnabled().first())
        assertNull(settingsRepository.getSteamTagId().first())
        assertTrue(steamApps().isEmpty())

        // 再度ONにするとタグを作り直して登録する
        settingsRepository.setIsSteamSyncEnabled(true)
        assertEquals(SteamSyncService.SyncResult.Synced(1, 0, 0), service.sync())
        assertNotNull(settingsRepository.getSteamTagId().first())
    }

    @Test
    fun manualAppsAreNeverTouched() = runBlocking<Unit> {
        settingsRepository.setIsSteamSyncEnabled(true)
        val tagId = tagRepository.insert(TagEntity(name = "Games", icon = "Star"))
        appsRepository.insert(
            AppsEntity(
                name = "Manual",
                imageUrl = "/not/exists.png",
                tagId = tagId,
                subTagId = null,
                path = "C:\\a.exe",
            )
        )
        service.sync()
        source.fingerprintSuffix++
        service.sync()
        val all = appsRepository.getAll().first()
        val manual = all.single { it.steamAppId == null }
        assertEquals("/not/exists.png", manual.imageUrl)
    }

    @Test
    fun brokenImageIsFixed() = runBlocking<Unit> {
        settingsRepository.setIsSteamSyncEnabled(true)
        val oldImage = File(temp.root, "old/library_600x900.jpg").apply {
            parentFile.mkdirs()
            writeText("x")
        }
        source.games += game(100, imageUrl = oldImage.absolutePath)
        service.sync()

        // Steamがアートワークを更新して古いファイルが消えた (manifestは変わらない)
        val newImage = File(temp.root, "new/library_600x900.jpg").apply {
            parentFile.mkdirs()
            writeText("x")
        }
        oldImage.delete()
        source.games[0] = game(100, imageUrl = newImage.absolutePath)

        val result = service.sync()
        assertIs<SteamSyncService.SyncResult.Synced>(result)
        assertEquals(newImage.absolutePath, steamApps().first().imageUrl)
    }
}

/**
 * 実機のSteamを使った結合テスト (Steamが無い環境ではスキップ)。DBはインメモリ
 */
class SteamSyncServiceRealSteamTest {

    @get:Rule
    val temp = TemporaryFolder()

    @Test
    fun syncWithRealSteam() = runBlocking<Unit> {
        val source = DesktopSteamLibrarySource()
        org.junit.Assume.assumeTrue("Steam is not installed", source.isAvailable())

        val database = Room.inMemoryDatabaseBuilder<AppsDatabase>()
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        try {
            val appsRepository = AppsRepository(AppsDataStore(database))
            val settingsRepository = SettingsRepository(
                SettingsDataStore(
                    SettingsPreferences(
                        PreferenceDataStoreFactory.create(scope = scope) { File(temp.root, "settings.preferences_pb") }
                    )
                )
            )
            val service = SteamSyncService(
                source = source,
                appsRepository = appsRepository,
                tagRepository = TagRepository(TagDataStore(database)),
                excludedRepository = SteamExcludedRepository(SteamExcludedDataStore(database)),
                settingsRepository = settingsRepository,
                scope = scope,
            )
            settingsRepository.setIsSteamSyncEnabled(true)

            val first = service.sync()
            println("first sync: $first")
            assertIs<SteamSyncService.SyncResult.Synced>(first)
            assertEquals(SteamSyncService.SyncResult.Unchanged, service.sync())

            val apps = appsRepository.getSteamApps()
            apps.forEach { println("${it.steamAppId}\t${it.name}\t${it.path} ${it.args}\t${it.imageUrl}") }
            assertTrue(apps.isNotEmpty())
            assertTrue(apps.all { File(it.path).isFile || it.path == "/usr/bin/xdg-open" })
        } finally {
            scope.cancel()
            database.close()
        }
    }
}
