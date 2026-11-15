package com.snowdango.amya.platform

import com.snowdango.amya.track.Log
import kotlinx.coroutines.CancellationException

/**
 * 登録されたアプリを起動する
 */
object AppLauncher {

    /**
     * アプリを起動する。失敗した場合はユーザーに表示するエラーメッセージを返す
     */
    fun launch(isRoot: Boolean, path: String, args: String?): String? {
        // エクスプローラーの「パスのコピー」で付く " を取り除く
        val normalizedPath = path.trim().removeSurrounding("\"")
        if (normalizedPath.isBlank()) return "File path is empty"
        return try {
            SubProcessBuilder.execBuilder(
                isRoot = isRoot,
                path = normalizedPath,
                args = args,
            ).spawn()
            null
        } catch (ce: CancellationException) {
            throw ce
        } catch (th: Throwable) {
            Log.e("Failed to launch $normalizedPath: ${th.message}")
            "Failed to launch app\n$normalizedPath"
        }
    }
}
