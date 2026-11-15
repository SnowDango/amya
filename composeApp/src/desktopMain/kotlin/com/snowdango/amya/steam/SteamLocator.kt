package com.snowdango.amya.steam

import com.snowdango.amya.platform.PlatformType
import com.snowdango.amya.track.Log
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Steamのインストール先と起動方法を解決する
 */
data class SteamInstallation(
    val root: File,
    val launcher: SteamLauncher,
)

/**
 * OSごとのゲーム起動方法
 */
sealed class SteamLauncher {
    abstract val path: String
    abstract fun argsFor(appId: Long): String

    /** Windows: `steam.exe -applaunch <appid>` */
    data class Windows(override val path: String) : SteamLauncher() {
        override fun argsFor(appId: Long) = "-applaunch $appId"
    }

    /** Linux: `xdg-open steam://rungameid/<appid>` (ネイティブ / Flatpak / Snap 共通) */
    data class Linux(override val path: String) : SteamLauncher() {
        override fun argsFor(appId: Long) = "steam://rungameid/$appId"
    }
}

object SteamLocator {

    private const val REG_TIMEOUT_SECONDS = 5L
    private const val DEFAULT_XDG_OPEN = "/usr/bin/xdg-open"

    fun locate(platformType: PlatformType): SteamInstallation? {
        return try {
            when (platformType) {
                PlatformType.WINDOWS -> locateWindows()
                PlatformType.LINUX -> locateLinux(
                    home = File(System.getProperty("user.home")),
                    xdgDataHome = System.getenv("XDG_DATA_HOME"),
                    pathEnv = System.getenv("PATH"),
                )

                PlatformType.MACOS -> null
            }
        } catch (th: Throwable) {
            Log.e("Failed to locate Steam: ${th.message}")
            null
        }
    }

    fun isSteamRoot(dir: File): Boolean = File(dir, "steamapps/libraryfolders.vdf").isFile

    // region Windows

    private fun locateWindows(): SteamInstallation? {
        val steamPath = queryRegistry("HKCU\\Software\\Valve\\Steam", "SteamPath")
        val installPath = queryRegistry("HKLM\\SOFTWARE\\WOW6432Node\\Valve\\Steam", "InstallPath")
        val programFilesX86 = System.getenv("ProgramFiles(x86)") ?: "C:\\Program Files (x86)"
        val candidates = listOfNotNull(steamPath, installPath, "$programFilesX86\\Steam").map { File(it) }
        val root = pickRoot(candidates) ?: return null

        val steamExe = queryRegistry("HKCU\\Software\\Valve\\Steam", "SteamExe")
            ?.let { File(it) }
            ?.takeIf { it.isFile }
            ?: File(root, "steam.exe")
        return SteamInstallation(root, SteamLauncher.Windows(steamExe.absolutePath))
    }

    private fun queryRegistry(key: String, valueName: String): String? {
        return try {
            val process = ProcessBuilder("reg", "query", key, "/v", valueName)
                .redirectErrorStream(true)
                .start()
            val output = process.inputStream.bufferedReader().readText()
            if (!process.waitFor(REG_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                process.destroy()
                return null
            }
            parseRegQueryOutput(output, valueName)
        } catch (th: Throwable) {
            Log.e("reg query failed: ${th.message}")
            null
        }
    }

    /** `reg query` の出力から値を取り出す (`    SteamPath    REG_SZ    c:/program files (x86)/steam`) */
    internal fun parseRegQueryOutput(output: String, valueName: String): String? {
        val regex = Regex("""^\s*${Regex.escape(valueName)}\s+REG_(?:EXPAND_)?SZ\s+(.+?)\s*$""", RegexOption.IGNORE_CASE)
        return output.lineSequence()
            .firstNotNullOfOrNull { regex.find(it)?.groupValues?.get(1) }
            ?.takeIf { it.isNotBlank() }
    }

    // endregion

    // region Linux

    internal fun locateLinux(home: File, xdgDataHome: String?, pathEnv: String?): SteamInstallation? {
        val root = pickRoot(linuxCandidates(home, xdgDataHome)) ?: return null
        val xdgOpen = findOnPath("xdg-open", pathEnv) ?: DEFAULT_XDG_OPEN
        return SteamInstallation(root, SteamLauncher.Linux(xdgOpen))
    }

    internal fun linuxCandidates(home: File, xdgDataHome: String?): List<File> {
        return listOfNotNull(
            xdgDataHome?.takeIf { it.isNotBlank() }?.let { File(it, "Steam") },
            File(home, ".local/share/Steam"),
            File(home, ".steam/steam"),
            File(home, ".steam/root"),
            // Flatpak
            File(home, ".var/app/com.valvesoftware.Steam/.local/share/Steam"),
            File(home, ".var/app/com.valvesoftware.Steam/data/Steam"),
            // Snap
            File(home, "snap/steam/common/.local/share/Steam"),
        )
    }

    private fun findOnPath(command: String, pathEnv: String?): String? {
        return pathEnv?.split(File.pathSeparatorChar)
            ?.filter { it.isNotBlank() }
            ?.map { File(it, command) }
            ?.firstOrNull { it.isFile && it.canExecute() }
            ?.absolutePath
    }

    // endregion

    /** シンボリックリンクを解決したうえで、最初に見つかったSteamのルートを返す */
    internal fun pickRoot(candidates: List<File>): File? {
        return candidates.asSequence()
            .filter { isSteamRoot(it) }
            .map { it.realFile() }
            .firstOrNull()
    }

    /** File.canonicalFile はWindowsでシンボリックリンクを解決しないため toRealPath を使う */
    private fun File.realFile(): File = try {
        toPath().toRealPath().toFile()
    } catch (_: Throwable) {
        absoluteFile
    }
}
