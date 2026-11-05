package com.caricepat.shopee

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

/**
 * Membuka halaman pencarian di aplikasi Shopee Indonesia.
 *
 * Strategi berlapis:
 * 1. Skema deeplink internal Shopee (paling cepat, langsung ke halaman pencarian).
 * 2. App Link https://shopee.co.id/search?... yang dipaksa ke paket Shopee.
 * 3. Fallback ke aplikasi Shopee, browser, atau Play Store.
 */
object ShopeeLauncher {

    const val SHOPEE_PACKAGE = "com.shopee.id"

    sealed class Result {
        data class Success(val uri: String) : Result()
        data class NotInstalled(val openedStore: Boolean) : Result()
        data object Failed : Result()
    }

    fun isShopeeInstalled(context: Context): Boolean = try {
        context.packageManager.getPackageInfo(SHOPEE_PACKAGE, 0)
        true
    } catch (e: Exception) {
        false
    }

    private fun searchCandidates(query: String): List<Uri> {
        val encoded = Uri.encode(query.trim())
        return listOf(
            Uri.parse("shopeeid://search?keyword=$encoded"),
            Uri.parse("shopee://search?keyword=$encoded"),
            Uri.parse("https://shopee.co.id/search?keyword=$encoded")
        )
    }

    fun launch(context: Context, query: String): Result {
        val appContext = context.applicationContext
        val installed = isShopeeInstalled(appContext)

        // 1 & 2: pakai intent yang benar-benar diresolusi oleh paket Shopee.
        for (uri in searchCandidates(query)) {
            val intent = Intent(Intent.ACTION_VIEW, uri)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (resolvedToShopee(appContext, intent) && start(appContext, intent)) {
                return Result.Success(uri.toString())
            }
        }

        // 3: paksa App Link https ke paket Shopee (beberapa versi hanya mendaftarkan https).
        if (installed) {
            val forced = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://shopee.co.id/search?keyword=" + Uri.encode(query.trim()))
            )
                .setPackage(SHOPEE_PACKAGE)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (start(appContext, forced)) {
                return Result.Success(forced.data.toString())
            }

            // Shopee terpasang tetapi tidak menerima tautan pencarian: buka aplikasinya
            // dan salin kata kunci agar bisa ditempel manual.
            val openApp = appContext.packageManager.getLaunchIntentForPackage(SHOPEE_PACKAGE)
            if (openApp != null) {
                copyToClipboard(appContext, query)
                if (start(appContext, openApp)) {
                    Toast.makeText(
                        appContext,
                        R.string.copied_open_app,
                        Toast.LENGTH_LONG
                    ).show()
                    return Result.Success("app://shopee")
                }
            }
        }

        // Shopee belum terpasang: buka Play Store agar pengguna bisa memasangnya.
        val openedStore = openStore(appContext)
        copyToClipboard(appContext, query)
        return Result.NotInstalled(openedStore)
    }

    private fun resolvedToShopee(context: Context, intent: Intent): Boolean {
        val resolved = try {
            context.packageManager.resolveActivity(intent, 0)
        } catch (e: Exception) {
            null
        }
        return resolved?.activityInfo?.packageName == SHOPEE_PACKAGE
    }

    private fun start(context: Context, intent: Intent): Boolean = try {
        context.startActivity(intent)
        true
    } catch (e: Exception) {
        false
    }

    private fun openStore(context: Context): Boolean {
        val market = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("market://details?id=$SHOPEE_PACKAGE")
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (start(context, market)) return true

        val web = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://play.google.com/store/apps/details?id=$SHOPEE_PACKAGE")
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return start(context, web)
    }

    private fun copyToClipboard(context: Context, query: String) {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("Pencarian Shopee", query))
        } catch (e: Exception) {
            // abaikan
        }
    }
}
