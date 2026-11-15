package com.snowdango.amya.platform

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SingleInstanceTest {

    @get:Rule
    val temp = TemporaryFolder()

    @Test
    fun secondInstanceActivatesFirstAndFails() {
        val first = assertNotNull(SingleInstance.acquire(temp.root))
        val activated = CountDownLatch(1)
        first.onActivateRequest = { activated.countDown() }

        // 同じJVM内ではtryLockがOverlappingFileLockExceptionになるが、これも起動済みとして扱われる
        assertNull(SingleInstance.acquire(temp.root))
        assertTrue(activated.await(5, TimeUnit.SECONDS))

        first.release()
        val third = assertNotNull(SingleInstance.acquire(temp.root))
        third.release()
    }
}
