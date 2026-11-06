package com.caricepat.shopee

import android.content.Context
import android.os.Build
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Menyimpan jejak crash terakhir ke penyimpanan internal aplikasi.
 * Dipakai agar pengguna bisa melihat (dan menyalin) penyebab crash
 * tanpa perlu menghubungkan perangkat ke komputer.
 */
object CrashLog {

    private const val FILE_NAME = "last_crash.txt"

    fun install(context: Context) {
        val app = context.applicationContext
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                write(app, throwable, thread.name)
            } catch (_: Throwable) {
                // jangan menutupi crash asli
            }
            previous?.uncaughtException(thread, throwable)
        }
    }

    fun write(context: Context, throwable: Throwable, threadName: String = Thread.currentThread().name) {
        val writer = StringWriter()
        throwable.printStackTrace(PrintWriter(writer))

        val version = try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        } catch (e: Exception) {
            "?"
        }

        val header = buildString {
            appendLine("Waktu   : " + SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date()))
            appendLine("Versi   : $version")
            appendLine("Android : ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
            appendLine("Perangkat: ${Build.MANUFACTURER} ${Build.MODEL}")
            appendLine("Thread  : $threadName")
            appendLine()
        }

        file(context).writeText(header + writer.toString())
    }

    fun read(context: Context): String? {
        val f = file(context)
        if (!f.exists()) return null
        val text = try {
            f.readText()
        } catch (e: Exception) {
            return null
        }
        if (text.isBlank()) return null

        // Jangan tampilkan crash dari versi lama: setelah pembaruan aplikasi,
        // laporan lama bisa membuat seolah-olah versi baru masih crash.
        val storedVersion = Regex("^Versi\\s*:\\s*(.+)$", RegexOption.MULTILINE)
            .find(text)?.groupValues?.get(1)?.trim()
        val currentVersion = try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        } catch (e: Exception) {
            null
        }
        if (storedVersion != null && currentVersion != null && storedVersion != currentVersion) {
            f.delete()
            return null
        }

        return text
    }

    fun clear(context: Context) {
        file(context).delete()
    }

    private fun file(context: Context) = File(context.filesDir, FILE_NAME)
}
