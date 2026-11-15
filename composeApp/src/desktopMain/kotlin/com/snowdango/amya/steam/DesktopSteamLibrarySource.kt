package com.snowdango.amya.steam

import com.snowdango.amya.platform.getPlatform

/**
 * 実機のSteamを読み取る [SteamLibrarySource]。
 * Steamが見つからなかった場合は次回の呼び出し時に再度探す (起動後にSteamをインストールした場合に対応)
 */
class DesktopSteamLibrarySource : SteamLibrarySource {

    @Volatile
    private var reader: SteamLibraryReader? = null

    private fun reader(): SteamLibraryReader? {
        reader?.let { return it }
        val installation = SteamLocator.locate(getPlatform().platformType) ?: return null
        return SteamLibraryReader(installation).also { reader = it }
    }

    override fun isAvailable(): Boolean = reader() != null

    override fun fingerprint(): String? = reader()?.fingerprint()

    override fun scan(): SteamScanResult? = reader()?.scan()
}
