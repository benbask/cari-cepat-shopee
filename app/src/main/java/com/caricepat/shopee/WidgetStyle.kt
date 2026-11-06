package com.caricepat.shopee

import android.graphics.Color
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.TextView

/**
 * Kumpulan tema dan preset tampilan widget.
 * Semua nilai disimpan sebagai indeks agar ringan dan mudah dibaca ulang.
 */
object WidgetStyle {

    /** Transparansi awal saat aplikasi baru dipasang, dalam persen. */
    const val DEFAULT_TRANSPARENCY = 25

    data class Theme(
        val labelRes: Int,
        val bg: Int,
        val title: Int,
        val subtitle: Int,
        val icon: Int,
        val ctaBg: Int,
        val ctaBgRes: Int,
        val ctaText: Int
    )

    val themes: List<Theme> = listOf(
        Theme(
            labelRes = R.string.theme_white,
            bg = Color.parseColor("#FFFFFF"),
            title = Color.parseColor("#1C1C1E"),
            subtitle = Color.parseColor("#5F5F66"),
            icon = Color.parseColor("#EE4D2D"),
            ctaBg = Color.parseColor("#EE4D2D"),
            ctaBgRes = R.drawable.bg_widget_cta,
            ctaText = Color.parseColor("#FFFFFF")
        ),
        Theme(
            labelRes = R.string.theme_shopee,
            bg = Color.parseColor("#EE4D2D"),
            title = Color.parseColor("#FFFFFF"),
            subtitle = Color.parseColor("#FFE6DE"),
            icon = Color.parseColor("#FFFFFF"),
            ctaBg = Color.parseColor("#FFFFFF"),
            ctaBgRes = R.drawable.bg_widget_cta_white,
            ctaText = Color.parseColor("#EE4D2D")
        ),
        Theme(
            labelRes = R.string.theme_dark,
            bg = Color.parseColor("#1E1E22"),
            title = Color.parseColor("#FFFFFF"),
            subtitle = Color.parseColor("#B0B0B8"),
            icon = Color.parseColor("#FF7A5C"),
            ctaBg = Color.parseColor("#EE4D2D"),
            ctaBgRes = R.drawable.bg_widget_cta,
            ctaText = Color.parseColor("#FFFFFF")
        ),
        Theme(
            labelRes = R.string.theme_mint,
            bg = Color.parseColor("#E9F7F0"),
            title = Color.parseColor("#123B2C"),
            subtitle = Color.parseColor("#3F6B59"),
            icon = Color.parseColor("#1B8A4B"),
            ctaBg = Color.parseColor("#1B8A4B"),
            ctaBgRes = R.drawable.bg_widget_cta_mint,
            ctaText = Color.parseColor("#FFFFFF")
        )
    )

    private val cornerDrawables = intArrayOf(
        R.drawable.bg_widget_corner_small,
        R.drawable.bg_widget_corner_medium,
        R.drawable.bg_widget_corner_large
    )

    /** Ukuran teks judul widget, dalam sp. */
    private val titleSizes = floatArrayOf(13f, 15f, 17f)

    fun theme(index: Int): Theme = themes.getOrElse(index) { themes[0] }

    fun cornerDrawable(index: Int): Int =
        cornerDrawables.getOrElse(index) { cornerDrawables[1] }

    fun titleSize(index: Int): Float =
        titleSizes.getOrElse(index) { titleSizes[1] }

    fun clampTheme(index: Int): Int = index.coerceIn(0, themes.lastIndex)

    fun clampTransparency(percent: Int): Int = percent.coerceIn(0, 100)

    fun clampCorner(index: Int): Int = index.coerceIn(0, cornerDrawables.lastIndex)

    fun clampTitleSize(index: Int): Int = index.coerceIn(0, titleSizes.lastIndex)

    /** Alpha latar widget; transparansi 0% = pekat, 100% = tembus pandang. */
    fun alphaFor(transparency: Int): Int =
        (100 - clampTransparency(transparency)) * 255 / 100

    /** Menerapkan gaya ke tampilan widget biasa (dipakai untuk pratinjau). */
    fun styleView(
        root: View,
        themeIndex: Int,
        transparency: Int,
        cornerIndex: Int,
        textSizeIndex: Int,
        showCta: Boolean
    ) {
        val theme = theme(themeIndex)

        root.findViewById<ImageView>(R.id.widgetBg).apply {
            setImageResource(cornerDrawable(cornerIndex))
            setColorFilter(theme.bg)
            alpha = alphaFor(transparency) / 255f
        }
        root.findViewById<ImageView>(R.id.widgetIcon).setColorFilter(theme.icon)

        root.findViewById<TextView>(R.id.widgetTitle).apply {
            setTextColor(theme.title)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, titleSize(textSizeIndex))
        }
        root.findViewById<TextView>(R.id.widgetSubtitle).setTextColor(theme.subtitle)

        root.findViewById<TextView>(R.id.widgetCta).apply {
            setBackgroundResource(theme.ctaBgRes)
            setTextColor(theme.ctaText)
            visibility = if (showCta) View.VISIBLE else View.GONE
        }
    }
}
