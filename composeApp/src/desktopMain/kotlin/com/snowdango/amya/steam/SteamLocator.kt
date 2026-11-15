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
        val userValues = readRegistryValues("HKCU\\Software\\Valve\\Steam")
        val steamPath = userValues["steampath"]
        val installPath = readRegistryValues("HKLM\\SOFTWARE\\WOW6432Node\\Valve\\Steam")["installpath"]
        val programFilesX86 = System.getenv("ProgramFiles(x86)") ?: "C:\\Program Files (x86)"
        val candidates = listOfNotNull(steamPath, installPath, "$programFilesX86\\Steam").map { File(it) }
        val root = pickRoot(candidates) ?: return null

        val steamExe = userValues["steamexe"]
            ?.let { File(it) }
            ?.takeIf { it.isFile }
            ?: File(root, "steam.exe")
        return SteamInstallation(root, SteamLauncher.Windows(steamExe.absolutePath))
    }

    /**
     * レジストリキーの文字列値を読む (値の名前は小文字にする)。
     * `reg query` の出力はコンソールのコードページ (日本語環境ではShift_JIS) になり非ASCIIのパスが化けるため、
     * 常にUTF-16で書き出される `reg export` を使う
     */
    private fun readRegistryValues(key: String): Map<String, String> {
        val file = try {
            File.createTempFile("amya-reg", ".reg")
        } catch (th: Throwable) {
            Log.e("Failed to create temp file: ${th.message}")
            return emptyMap()
        }
        return try {
            val process = ProcessBuilder("reg", "export", key, file.absolutePath, "/y")
                .redirectErrorStream(true)
                .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                .start()
            if (!process.waitFor(REG_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                process.destroyForcibly()
                Log.e("reg export timed out: $key")
                return emptyMap()
            }
            if (process.exitValue() != 0) return emptyMap() // キーが無い
            parseRegExport(file.readText(Charsets.UTF_16), key)
        } catch (th: Throwable) {
            Log.e("reg export failed: ${th.message}")
            emptyMap()
        } finally {
            file.delete()
        }
    }

    /**
     * `reg export` の出力から、指定したキー直下の文字列値を取り出す (サブキーの値は含めない)
     * ```
     * [HKEY_CURRENT_USER\Software\Valve\Steam]
     * "SteamPath"="c:/program files (x86)/steam"
     * ```
     */
    internal fun parseRegExport(content: String, key: String): Map<String, String> {
        val header = "[${expandRootKey(key)}]"
        val values = mutableMapOf<String, String>()
        var inTargetKey = false
        content.lineSequence().map { it.trim() }.forEach { line ->
            when {
                line.startsWith("[") -> inTargetKey = line.equals(header, ignoreCase = true)
                inTargetKey -> REG_STRING_VALUE.matchEntire(line)?.let { match ->
                    val name = unescapeRegString(match.groupValues[1]).lowercase()
                    values[name] = unescapeRegString(match.groupValues[2])
                }
            }
        }
        return values
    }

    private val REG_STRING_VALUE = Regex("""^"((?:[^"\\]|\\.)*)"="((?:[^"\\]|\\.)*)"$""")

    private fun unescapeRegString(value: String): String = value.replace(Regex("""\\(.)"""), "$1")

    private val ROOT_KEY_ALIASES = mapOf(
        "HKCU\\" to "HKEY_CURRENT_USER\\",
        "HKLM\\" to "HKEY_LOCAL_MACHINE\\",
    )

    /** `reg export` の出力では `HKCU` 等の省略形ではなく正式名になる */
    private fun expandRootKey(key: String): String {
        val (alias, fullName) = ROOT_KEY_ALIASES.entries
            .firstOrNull { key.startsWith(it.key, ignoreCase = true) }
            ?: return key
        return fullName + key.substring(alias.length)
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
