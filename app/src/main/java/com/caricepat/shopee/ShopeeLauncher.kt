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
 * Shopee tidak menyediakan dokumentasi resmi untuk tautan pencarian. Karena itu
 * beberapa format dicoba berurutan, mulai dari rute internal React Native
 * (`apprl`) sampai tautan https biasa. Jika semuanya gagal, kata kunci disalin
 * ke clipboard lalu aplikasi Shopee dibuka.
 */
object ShopeeLauncher {

    const val SHOPEE_PACKAGE = "com.shopee.id"

    sealed class Result {
        data class Success(val uri: String) : Result()
        data class NotInstalled(val openedStore: Boolean) : Result()
        data object Failed : Result()
    }

    data class Method(
        val label: String,
        val uri: String,
        val forcePackage: String? = null,
        val isControl: Boolean = false
    )

    fun isShopeeInstalled(context: Context): Boolean = try {
        context.packageManager.getPackageInfo(SHOPEE_PACKAGE, 0)
        true
    } catch (e: Exception) {
        false
    }

    /**
     * Daftar kandidat tautan. Dipakai juga oleh layar uji tautan di MainActivity.
     */
    fun methods(query: String): List<Method> {
        val q = Uri.encode(query.trim())
        val web = "https://shopee.co.id/search?keyword=$q"

        fun apprl(label: String, route: String, params: String = ""): Method {
            val inner = route + if (params.isEmpty()) "" else "?$params"
            return Method(
                label,
                "shopeeid://main?apprl=" + Uri.encode(inner)
            )
        }

        return listOf(
            apprl("1 · apprl search-result + keyword", "/rn/@shopee-rn/search-result/SEARCH_RESULT", "keyword=$q"),
            apprl("2 · apprl search-result + tipe GLOBAL", "/rn/@shopee-rn/search-result/SEARCH_RESULT", "keyword=$q&searchPageType=GLOBAL"),
            apprl("3 · apprl search-result tanpa garis miring", "rn/@shopee-rn/search-result/SEARCH_RESULT", "keyword=$q"),
            apprl("4 · apprl PRE_SEARCH", "/rn/@shopee-rn/search/PRE_SEARCH", "keyword=$q"),
            apprl("5 · apprl SEARCH_RESULT", "/rn/SEARCH_RESULT", "keyword=$q"),
            Method("6 · shopeeid://search + tipe GLOBAL", "shopeeid://search?keyword=$q&searchPageType=GLOBAL"),
            Method("7 · shopeeid://search (lama)", "shopeeid://search?keyword=$q"),
            Method("8 · shopee://search", "shopee://search?keyword=$q"),
            Method("9 · https shopee.co.id (paksa app)", web, SHOPEE_PACKAGE),
            Method("10 · shopeeid://home (kontrol)", "shopeeid://home", isControl = true)
        )
    }

    fun launch(context: Context, query: String): Result {
        val appContext = context.applicationContext
        val installed = isShopeeInstalled(appContext)

        // Selalu salin kata kunci: berguna jika Shopee hanya membuka beranda.
        copyToClipboard(appContext, query)

        val allMethods = methods(query)
        val preferred = Prefs.preferredMethod(appContext)
        val ordered = if (preferred in allMethods.indices) {
            listOf(allMethods[preferred]) + allMethods.filterIndexed { index, _ -> index != preferred }
        } else {
            allMethods
        }

        for (method in ordered) {
            if (method.isControl) continue
            val intent = buildIntent(method) ?: continue

            val canOpen = if (method.forcePackage != null) {
                installed
            } else {
                resolvedToShopee(appContext, intent)
            }
            if (canOpen && start(appContext, intent)) {
                return Result.Success(method.uri)
            }
        }

        if (installed) {
            val openApp = appContext.packageManager.getLaunchIntentForPackage(SHOPEE_PACKAGE)
            if (openApp != null) {
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
        return Result.NotInstalled(openedStore)
    }

    /**
     * Menjalankan satu metode tertentu (dipakai layar diagnostik).
     */
    fun launchMethod(context: Context, method: Method): Boolean {
        val intent = buildIntent(method) ?: return false
        return start(context.applicationContext, intent)
    }

    private fun buildIntent(method: Method): Intent? = try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(method.uri))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (method.forcePackage != null) {
            intent.setPackage(method.forcePackage)
        }
        intent
    } catch (e: Exception) {
        null
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
