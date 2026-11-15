package com.snowdango.amya.platform

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.request.ErrorResult
import coil3.request.SuccessResult
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.Test
import kotlin.test.assertIs

class ImageRequestProviderTest {

    @get:Rule
    val temp = TemporaryFolder()

    /**
     * Steam同期ではlibrarycacheの画像を絶対パスのまま imageUrl に保存するため、
     * スペースや括弧を含む絶対パス ("C:\Program Files (x86)\Steam\..." など) を読み込めること
     */
    @Test
    fun loadsLocalAbsolutePath() = runBlocking<Unit> {
        val dir = File(temp.root, "Program Files (x86)/Steam/appcache/librarycache/613100/abc").apply { mkdirs() }
        val file = File(dir, "library_600x900.jpg")
        ImageIO.write(BufferedImage(6, 9, BufferedImage.TYPE_INT_RGB), "jpg", file)

        val context = PlatformContext.INSTANCE
        val result = ImageLoader(context).execute(ImageRequestProvider.getImageRequest(context, file.absolutePath))
        assertIs<SuccessResult>(result, (result as? ErrorResult)?.throwable?.toString())
    }
}
