package com.snowdango.amya.steam

import com.snowdango.amya.domain.db.entity.AppsEntity
import com.snowdango.amya.domain.db.entity.TagEntity
import com.snowdango.amya.repository.apps.AppsRepository
import com.snowdango.amya.repository.settings.SettingsRepository
import com.snowdango.amya.repository.steam.SteamExcludedRepository
import com.snowdango.amya.repository.tag.TagRepository
import com.snowdango.amya.track.Log
import compose.icons.TablerIcons
import compose.icons.tablericons.BrandSteam
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/**
 * Steamライブラリを定期的に確認し、インストール済みゲームをamyaのアプリとして同期する
 *
 * - 設定 (steam_sync_enabled) がONの間だけ動作する
 * - 起動時・一定間隔・ウィンドウ表示時 ([requestSync]) に確認する
 * - Steam側のファイルの指紋が変わった時だけライブラリを読み直す
 */
class SteamSyncService(
    private val source: SteamLibrarySource,
    private val appsRepository: AppsRepository,
    private val tagRepository: TagRepository,
    private val excludedRepository: SteamExcludedRepository,
    private val settingsRepository: SettingsRepository,
    private val scope: CoroutineScope,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    /** 現在時刻 (エポックミリ秒)。テストで差し替える */
    private val clock: () -> Long = System::currentTimeMillis,
) {

    sealed class SyncResult {
        data object Disabled : SyncResult()
        data object SteamNotFound : SyncResult()
        data object Unchanged : SyncResult()

        /** Steamタグがユーザーに削除されたため同期をOFFにした */
        data object TagDeleted : SyncResult()
        data class Synced(val added: Int, val deleted: Int, val imageUpdated: Int) : SyncResult()
    }

    private val mutex = Mutex()
    private var lastFingerprint: String? = null
    private var missingSince: Map<Long, Long> = emptyMap()
    private var loopJob: Job? = null

    private val _isSteamAvailable = MutableStateFlow(false)

    /** Steamがインストールされているか (設定画面の表示に使う) */
    val isSteamAvailable: StateFlow<Boolean> = _isSteamAvailable.asStateFlow()

    /**
     * 定期チェックを開始する。設定のON/OFFに追従する
     */
    fun start(interval: Duration = DEFAULT_INTERVAL) {
        if (loopJob?.isActive == true) return
        loopJob = scope.launch(ioDispatcher) {
            // 設定の読み込み等で失敗しても定期チェックが止まらないよう、間隔を空けてやり直す
            while (isActive) {
                try {
                    refreshAvailability()
                    settingsRepository.getIsSteamSyncEnabled()
                        .distinctUntilChanged()
                        .collectLatest { enabled ->
                            resetState()
                            if (!enabled) return@collectLatest
                            while (isActive) {
                                syncSafely()
                                delay(interval)
                            }
                        }
                } catch (ce: CancellationException) {
                    throw ce
                } catch (th: Throwable) {
                    Log.e("Steam sync loop failed: ${th.message}")
                }
                delay(interval)
            }
        }
    }

    /**
     * 今すぐ確認する (ウィンドウ表示時など)
     */
    fun requestSync(force: Boolean = false) {
        scope.launch(ioDispatcher) { syncSafely(force) }
    }

    suspend fun refreshAvailability(): Boolean = withContext(ioDispatcher) {
        source.isAvailable().also { _isSteamAvailable.value = it }
    }

    private suspend fun syncSafely(force: Boolean = false) {
        try {
            val result = sync(force)
            if (result is SyncResult.Synced) Log.i("Steam sync: $result")
        } catch (ce: CancellationException) {
            throw ce
        } catch (th: Throwable) {
            Log.e("Steam sync failed: ${th.message}")
        }
    }

    suspend fun sync(force: Boolean = false): SyncResult = withContext(ioDispatcher) {
        mutex.withLock {
            if (!settingsRepository.getIsSteamSyncEnabled().first()) return@withLock SyncResult.Disabled
            val fingerprint = source.fingerprint()
            _isSteamAvailable.value = fingerprint != null || source.isAvailable()
            if (fingerprint == null) return@withLock SyncResult.SteamNotFound

            val managed = appsRepository.getSteamApps().mapNotNull { app ->
                app.steamAppId?.let { ManagedSteamApp(app.id, it, app.imageUrl) }
            }
            val needsScan = force ||
                fingerprint != lastFingerprint ||
                missingSince.isNotEmpty() ||
                managed.any { SteamReconciler.isLocalImageMissing(it.imageUrl) }
            if (!needsScan) return@withLock SyncResult.Unchanged

            val tagId = ensureSteamTag() ?: return@withLock SyncResult.TagDeleted
            val scan = source.scan() ?: return@withLock SyncResult.SteamNotFound

            val plan = SteamReconciler.plan(
                managed = managed,
                scan = scan,
                excludedAppIds = excludedRepository.getAllAppIds(),
                previousMissingSince = missingSince,
                now = clock(),
            )
            apply(plan, tagId)

            lastFingerprint = fingerprint
            missingSince = plan.missingSince
            if (plan.hasChanges) {
                SyncResult.Synced(plan.toAdd.size, plan.toDeleteIds.size, plan.imageUpdates.size)
            } else {
                SyncResult.Unchanged
            }
        }
    }

    private suspend fun apply(plan: SteamSyncPlan, tagId: Long) {
        plan.toAdd.forEach { game ->
            try {
                appsRepository.insert(
                    AppsEntity(
                        name = game.name,
                        imageUrl = game.imageUrl,
                        tagId = tagId,
                        subTagId = null,
                        path = game.launchPath,
                        args = game.launchArgs,
                        root = false,
                        steamAppId = game.appId,
                    )
                )
            } catch (ce: CancellationException) {
                throw ce
            } catch (th: Throwable) {
                Log.e("Failed to add steam app ${game.appId}: ${th.message}")
            }
        }
        if (plan.toDeleteIds.isNotEmpty()) {
            appsRepository.deleteByIds(plan.toDeleteIds)
        }
        plan.imageUpdates.forEach { (id, imageUrl) ->
            appsRepository.updateImageUrl(id, imageUrl)
        }
    }

    /**
     * 同期先のSteamタグを返す。無ければ作る。
     * 一度作ったタグが削除されていた場合はユーザーが消したとみなし、同期をOFFにしてnullを返す
     */
    private suspend fun ensureSteamTag(): Long? {
        val tagId = settingsRepository.getSteamTagId().first()
        if (tagId != null) {
            if (tagRepository.getById(tagId) != null) return tagId
            onSteamTagDeleted()
            return null
        }
        val newId = tagRepository.insert(TagEntity(name = STEAM_TAG_NAME, icon = TablerIcons.BrandSteam.name))
        settingsRepository.setSteamTagId(newId)
        return newId
    }

    /**
     * Steamタグが削除された時の処理。同期をOFFにする
     */
    suspend fun onSteamTagDeleted() {
        Log.i("Steam tag was deleted. Steam sync is disabled.")
        settingsRepository.setSteamTagId(null)
        settingsRepository.setIsSteamSyncEnabled(false)
    }

    private suspend fun resetState() {
        mutex.withLock {
            lastFingerprint = null
            missingSince = emptyMap()
        }
    }

    companion object {
        val DEFAULT_INTERVAL = 5.minutes
        const val STEAM_TAG_NAME = "Steam"
    }
}
