package com.snowdango.amya.steam

import com.snowdango.amya.steam.SteamFixtures.LibraryEntry
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SteamLibraryReaderTest {

    @get:Rule
    val temp = TemporaryFolder()

    private val launcher = SteamLauncher.Linux("/usr/bin/xdg-open")

    private fun reader(root: File) = SteamLibraryReader(SteamInstallation(root, launcher))

    @Test
    fun scanMultipleLibraries() {
        val root = temp.newFolder("Steam")
        val external = temp.newFolder("SteamLibrary")
        SteamFixtures.createSteamRoot(
            root,
            LibraryEntry(root, listOf(228980, 2079120)),
            LibraryEntry(external, listOf(613100)),
        )
        SteamFixtures.writeManifest(root, 228980, "Steamworks Common Redistributables")
        SteamFixtures.writeManifest(root, 2079120, "Warudo")
        SteamFixtures.writeManifest(external, 613100, "House Flipper")

        val result = assertNotNull(reader(root).scan())
        assertEquals(setOf(2079120L, 613100L), result.installedGames.map { it.appId }.toSet())
        val houseFlipper = result.installedGames.first { it.appId == 613100L }
        assertEquals("House Flipper", houseFlipper.name)
        assertEquals("/usr/bin/xdg-open", houseFlipper.launchPath)
        assertEquals("steam://rungameid/613100", houseFlipper.launchArgs)
        // 除外対象でもmanifestがあれば保護対象
        assertEquals(setOf(228980L, 2079120L, 613100L), result.protectedAppIds)
    }

    @Test
    fun oldLibraryFoldersFormat() {
        val root = temp.newFolder("Steam")
        val external = temp.newFolder("SteamLibrary")
        File(root, "steamapps").mkdirs()
        File(root, "steamapps/libraryfolders.vdf").writeText(
            """
            "LibraryFolders"
            {
            	"TimeNextStatsReport"		"1600000000"
            	"ContentStatsID"		"-1234"
            	"1"		"${external.absolutePath.replace("\\", "\\\\")}"
            }
            """.trimIndent()
        )
        SteamFixtures.writeManifest(root, 1, "RootGame")
        SteamFixtures.writeManifest(external, 2, "ExternalGame")

        val libraries = assertNotNull(reader(root).libraries())
        assertEquals(2, libraries.size)
        assertEquals(setOf(1L, 2L), assertNotNull(reader(root).scan()).installedGames.map { it.appId }.toSet())
    }

    @Test
    fun unavailableLibraryAppsAreProtected() {
        val root = temp.newFolder("Steam")
        val unmounted = File(temp.root, "mnt/games/SteamLibrary") // 存在しない
        SteamFixtures.createSteamRoot(
            root,
            LibraryEntry(root, emptyList()),
            LibraryEntry(unmounted, listOf(500, 600)),
        )
        val result = assertNotNull(reader(root).scan())
        assertTrue(result.installedGames.isEmpty())
        assertEquals(setOf(500L, 600L), result.protectedAppIds)
        // 含まれるゲームが分かっているので、他のライブラリのゲームは削除してよい
        assertTrue(result.isComplete)
    }

    @Test
    fun unavailableOldFormatLibraryMakesScanIncomplete() {
        // 古い形式ではライブラリに含まれるゲームが分からないため、ドライブが外れている間は削除しない
        val root = temp.newFolder("Steam")
        val unmounted = File(temp.root, "mnt/games/SteamLibrary") // 存在しない
        File(root, "steamapps").mkdirs()
        File(root, "steamapps/libraryfolders.vdf").writeText(
            """
            "LibraryFolders"
            {
            	"1"		"${unmounted.absolutePath.replace("\\", "\\\\")}"
            }
            """.trimIndent()
        )
        SteamFixtures.writeManifest(root, 1, "RootGame")

        val result = assertNotNull(reader(root).scan())
        assertEquals(listOf(1L), result.installedGames.map { it.appId })
        assertFalse(result.isComplete)
    }

    @Test
    fun notFullyInstalledGamesAreProtectedButNotInstalled() {
        val root = temp.newFolder("Steam")
        SteamFixtures.createSteamRoot(root, LibraryEntry(root, listOf(10, 20, 30)))
        SteamFixtures.writeManifest(root, 10, "Downloading", stateFlags = 1026) // 初回ダウンロード中
        SteamFixtures.writeManifest(root, 20, "Updating", stateFlags = 6) // インストール済み+更新待ち
        File(root, "steamapps/appmanifest_30.acf").writeText("\"AppState\" { broken")

        val result = assertNotNull(reader(root).scan())
        assertEquals(listOf(20L), result.installedGames.map { it.appId })
        assertEquals(setOf(10L, 20L, 30L), result.protectedAppIds)
    }

    @Test
    fun protonAndRuntimesAreExcluded() {
        val root = temp.newFolder("Steam")
        SteamFixtures.createSteamRoot(root, LibraryEntry(root, emptyList()))
        SteamFixtures.writeManifest(root, 1493710, "Proton Experimental")
        SteamFixtures.writeManifest(root, 2805730, "Proton 9.0") // IDリストに無くても名前で除外
        SteamFixtures.writeManifest(root, 1628350, "Steam Linux Runtime 3.0 (sniper)")
        SteamFixtures.writeManifest(root, 413150, "Stardew Valley")

        val result = assertNotNull(reader(root).scan())
        assertEquals(listOf(413150L), result.installedGames.map { it.appId })
    }

    @Test
    fun missingLibraryFoldersReturnsNull() {
        val root = temp.newFolder("Steam")
        assertNull(reader(root).scan())
        assertNull(reader(root).fingerprint())
    }

    @Test
    fun brokenLibraryFoldersReturnsNull() {
        val root = temp.newFolder("Steam")
        File(root, "steamapps").mkdirs()
        File(root, "steamapps/libraryfolders.vdf").writeText("\"libraryfolders\" {")
        assertNull(reader(root).scan())
    }

    @Test
    fun findImageFlat() {
        val root = temp.newFolder("Steam")
        val dir = File(root, "appcache/librarycache/2079120").apply { mkdirs() }
        File(dir, "header.jpg").writeText("x")
        val cover = File(dir, "library_600x900.jpg").apply { writeText("x") }
        assertEquals(cover.absolutePath, reader(root).findImage(2079120))
    }

    @Test
    fun findImageInHashedDirectory() {
        val root = temp.newFolder("Steam")
        val dir = File(root, "appcache/librarycache/613100").apply { mkdirs() }
        File(dir, "6cee40fe293f0bab47534e78bcda64487902efc1").mkdirs()
        File(dir, "6cee40fe293f0bab47534e78bcda64487902efc1/library_header.jpg").writeText("x")
        File(dir, "e28856fc6bbdf3d8a4d612e8d3cb7a4c400c4220").mkdirs()
        val cover = File(dir, "e28856fc6bbdf3d8a4d612e8d3cb7a4c400c4220/library_600x900.jpg").apply { writeText("x") }
        assertEquals(cover.absolutePath, reader(root).findImage(613100))
    }

    @Test
    fun findImageFallsBackToHeaderThenCdn() {
        val root = temp.newFolder("Steam")
        val dir = File(root, "appcache/librarycache/1").apply { mkdirs() }
        val header = File(dir, "abc/library_header.jpg").apply {
            parentFile.mkdirs()
            writeText("x")
        }
        assertEquals(header.absolutePath, reader(root).findImage(1))
        assertEquals(SteamGame.cdnImageUrl(2), reader(root).findImage(2))
    }

    @Test
    fun findImageOldFlatFormat() {
        val root = temp.newFolder("Steam")
        val cacheDir = File(root, "appcache/librarycache").apply { mkdirs() }
        val cover = File(cacheDir, "70_library_600x900.jpg").apply { writeText("x") }
        assertEquals(cover.absolutePath, reader(root).findImage(70))
    }

    @Test
    fun fingerprintChangesWhenManifestChanges() {
        val root = temp.newFolder("Steam")
        SteamFixtures.createSteamRoot(root, LibraryEntry(root, emptyList()))
        SteamFixtures.writeManifest(root, 1, "Game1")
        val reader = reader(root)
        val first = assertNotNull(reader.fingerprint())
        assertEquals(first, reader.fingerprint())

        // インストール
        val manifest2 = SteamFixtures.writeManifest(root, 2, "Game2")
        val second = assertNotNull(reader.fingerprint())
        assertNotEquals(first, second)

        // アップデート (更新日時の変化)
        manifest2.setLastModified(manifest2.lastModified() - 10_000)
        val third = assertNotNull(reader.fingerprint())
        assertNotEquals(second, third)

        // アンインストール
        manifest2.delete()
        assertNotEquals(third, reader.fingerprint())
    }

    @Test
    fun readRealSteamInstallationIfExists() {
        // 実機にSteamがある場合のみ: 読み取りが例外無く完了すること
        val platform = when {
            System.getProperty("os.name").startsWith("Windows") -> com.snowdango.amya.platform.PlatformType.WINDOWS
            System.getProperty("os.name") == "Linux" -> com.snowdango.amya.platform.PlatformType.LINUX
            else -> return
        }
        val installation = SteamLocator.locate(platform) ?: return
        val result = assertNotNull(SteamLibraryReader(installation).scan())
        result.installedGames.forEach { println("${it.appId}\t${it.name}\t${it.imageUrl}\t${it.launchPath} ${it.launchArgs}") }
    }
}
