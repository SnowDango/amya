package com.snowdango.amya.steam

import java.io.File
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

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
 * @property missingSince 見つからなくなった時刻 (steamAppId -> エポックミリ秒。次回の同期に引き継ぐ)
 */
data class SteamSyncPlan(
    val toAdd: List<SteamGame>,
    val toDeleteIds: List<Long>,
    val imageUpdates: Map<Long, String>,
    val missingSince: Map<Long, Long>,
) {
    val hasChanges: Boolean get() = toAdd.isNotEmpty() || toDeleteIds.isNotEmpty() || imageUpdates.isNotEmpty()
}

/**
 * Steamライブラリと登録済みアプリを突き合わせて、同期で行う操作を決める
 */
object SteamReconciler {

    /**
     * 見つからない状態がこの時間続いたら削除する (一時的な状態での誤削除を防ぐ)。
     * 回数で判定すると、起動直後など同期が続けて実行された時に一瞬で削除されてしまうため時間で判定する
     */
    val MISSING_GRACE_PERIOD: Duration = 1.minutes

    fun plan(
        managed: List<ManagedSteamApp>,
        scan: SteamScanResult,
        excludedAppIds: Set<Long>,
        previousMissingSince: Map<Long, Long>,
        now: Long,
        isImageBroken: (String) -> Boolean = ::isLocalImageMissing,
    ): SteamSyncPlan {
        val installed = scan.installedGames.associateBy { it.appId }
        val managedAppIds = managed.map { it.steamAppId }.toSet()

        val toAdd = scan.installedGames.filter { it.appId !in managedAppIds && it.appId !in excludedAppIds }

        val toDelete = mutableListOf<Long>()
        val imageUpdates = mutableMapOf<Long, String>()
        val missingSince = mutableMapOf<Long, Long>()

        managed.forEach { app ->
            val game = installed[app.steamAppId]
            when {
                game != null -> {
                    if (shouldUpdateImage(app, game, isImageBroken)) {
                        imageUpdates[app.id] = game.imageUrl
                    }
                }

                app.steamAppId in scan.protectedAppIds -> Unit // 更新中・ドライブが外れている等
                !scan.isComplete -> {
                    // 一部のライブラリが読めず、本当に消えたのか判断できない。状態を据え置く
                    previousMissingSince[app.steamAppId]?.let { missingSince[app.steamAppId] = it }
                }

                else -> {
                    val since = previousMissingSince[app.steamAppId]
                    if (since != null && now - since >= MISSING_GRACE_PERIOD.inWholeMilliseconds) {
                        toDelete.add(app.id)
                    } else {
                        missingSince[app.steamAppId] = since ?: now
                    }
                }
            }
        }
        return SteamSyncPlan(toAdd, toDelete, imageUpdates, missingSince)
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
