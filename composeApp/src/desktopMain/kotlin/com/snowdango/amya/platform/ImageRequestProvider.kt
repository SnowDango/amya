package com.snowdango.amya.platform

import coil3.PlatformContext
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import io.ktor.client.*
import io.ktor.client.engine.java.*

actual object ImageRequestProvider {

    actual fun getImageRequest(
        context: PlatformContext,
        model: Any?,
    ): ImageRequest {
        return ImageRequest.Builder(context)
            .fetcherFactory(
                KtorNetworkFetcherFactory(
                    HttpClient(Java) {
                        engine {
                            protocolVersion = java.net.http.HttpClient.Version.HTTP_2
                        }
                    }
                )
            )
            .data(model)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .networkCachePolicy(CachePolicy.ENABLED)
            .build()
    }

}