package com.snowdango.amya.platform

import coil3.PlatformContext
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import io.ktor.client.*
import io.ktor.client.engine.java.*

actual object ImageRequestProvider {

    /**
     * 全ての画像読み込みで共有する。
     * リクエストごとに作るとHttpClient(スレッド)が溜まり続け、リクエストも毎回別物になり再読み込みが走る
     */
    private val fetcherFactory by lazy {
        KtorNetworkFetcherFactory(
            HttpClient(Java) {
                engine {
                    protocolVersion = java.net.http.HttpClient.Version.HTTP_2
                }
            }
        )
    }

    actual fun getImageRequest(
        context: PlatformContext,
        model: Any?,
    ): ImageRequest {
        return ImageRequest.Builder(context)
            .fetcherFactory(fetcherFactory)
            .data(model)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .networkCachePolicy(CachePolicy.ENABLED)
            .build()
    }
}
