package com.caricepat.shopee

import android.content.Context

/**
 * Penyimpanan riwayat pencarian yang ringan (tanpa AndroidX).
 */
object Prefs {

    private const val FILE = "cari_cepat"
    private const val KEY_RECENT = "recent"
    private const val KEY_HISTORY = "history_enabled"
    private const val KEY_METHOD = "preferred_method"
    private const val KEY_WIDGET_THEME = "widget_theme"
    private const val KEY_WIDGET_TRANSPARENCY = "widget_transparency"
    private const val KEY_WIDGET_CORNER = "widget_corner"
    private const val KEY_WIDGET_TEXT_SIZE = "widget_text_size"
    private const val KEY_WIDGET_CTA = "widget_cta"
    private const val KEY_WIDGET_CART = "widget_cart"
    private const val SEPARATOR = "\u0001"
    private const val MAX_RECENT = 8

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun isHistoryEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_HISTORY, true)

    fun setHistoryEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_HISTORY, enabled).apply()
        if (!enabled) clearRecent(context)
    }

    fun recent(context: Context): List<String> {
        val raw = prefs(context).getString(KEY_RECENT, "") ?: ""
        if (raw.isEmpty()) return emptyList()
        return raw.split(SEPARATOR).filter { it.isNotBlank() }.take(MAX_RECENT)
    }

    fun addRecent(context: Context, query: String) {
        if (!isHistoryEnabled(context)) return
        val clean = query.trim()
        if (clean.isEmpty()) return
        val updated = (listOf(clean) + recent(context).filterNot { it.equals(clean, true) })
            .take(MAX_RECENT)
        prefs(context).edit()
            .putString(KEY_RECENT, updated.joinToString(SEPARATOR))
            .apply()
    }

    fun clearRecent(context: Context) {
        prefs(context).edit().remove(KEY_RECENT).apply()
    }

    /** Indeks metode deep link yang dipilih pengguna, atau -1 jika belum ada. */
    fun preferredMethod(context: Context): Int =
        prefs(context).getInt(KEY_METHOD, -1)

    fun setPreferredMethod(context: Context, index: Int) {
        prefs(context).edit().putInt(KEY_METHOD, index).apply()
    }

    // --- Tampilan widget ---

    fun widgetTheme(context: Context): Int =
        WidgetStyle.clampTheme(prefs(context).getInt(KEY_WIDGET_THEME, 0))

    fun setWidgetTheme(context: Context, index: Int) {
        prefs(context).edit().putInt(KEY_WIDGET_THEME, WidgetStyle.clampTheme(index)).apply()
    }

    fun widgetTransparency(context: Context): Int =
        WidgetStyle.clampTransparency(
            prefs(context).getInt(KEY_WIDGET_TRANSPARENCY, WidgetStyle.DEFAULT_TRANSPARENCY)
        )

    fun setWidgetTransparency(context: Context, percent: Int) {
        prefs(context).edit()
            .putInt(KEY_WIDGET_TRANSPARENCY, WidgetStyle.clampTransparency(percent))
            .apply()
    }

    fun widgetCorner(context: Context): Int =
        WidgetStyle.clampCorner(prefs(context).getInt(KEY_WIDGET_CORNER, 1))

    fun setWidgetCorner(context: Context, index: Int) {
        prefs(context).edit().putInt(KEY_WIDGET_CORNER, WidgetStyle.clampCorner(index)).apply()
    }

    fun widgetTextSize(context: Context): Int =
        WidgetStyle.clampTitleSize(prefs(context).getInt(KEY_WIDGET_TEXT_SIZE, 1))

    fun setWidgetTextSize(context: Context, index: Int) {
        prefs(context).edit()
            .putInt(KEY_WIDGET_TEXT_SIZE, WidgetStyle.clampTitleSize(index))
            .apply()
    }

    fun showWidgetCta(context: Context): Boolean =
        prefs(context).getBoolean(KEY_WIDGET_CTA, true)

    fun setShowWidgetCta(context: Context, show: Boolean) {
        prefs(context).edit().putBoolean(KEY_WIDGET_CTA, show).apply()
    }

    fun showWidgetCart(context: Context): Boolean =
        prefs(context).getBoolean(KEY_WIDGET_CART, true)

    fun setShowWidgetCart(context: Context, show: Boolean) {
        prefs(context).edit().putBoolean(KEY_WIDGET_CART, show).apply()
    }

    fun resetWidgetStyle(context: Context) {
        prefs(context).edit()
            .remove(KEY_WIDGET_THEME)
            .remove(KEY_WIDGET_TRANSPARENCY)
            .remove(KEY_WIDGET_CORNER)
            .remove(KEY_WIDGET_TEXT_SIZE)
            .remove(KEY_WIDGET_CTA)
            .remove(KEY_WIDGET_CART)
            .apply()
    }
}
