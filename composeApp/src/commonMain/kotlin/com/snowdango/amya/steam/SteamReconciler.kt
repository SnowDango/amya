package com.snowdango.amya.steam

import java.io.File

/**
 * Steam同期で登録済みのアプリ
 */
data class ManagedSteamApp(
    val id: Long,
    val steamAppId: Long,
    val imageUrl: String,
)

/**
 * 同期で行う操作
 *
 * @property toAdd 新しく登録するゲーム
 * @property toDeleteIds 削除するアプリのid (appsテーブルのid)
 * @property imageUpdates 画像を差し替えるアプリ (id -> 新しい画像)
 * @property missingCounts 見つからなかった回数 (次回の同期に引き継ぐ)
 */
data class SteamSyncPlan(
    val toAdd: List<SteamGame>,
    val toDeleteIds: List<Long>,
    val imageUpdates: Map<Long, String>,
    val missingCounts: Map<Long, Int>,
) {
    val hasChanges: Boolean get() = toAdd.isNotEmpty() || toDeleteIds.isNotEmpty() || imageUpdates.isNotEmpty()
}

/**
 * Steamライブラリと登録済みアプリを突き合わせて、同期で行う操作を決める
 */
object SteamReconciler {

    /** この回数続けて見つからなかったら削除する (一時的な状態での誤削除を防ぐ) */
    const val MISSING_THRESHOLD = 2

    fun plan(
        managed: List<ManagedSteamApp>,
        scan: SteamScanResult,
        excludedAppIds: Set<Long>,
        previousMissingCounts: Map<Long, Int>,
        isImageBroken: (String) -> Boolean = ::isLocalImageMissing,
    ): SteamSyncPlan {
        val installed = scan.installedGames.associateBy { it.appId }
        val managedAppIds = managed.map { it.steamAppId }.toSet()

        val toAdd = scan.installedGames.filter { it.appId !in managedAppIds && it.appId !in excludedAppIds }

        val toDelete = mutableListOf<Long>()
        val imageUpdates = mutableMapOf<Long, String>()
        val missingCounts = mutableMapOf<Long, Int>()

        managed.forEach { app ->
            val game = installed[app.steamAppId]
            when {
                game != null -> {
                    if (shouldUpdateImage(app, game, isImageBroken)) {
                        imageUpdates[app.id] = game.imageUrl
                    }
                }

                app.steamAppId in scan.protectedAppIds -> Unit // 更新中・ドライブが外れている等
                else -> {
                    val count = (previousMissingCounts[app.steamAppId] ?: 0) + 1
                    if (count >= MISSING_THRESHOLD) {
                        toDelete.add(app.id)
                    } else {
                        missingCounts[app.steamAppId] = count
                    }
                }
            }
        }
        return SteamSyncPlan(toAdd, toDelete, imageUpdates, missingCounts)
    }

    /**
     * 画像を差し替えるか。ユーザーが設定した画像は壊れていない限り変更しない。
     * - ローカルの画像ファイルが消えた (Steamのアートワーク更新でハッシュ名のディレクトリが変わった等)
     * - 自動で設定したCDNの画像を使っていて、ローカルに画像ができた
     */
    private fun shouldUpdateImage(app: ManagedSteamApp, game: SteamGame, isImageBroken: (String) -> Boolean): Boolean {
        if (app.imageUrl == game.imageUrl) return false
        if (isImageBroken(app.imageUrl)) return true
        return app.imageUrl == SteamGame.cdnImageUrl(app.steamAppId) && !isRemoteUrl(game.imageUrl)
    }

    fun isLocalImageMissing(imageUrl: String): Boolean {
        if (imageUrl.isBlank()) return true
        if (isRemoteUrl(imageUrl)) return false
        return !File(imageUrl).isFile
    }

    private fun isRemoteUrl(url: String) = url.startsWith("http://") || url.startsWith("https://")
}
