package com.snowdango.amya.steam

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SteamReconcilerTest {

    private fun game(appId: Long, imageUrl: String = "/steam/librarycache/$appId/library_600x900.jpg") = SteamGame(
        appId = appId,
        name = "Game$appId",
        imageUrl = imageUrl,
        launchPath = "steam.exe",
        launchArgs = "-applaunch $appId",
    )

    private fun scan(vararg games: SteamGame, protected: Set<Long> = emptySet()) =
        SteamScanResult(games.toList(), games.map { it.appId }.toSet() + protected)

    private fun managed(id: Long, appId: Long, imageUrl: String = "/steam/librarycache/$appId/library_600x900.jpg") =
        ManagedSteamApp(id, appId, imageUrl)

    private val noBrokenImage: (String) -> Boolean = { false }

    @Test
    fun addsNewGames() {
        val plan = SteamReconciler.plan(
            managed = listOf(managed(1, 100)),
            scan = scan(game(100), game(200), game(300)),
            excludedAppIds = emptySet(),
            previousMissingCounts = emptyMap(),
            isImageBroken = noBrokenImage,
        )
        assertEquals(listOf(200L, 300L), plan.toAdd.map { it.appId })
        assertTrue(plan.toDeleteIds.isEmpty())
        assertTrue(plan.hasChanges)
    }

    @Test
    fun excludedGamesAreNotAdded() {
        val plan = SteamReconciler.plan(
            managed = emptyList(),
            scan = scan(game(100), game(200)),
            excludedAppIds = setOf(200),
            previousMissingCounts = emptyMap(),
            isImageBroken = noBrokenImage,
        )
        assertEquals(listOf(100L), plan.toAdd.map { it.appId })
    }

    @Test
    fun deletesOnlyAfterMissingTwice() {
        val first = SteamReconciler.plan(
            managed = listOf(managed(1, 100), managed(2, 200)),
            scan = scan(game(100)),
            excludedAppIds = emptySet(),
            previousMissingCounts = emptyMap(),
            isImageBroken = noBrokenImage,
        )
        assertTrue(first.toDeleteIds.isEmpty())
        assertEquals(mapOf(200L to 1), first.missingCounts)
        assertFalse(first.hasChanges)

        val second = SteamReconciler.plan(
            managed = listOf(managed(1, 100), managed(2, 200)),
            scan = scan(game(100)),
            excludedAppIds = emptySet(),
            previousMissingCounts = first.missingCounts,
            isImageBroken = noBrokenImage,
        )
        assertEquals(listOf(2L), second.toDeleteIds)
        assertTrue(second.missingCounts.isEmpty())
    }

    @Test
    fun missingCountResetsWhenGameComesBack() {
        val plan = SteamReconciler.plan(
            managed = listOf(managed(2, 200)),
            scan = scan(game(200)),
            excludedAppIds = emptySet(),
            previousMissingCounts = mapOf(200L to 1),
            isImageBroken = noBrokenImage,
        )
        assertTrue(plan.toDeleteIds.isEmpty())
        assertTrue(plan.missingCounts.isEmpty())
    }

    @Test
    fun protectedGamesAreNeverDeleted() {
        // 更新中 (manifestはあるが完全インストールでない) / ドライブが外れている
        val plan = SteamReconciler.plan(
            managed = listOf(managed(1, 100)),
            scan = scan(protected = setOf(100)),
            excludedAppIds = emptySet(),
            previousMissingCounts = mapOf(100L to 5),
            isImageBroken = noBrokenImage,
        )
        assertTrue(plan.toDeleteIds.isEmpty())
        assertTrue(plan.missingCounts.isEmpty())
    }

    @Test
    fun brokenLocalImageIsReplaced() {
        val plan = SteamReconciler.plan(
            managed = listOf(managed(1, 100, imageUrl = "/steam/librarycache/100/oldhash/library_600x900.jpg")),
            scan = scan(game(100, imageUrl = "/steam/librarycache/100/newhash/library_600x900.jpg")),
            excludedAppIds = emptySet(),
            previousMissingCounts = emptyMap(),
            isImageBroken = { it.contains("oldhash") },
        )
        assertEquals(mapOf(1L to "/steam/librarycache/100/newhash/library_600x900.jpg"), plan.imageUpdates)
    }

    @Test
    fun userImageIsKeptWhenNotBroken() {
        val plan = SteamReconciler.plan(
            managed = listOf(managed(1, 100, imageUrl = "https://example.com/my-cover.png")),
            scan = scan(game(100)),
            excludedAppIds = emptySet(),
            previousMissingCounts = emptyMap(),
            isImageBroken = noBrokenImage,
        )
        assertTrue(plan.imageUpdates.isEmpty())
    }

    @Test
    fun cdnFallbackIsUpgradedToLocalImage() {
        val plan = SteamReconciler.plan(
            managed = listOf(managed(1, 100, imageUrl = SteamGame.cdnImageUrl(100))),
            scan = scan(game(100)),
            excludedAppIds = emptySet(),
            previousMissingCounts = emptyMap(),
            isImageBroken = noBrokenImage,
        )
        assertEquals(mapOf(1L to "/steam/librarycache/100/library_600x900.jpg"), plan.imageUpdates)
    }

    @Test
    fun isLocalImageMissing() {
        assertFalse(SteamReconciler.isLocalImageMissing("https://example.com/a.png"))
        assertTrue(SteamReconciler.isLocalImageMissing(""))
        assertTrue(SteamReconciler.isLocalImageMissing("/definitely/not/exists.jpg"))
        val file = kotlin.io.path.createTempFile().toFile().apply { deleteOnExit() }
        assertFalse(SteamReconciler.isLocalImageMissing(file.absolutePath))
    }
}
