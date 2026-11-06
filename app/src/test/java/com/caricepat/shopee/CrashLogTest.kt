package com.caricepat.shopee

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CrashLogTest {

    @Test
    fun crashFromCurrentVersionIsShown() {
        val context = RuntimeEnvironment.getApplication()

        CrashLog.write(context, RuntimeException("uji"))

        assertNotNull(CrashLog.read(context))
    }

    @Test
    fun crashFromOlderVersionIsIgnoredAndDeleted() {
        val context = RuntimeEnvironment.getApplication()
        val file = File(context.filesDir, "last_crash.txt")
        file.writeText("Waktu   : 2026-01-01 00:00:00\nVersi   : 0.9\n\njava.lang.RuntimeException: lama\n")

        assertNull(CrashLog.read(context))
        assertNull(CrashLog.read(context))
    }
}
