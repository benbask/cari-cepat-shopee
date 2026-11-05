package com.caricepat.shopee

import android.content.Context

/**
 * Penyimpanan riwayat pencarian yang ringan (tanpa AndroidX).
 */
object Prefs {

    private const val FILE = "cari_cepat"
    private const val KEY_RECENT = "recent"
    private const val KEY_HISTORY = "history_enabled"
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
}
