package com.caricepat.shopee

import android.graphics.Color

/**
 * Kumpulan tema dan preset tampilan widget.
 * Semua nilai disimpan sebagai indeks agar ringan dan mudah dibaca ulang.
 */
object WidgetStyle {

    data class Theme(
        val labelRes: Int,
        val bg: Int,
        val title: Int,
        val subtitle: Int,
        val icon: Int,
        val ctaBg: Int,
        val ctaText: Int
    )

    val themes: List<Theme> = listOf(
        Theme(
            labelRes = R.string.theme_white,
            bg = Color.parseColor("#FFFFFF"),
            title = Color.parseColor("#1C1C1E"),
            subtitle = Color.parseColor("#8A8A8E"),
            icon = Color.parseColor("#EE4D2D"),
            ctaBg = Color.parseColor("#EE4D2D"),
            ctaText = Color.parseColor("#FFFFFF")
        ),
        Theme(
            labelRes = R.string.theme_shopee,
            bg = Color.parseColor("#EE4D2D"),
            title = Color.parseColor("#FFFFFF"),
            subtitle = Color.parseColor("#FFE0D6"),
            icon = Color.parseColor("#FFFFFF"),
            ctaBg = Color.parseColor("#FFFFFF"),
            ctaText = Color.parseColor("#EE4D2D")
        ),
        Theme(
            labelRes = R.string.theme_dark,
            bg = Color.parseColor("#1E1E22"),
            title = Color.parseColor("#FFFFFF"),
            subtitle = Color.parseColor("#A0A0A8"),
            icon = Color.parseColor("#FF7A5C"),
            ctaBg = Color.parseColor("#EE4D2D"),
            ctaText = Color.parseColor("#FFFFFF")
        ),
        Theme(
            labelRes = R.string.theme_mint,
            bg = Color.parseColor("#E9F7F0"),
            title = Color.parseColor("#123B2C"),
            subtitle = Color.parseColor("#4F7A68"),
            icon = Color.parseColor("#1B8A4B"),
            ctaBg = Color.parseColor("#1B8A4B"),
            ctaText = Color.parseColor("#FFFFFF")
        )
    )

    /** Pilihan transparansi latar widget, dalam persen. */
    val transparency = intArrayOf(100, 85, 70, 50)

    private val cornerDrawables = intArrayOf(
        R.drawable.bg_widget_corner_small,
        R.drawable.bg_widget_corner_medium,
        R.drawable.bg_widget_corner_large
    )

    /** Ukuran teks judul widget, dalam sp. */
    private val titleSizes = floatArrayOf(13f, 15f, 17f)

    fun theme(index: Int): Theme = themes.getOrElse(index) { themes[0] }

    fun transparencyPercent(index: Int): Int =
        transparency.getOrElse(index) { transparency[0] }

    fun cornerDrawable(index: Int): Int =
        cornerDrawables.getOrElse(index) { cornerDrawables[1] }

    fun titleSize(index: Int): Float =
        titleSizes.getOrElse(index) { titleSizes[1] }

    fun clampTheme(index: Int): Int = index.coerceIn(0, themes.lastIndex)

    fun clampTransparency(index: Int): Int = index.coerceIn(0, transparency.lastIndex)

    fun clampCorner(index: Int): Int = index.coerceIn(0, cornerDrawables.lastIndex)

    fun clampTitleSize(index: Int): Int = index.coerceIn(0, titleSizes.lastIndex)
}
