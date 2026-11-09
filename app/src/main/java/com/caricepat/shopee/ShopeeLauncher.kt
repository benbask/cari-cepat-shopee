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
     *
     * Urutan sengaja dimulai dari tautan universal `https://shopee.co.id/search`.
     * Tautan itu ditangani ProxyActivity Shopee (tanpa batasan path), jadi paling
     * stabil untuk membuka halaman hasil pencarian. Kandidat `apprl` di bawahnya
     * memakai rute internal yang ditemukan di kode Shopee:
     * `rn/@shopee-rn/search/SEARCH_RESULT` dengan `searchPageType=GLOBAL` dan
     * `searchParams` berisi kata kunci.
     */
    fun methods(query: String): List<Method> {
        val q = query.trim()
        val enc = Uri.encode(q)
        val web = "https://shopee.co.id/search?keyword=$enc"

        val escaped = q.replace("\\", "\\\\").replace("\"", "\\\"")
        val paramsJson = Uri.encode("{\"keyword\":\"$escaped\"}")

        fun apprl(label: String, route: String): Method =
            Method(label, "shopeeid://home?apprl=" + Uri.encode(route))

        return listOf(
            Method("1 · https shopee.co.id/search (paling andal)", web, SHOPEE_PACKAGE),
            apprl(
                "2 · apprl search/SEARCH_RESULT + keyword",
                "rn/@shopee-rn/search/SEARCH_RESULT?keyword=$enc&searchPageType=GLOBAL"
            ),
            apprl(
                "3 · apprl search/SEARCH_RESULT + searchParams",
                "rn/@shopee-rn/search/SEARCH_RESULT?searchPageType=GLOBAL" +
                    "&searchParams=$paramsJson&searchEntranceTracking=homepage_trending_search"
            ),
            apprl(
                "4 · apprl DRE searchModule + SEARCH_RESULT",
                "dre/searchModule?pageKey=SEARCH_RESULT&searchPageType=GLOBAL&searchParams=$paramsJson"
            ),
            apprl(
                "5 · apprl legacy search-result + searchParams",
                "rn/@shopee-rn/search-result/SEARCH_RESULT?searchPageType=GLOBAL&searchParams=$paramsJson"
            ),
            apprl(
                "6 · apprl legacy search-result + keyword",
                "rn/@shopee-rn/search-result/SEARCH_RESULT?keyword=$enc"
            ),
            apprl(
                "7 · apprl PRE_SEARCH + keyword",
                "rn/@shopee-rn/search/PRE_SEARCH?keyword=$enc"
            ),
            Method("8 · shopeeid://search + keyword", "shopeeid://search?keyword=$enc"),
            Method("9 · shopee://search + keyword", "shopee://search?keyword=$enc"),
            Method("10 · shopeeid://home (kontrol)", "shopeeid://home", isControl = true)
        )
    }

    /**
     * Kandidat tautan ke halaman keranjang Shopee. Sama seperti pencarian,
     * urutannya dimulai dari tautan https yang paling stabil, lalu rute internal.
     */
    fun cartMethods(): List<Method> {
        fun apprl(label: String, route: String): Method =
            Method(label, "shopeeid://home?apprl=" + Uri.encode(route))

        return listOf(
            Method("1 · https shopee.co.id/cart", "https://shopee.co.id/cart", SHOPEE_PACKAGE),
            apprl("2 · apprl cart", "rn/@shopee-rn/cart"),
            apprl("3 · apprl cart/CART", "rn/@shopee-rn/cart/CART"),
            Method("4 · shopeeid://cart", "shopeeid://cart"),
            Method("5 · shopee://cart", "shopee://cart")
        )
    }

    /** Membuka halaman keranjang Shopee tanpa membuka dialog pencarian. */
    fun openCart(context: Context): Result {
        val appContext = context.applicationContext
        val installed = isShopeeInstalled(appContext)

        for (method in cartMethods()) {
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
            if (openApp != null && start(appContext, openApp)) {
                Toast.makeText(
                    appContext,
                    R.string.cart_open_app_manually,
                    Toast.LENGTH_LONG
                ).show()
                return Result.Success("app://shopee")
            }
        }

        return Result.NotInstalled(openStore(appContext))
    }

    fun launch(context: Context, query: String): Result {        val appContext = context.applicationContext
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
