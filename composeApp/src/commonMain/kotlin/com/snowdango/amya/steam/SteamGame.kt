package com.snowdango.amya.steam

/**
 * インストール済みのSteamゲーム
 *
 * @property imageUrl カバー画像。ローカルのlibrarycacheにあればその絶対パス、無ければCDNのURL
 * @property launchPath 起動に使う実行ファイル (Windows: steam.exe / Linux: xdg-open)
 * @property launchArgs 起動時の引数 (Windows: -applaunch <appid> / Linux: steam://rungameid/<appid>)
 */
data class SteamGame(
    val appId: Long,
    val name: String,
    val imageUrl: String,
    val launchPath: String,
    val launchArgs: String,
)

/**
 * Steamライブラリの読み取り結果
 *
 * @property installedGames インストールが完了しているゲーム
 * @property protectedAppIds 削除してはいけないappId。
 * manifestが存在する(更新中・ダウンロード中を含む)もの、及びアクセスできないライブラリに登録されているもの
 */
data class SteamScanResult(
    val installedGames: List<SteamGame>,
    val protectedAppIds: Set<Long>,
)
