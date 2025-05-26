package com.snowdango.amya.platform

import coil3.PlatformContext
import coil3.request.ImageRequest

expect object ImageRequestProvider {

    fun getImageRequest(
        context: PlatformContext,
        model: Any?,
    ): ImageRequest
}
