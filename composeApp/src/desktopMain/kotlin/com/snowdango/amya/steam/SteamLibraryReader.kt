package com.snowdango.amya.steam

import com.snowdango.amya.track.Log
import java.io.File

/**
 * Steamのルートディレクトリからライブラリ・インストール済みゲーム・カバー画像を読み取る
 */
class SteamLibraryReader(
    private val installation: SteamInstallation,
) {
    private val root: File get() = installation.root

    data class Library(
        val path: File,
        /** libraryfolders.vdf に記載されているappId (ライブラリにアクセスできない場合の削除保護に使う) */
        val appIds: Set<Long>,
    ) {
        val steamApps: File get() = File(path, "steamapps")
        val isAvailable: Boolean get() = steamApps.isDirectory
    }

    /**
     * ライブラリ一覧。libraryfolders.vdf が読めなければnull
     */
    fun libraries(): List<Library>? {
        val vdfFile = File(root, "steamapps/libraryfolders.vdf")
        val vdf = try {
            VdfParser.parse(vdfFile.readText())
        } catch (th: Throwable) {
            Log.e("Failed to read libraryfolders.vdf: ${th.message}")
            return null
        }
        val folders = vdf.getObject("libraryfolders") ?: vdf.getObject("LibraryFolders") ?: return null

        val libraries = folders.entries
            .filter { (key, _) -> key.toLongOrNull() != null }
            .mapNotNull { (_, value) ->
                when (value) {
                    // 2021年以前の形式: "1" "D:\\SteamLibrary"
                    is String -> Library(File(value), emptySet())
                    is VdfObject -> {
                        val path = value.getString("path") ?: return@mapNotNull null
                        val appIds = value.getObject("apps")?.entries
                            ?.mapNotNull { it.first.toLongOrNull() }
                            ?.toSet()
                            ?: emptySet()
                        Library(File(path), appIds)
                    }

                    else -> null
                }
            }
            .toMutableList()

        // 古い形式ではSteam本体のライブラリが記載されないため補完する
        if (libraries.none { it.path.sameFileAs(root) }) {
            libraries.add(0, Library(root, emptySet()))
        }
        return libraries.distinctBy { it.path.normalizedKey() }
    }

    /**
     * 変更検知用の指紋。libraryfolders.vdf と各 appmanifest のファイル名・更新日時・サイズから作る
     */
    fun fingerprint(): String? {
        val vdfFile = File(root, "steamapps/libraryfolders.vdf")
        if (!vdfFile.isFile) return null
        val libraries = libraries() ?: return null
        return buildString {
            append(vdfFile.fileStamp())
            libraries.forEach { library ->
                append('|').append(library.path.absolutePath).append(':').append(library.isAvailable)
                manifestFiles(library).forEach { append(';').append(it.fileStamp()) }
            }
        }
    }

    /**
     * ライブラリを読み取る。libraryfolders.vdf が読めなければnull (同期をスキップする)
     */
    fun scan(): SteamScanResult? {
        val libraries = libraries() ?: return null
        val installed = mutableMapOf<Long, SteamGame>()
        val protected = mutableSetOf<Long>()

        libraries.forEach { library ->
            if (!library.isAvailable) {
                // 外付けドライブが外れている等。記載されているゲームは削除しない
                protected.addAll(library.appIds)
                return@forEach
            }
            manifestFiles(library).forEach { file ->
                val manifest = readManifest(file) ?: run {
                    // 読めないmanifestもゲーム自体は存在するとみなす
                    file.appIdFromManifestName()?.let { protected.add(it) }
                    return@forEach
                }
                protected.add(manifest.appId)
                if (manifest.isFullyInstalled && !SteamExclusion.isExcluded(manifest.appId, manifest.name)) {
                    installed.putIfAbsent(
                        manifest.appId,
                        SteamGame(
                            appId = manifest.appId,
                            name = manifest.name,
                            imageUrl = findImage(manifest.appId),
                            launchPath = installation.launcher.path,
                            launchArgs = installation.launcher.argsFor(manifest.appId),
                        )
                    )
                }
            }
        }
        return SteamScanResult(installed.values.toList(), protected)
    }

    /**
     * カバー画像を探す。librarycacheのファイル配置はゲーム・Steamのバージョンによって異なる
     * - `librarycache/<appid>/library_600x900.jpg`
     * - `librarycache/<appid>/<hash>/library_600x900.jpg`
     * - `librarycache/<appid>_library_600x900.jpg` (旧形式)
     */
    fun findImage(appId: Long): String {
        val cacheDir = File(root, "appcache/librarycache")
        val appDir = File(cacheDir, appId.toString())
        val files = if (appDir.isDirectory) {
            appDir.walkTopDown().maxDepth(2).filter { it.isFile }.toList()
        } else {
            emptyList()
        }
        IMAGE_CANDIDATES.forEach { name ->
            files.firstOrNull { it.name.equals(name, ignoreCase = true) }?.let { return it.absolutePath }
            File(cacheDir, "${appId}_$name").takeIf { it.isFile }?.let { return it.absolutePath }
        }
        return SteamGame.cdnImageUrl(appId)
    }

    private fun manifestFiles(library: Library): List<File> {
        return library.steamApps
            .listFiles { file -> file.isFile && MANIFEST_REGEX.matches(file.name) }
            ?.sortedBy { it.name }
            ?: emptyList()
    }

    private fun readManifest(file: File): Manifest? {
        return try {
            val appState = VdfParser.parse(file.readText()).getObject("AppState") ?: return null
            val appId = appState.getString("appid")?.toLongOrNull() ?: return null
            val name = appState.getString("name")?.takeIf { it.isNotBlank() } ?: return null
            val stateFlags = appState.getString("StateFlags")?.toLongOrNull() ?: 0L
            Manifest(appId, name, stateFlags)
        } catch (th: Throwable) {
            Log.e("Failed to read ${file.name}: ${th.message}")
            null
        }
    }

    private data class Manifest(val appId: Long, val name: String, val stateFlags: Long) {
        val isFullyInstalled: Boolean get() = stateFlags and STATE_FULLY_INSTALLED != 0L
    }

    companion object {
        private const val STATE_FULLY_INSTALLED = 4L
        private val MANIFEST_REGEX = Regex("""appmanifest_(\d+)\.acf""", RegexOption.IGNORE_CASE)
        private val IMAGE_CANDIDATES = listOf("library_600x900.jpg", "library_header.jpg", "header.jpg")

        private fun File.appIdFromManifestName(): Long? =
            MANIFEST_REGEX.matchEntire(name)?.groupValues?.get(1)?.toLongOrNull()

        private fun File.fileStamp() = "$name:${lastModified()}:${length()}"

        private fun File.normalizedKey(): String = try {
            canonicalPath.lowercase()
        } catch (_: Throwable) {
            absolutePath.lowercase()
        }

        private fun File.sameFileAs(other: File) = normalizedKey() == other.normalizedKey()
    }
}

/**
 * ゲームではないSteamのツール類 (再頒布パッケージ / Proton / Steam Linux Runtime)
 */
object SteamExclusion {
    private val EXCLUDED_APP_IDS = setOf(
        228980L, // Steamworks Common Redistributables
        1070560L, // Steam Linux Runtime 1.0 (scout)
        1391110L, // Steam Linux Runtime 2.0 (soldier)
        1628350L, // Steam Linux Runtime 3.0 (sniper)
        1493710L, // Proton Experimental
        2180100L, // Proton Hotfix
        1826330L, // Proton EasyAntiCheat Runtime
        1161040L, // Proton BattlEye Runtime
    )
    private val EXCLUDED_NAME_PREFIXES = listOf(
        "Proton ",
        "Steam Linux Runtime",
        "Steamworks Common Redistributables",
    )

    fun isExcluded(appId: Long, name: String): Boolean {
        return appId in EXCLUDED_APP_IDS || EXCLUDED_NAME_PREFIXES.any { name.startsWith(it, ignoreCase = true) }
    }
}
