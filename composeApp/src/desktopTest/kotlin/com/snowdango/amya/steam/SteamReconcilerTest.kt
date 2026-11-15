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

    private companion object {
        const val NOW = 1_700_000_000_000L
    }

    @Test
    fun addsNewGames() {
        val plan = SteamReconciler.plan(
            managed = listOf(managed(1, 100)),
            scan = scan(game(100), game(200), game(300)),
            excludedAppIds = emptySet(),
            previousMissingSince = emptyMap(),
            now = NOW,
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
            previousMissingSince = emptyMap(),
            now = NOW,
            isImageBroken = noBrokenImage,
        )
        assertEquals(listOf(100L), plan.toAdd.map { it.appId })
    }

    private fun planMissing(previousMissingSince: Map<Long, Long>, now: Long, scan: SteamScanResult = scan(game(100))) =
        SteamReconciler.plan(
            managed = listOf(managed(1, 100), managed(2, 200)),
            scan = scan,
            excludedAppIds = emptySet(),
            previousMissingSince = previousMissingSince,
            now = now,
            isImageBroken = noBrokenImage,
        )

    @Test
    fun deletesOnlyAfterMissingForGracePeriod() {
        val first = planMissing(previousMissingSince = emptyMap(), now = NOW)
        assertTrue(first.toDeleteIds.isEmpty())
        assertEquals(mapOf(200L to NOW), first.missingSince)
        assertFalse(first.hasChanges)

        val afterGrace = NOW + SteamReconciler.MISSING_GRACE_PERIOD.inWholeMilliseconds
        val second = planMissing(previousMissingSince = first.missingSince, now = afterGrace)
        assertEquals(listOf(2L), second.toDeleteIds)
        assertTrue(second.missingSince.isEmpty())
    }

    @Test
    fun backToBackSyncsDoNotDelete() {
        // 起動直後など、同期が続けて実行されても猶予期間内は削除しない
        val first = planMissing(previousMissingSince = emptyMap(), now = NOW)
        val second = planMissing(previousMissingSince = first.missingSince, now = NOW + 10)
        assertTrue(second.toDeleteIds.isEmpty())
        // 見つからなくなった時刻は最初のまま引き継ぐ
        assertEquals(mapOf(200L to NOW), second.missingSince)
    }

    @Test
    fun incompleteScanDoesNotDelete() {
        // 一部のライブラリが読めない場合は、猶予期間を過ぎていても削除しない
        val plan = planMissing(
            previousMissingSince = mapOf(200L to NOW),
            now = NOW + SteamReconciler.MISSING_GRACE_PERIOD.inWholeMilliseconds * 10,
            scan = scan(game(100)).copy(isComplete = false),
        )
        assertTrue(plan.toDeleteIds.isEmpty())
        assertEquals(mapOf(200L to NOW), plan.missingSince)
    }

    @Test
    fun missingStateResetsWhenGameComesBack() {
        val plan = SteamReconciler.plan(
            managed = listOf(managed(2, 200)),
            scan = scan(game(200)),
            excludedAppIds = emptySet(),
            previousMissingSince = mapOf(200L to NOW),
            now = NOW + SteamReconciler.MISSING_GRACE_PERIOD.inWholeMilliseconds,
            isImageBroken = noBrokenImage,
        )
        assertTrue(plan.toDeleteIds.isEmpty())
        assertTrue(plan.missingSince.isEmpty())
    }

    @Test
    fun protectedGamesAreNeverDeleted() {
        // 更新中 (manifestはあるが完全インストールでない) / ドライブが外れている
        val plan = SteamReconciler.plan(
            managed = listOf(managed(1, 100)),
            scan = scan(protected = setOf(100)),
            excludedAppIds = emptySet(),
            previousMissingSince = mapOf(100L to NOW),
            now = NOW + SteamReconciler.MISSING_GRACE_PERIOD.inWholeMilliseconds * 10,
            isImageBroken = noBrokenImage,
        )
        assertTrue(plan.toDeleteIds.isEmpty())
        assertTrue(plan.missingSince.isEmpty())
    }

    @Test
    fun brokenLocalImageIsReplaced() {
        val plan = SteamReconciler.plan(
            managed = listOf(managed(1, 100, imageUrl = "/steam/librarycache/100/oldhash/library_600x900.jpg")),
            scan = scan(game(100, imageUrl = "/steam/librarycache/100/newhash/library_600x900.jpg")),
            excludedAppIds = emptySet(),
            previousMissingSince = emptyMap(),
            now = NOW,
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
            previousMissingSince = emptyMap(),
            now = NOW,
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
            previousMissingSince = emptyMap(),
            now = NOW,
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
