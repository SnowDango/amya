package com.snowdango.amya.steam

import java.io.File

/**
 * 実機 (Windows) のSteamから取得したファイルを元にしたテスト用データ
 */
object SteamFixtures {

    val windowsLibraryFolders = """
        "libraryfolders"
        {
        	"0"
        	{
        		"path"		"C:\\Program Files (x86)\\Steam"
        		"label"		""
        		"contentid"		"3775947640195184532"
        		"totalsize"		"0"
        		"update_clean_bytes_tally"		"257212132"
        		"time_last_update_verified"		"1793466837"
        		"apps"
        		{
        			"228980"		"0"
        			"2079120"		"4262440653"
        		}
        	}
        	"1"
        	{
        		"path"		"F:\\SteamLibrary"
        		"label"		""
        		"contentid"		"7020003345935539800"
        		"totalsize"		"500104687616"
        		"update_clean_bytes_tally"		"2149103574"
        		"time_last_update_verified"		"1794016356"
        		"apps"
        		{
        			"613100"		"12954694809"
        			"5335510"		"1096312261"
        		}
        	}
        	"2"
        	{
        		"path"		"E:\\SteamLibrary"
        		"label"		""
        		"contentid"		"34947469938669417"
        		"totalsize"		"2048390066176"
        		"update_clean_bytes_tally"		"749362509"
        		"time_last_update_verified"		"1791324128"
        		"apps"
        		{
        		}
        	}
        }
    """.trimIndent()

    fun manifest(appId: Long, name: String, stateFlags: Int = 4) = """
        "AppState"
        {
        	"appid"		"$appId"
        	"universe"		"1"
        	"LauncherPath"		"C:\\Program Files (x86)\\Steam\\steam.exe"
        	"name"		"$name"
        	"StateFlags"		"$stateFlags"
        	"installdir"		"$name"
        	"LastUpdated"		"1794016357"
        	"LastPlayed"		"1794031364"
        	"SizeOnDisk"		"12954694809"
        	"buildid"		"25247477"
        	"InstalledDepots"
        	{
        		"${appId + 1}"
        		{
        			"manifest"		"7603952764074539224"
        			"size"		"12218544984"
        		}
        	}
        	"UserConfig"
        	{
        		"language"		"english"
        	}
        }
    """.trimIndent()

    /** 1つのライブラリを表す libraryfolders.vdf のエントリ */
    data class LibraryEntry(val path: File, val appIds: List<Long>)

    fun libraryFolders(vararg libraries: LibraryEntry): String = buildString {
        appendLine("\"libraryfolders\"")
        appendLine("{")
        libraries.forEachIndexed { index, library ->
            appendLine("\t\"$index\"")
            appendLine("\t{")
            appendLine("\t\t\"path\"\t\t\"${library.path.absolutePath.replace("\\", "\\\\")}\"")
            appendLine("\t\t\"apps\"")
            appendLine("\t\t{")
            library.appIds.forEach { appendLine("\t\t\t\"$it\"\t\t\"0\"") }
            appendLine("\t\t}")
            appendLine("\t}")
        }
        appendLine("}")
    }

    /** テスト用のSteamルートを作る */
    fun createSteamRoot(root: File, vararg libraries: LibraryEntry): File {
        File(root, "steamapps").mkdirs()
        File(root, "steamapps/libraryfolders.vdf").writeText(libraryFolders(*libraries))
        return root
    }

    fun writeManifest(library: File, appId: Long, name: String, stateFlags: Int = 4): File {
        val steamApps = File(library, "steamapps").apply { mkdirs() }
        return File(steamApps, "appmanifest_$appId.acf").apply { writeText(manifest(appId, name, stateFlags)) }
    }
}
