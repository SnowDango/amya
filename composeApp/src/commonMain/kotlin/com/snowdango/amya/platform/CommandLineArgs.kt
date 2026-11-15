package com.snowdango.amya.platform

/**
 * ユーザーが入力した引数文字列を分割する
 *
 * - 空白 (連続していても1つの区切りとして扱う) で区切る
 * - `"` で囲まれた部分は空白を含めて1つの引数にする (`"` 自体は取り除く)
 * - `""` は空の引数として扱う
 */
object CommandLineArgs {

    fun split(args: String?): List<String> {
        if (args.isNullOrBlank()) return emptyList()
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        var hasToken = false
        args.forEach { c ->
            when {
                c == '"' -> {
                    inQuotes = !inQuotes
                    hasToken = true
                }

                c.isWhitespace() && !inQuotes -> {
                    if (hasToken) {
                        result.add(current.toString())
                        current.clear()
                        hasToken = false
                    }
                }

                else -> {
                    current.append(c)
                    hasToken = true
                }
            }
        }
        if (hasToken) result.add(current.toString())
        return result
    }
}
