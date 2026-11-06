package com.caricepat.shopee

import android.app.Activity
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast

/**
 * Pengaturan tampilan widget. Setiap tema ditampilkan sebagai mini widget
 * yang bisa diketuk. Semua perubahan langsung disimpan dan diterapkan.
 */
class WidgetStyleActivity : Activity() {

    private data class ThemeCard(
        val container: LinearLayout,
        val preview: View,
        val label: TextView
    )

    private lateinit var preview: View
    private lateinit var themePreviewList: LinearLayout
    private lateinit var transparencySeek: SeekBar
    private lateinit var transparencyValue: TextView
    private lateinit var cornerGroup: RadioGroup
    private lateinit var textSizeGroup: RadioGroup
    private lateinit var ctaSwitch: Switch

    private val themeCards = mutableListOf<ThemeCard>()

    private val cornerIds = intArrayOf(
        R.id.cornerSmall, R.id.cornerMedium, R.id.cornerLarge
    )
    private val textSizeIds = intArrayOf(
        R.id.sizeSmall, R.id.sizeNormal, R.id.sizeLarge
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_widget_style)

        preview = layoutInflater.inflate(R.layout.widget_search, findViewById(R.id.previewBox), false)
        findViewById<FrameLayout>(R.id.previewBox).addView(preview)

        themePreviewList = findViewById(R.id.themePreviewList)
        transparencySeek = findViewById(R.id.transparencySeek)
        transparencyValue = findViewById(R.id.transparencyValue)
        cornerGroup = findViewById(R.id.cornerGroup)
        textSizeGroup = findViewById(R.id.textSizeGroup)
        ctaSwitch = findViewById(R.id.ctaSwitch)

        buildThemeCards()
        loadCurrentValues()
        attachListeners()
        renderAll()

        findViewById<View>(R.id.btnResetStyle).setOnClickListener { resetStyle() }
    }

    /** Membuat 4 pilihan tema berupa mini widget yang bisa diketuk. */
    private fun buildThemeCards() {
        WidgetStyle.themes.forEachIndexed { index, theme ->
            val container = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setBackgroundResource(R.drawable.bg_theme_unselected)
                setPadding(dp(4), dp(4), dp(4), dp(4))
                isClickable = true
                isFocusable = true
                contentDescription = getString(theme.labelRes)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = dp(10) }
                setOnClickListener { selectTheme(index) }
            }

            val cardPreview = layoutInflater.inflate(R.layout.widget_search, container, false)
            container.addView(
                cardPreview,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dp(62)
                )
            )

            val label = TextView(this).apply {
                text = getString(theme.labelRes)
                gravity = Gravity.CENTER
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
                setPadding(0, dp(6), 0, dp(2))
                setTextColor(getColor(R.color.text_primary))
            }
            container.addView(label)

            themePreviewList.addView(container)
            themeCards += ThemeCard(container, cardPreview, label)
        }
    }

    private fun loadCurrentValues() {
        val transparency = Prefs.widgetTransparency(this)
        transparencySeek.progress = transparency
        transparencyValue.text = getString(R.string.transparency_value, transparency)

        cornerGroup.check(cornerIds[Prefs.widgetCorner(this)])
        textSizeGroup.check(textSizeIds[Prefs.widgetTextSize(this)])
        ctaSwitch.isChecked = Prefs.showWidgetCta(this)
    }

    private fun attachListeners() {
        transparencySeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                transparencyValue.text = getString(R.string.transparency_value, progress)
                Prefs.setWidgetTransparency(this@WidgetStyleActivity, progress)
                renderPreview()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                SearchWidgetProvider.refreshAll(this@WidgetStyleActivity)
            }
        })

        cornerGroup.onSelect { index ->
            Prefs.setWidgetCorner(this, index)
            onStyleChanged()
        }
        textSizeGroup.onSelect { index ->
            Prefs.setWidgetTextSize(this, index)
            onStyleChanged()
        }
        ctaSwitch.setOnCheckedChangeListener { _, checked ->
            Prefs.setShowWidgetCta(this, checked)
            onStyleChanged()
        }
    }

    private fun selectTheme(index: Int) {
        Prefs.setWidgetTheme(this, index)
        onStyleChanged()
    }

    private fun onStyleChanged() {
        renderAll()
        SearchWidgetProvider.refreshAll(this)
    }

    private fun renderAll() {
        renderPreview()
        renderThemeCards()
    }

    private fun renderPreview() {
        WidgetStyle.styleView(
            preview,
            Prefs.widgetTheme(this),
            Prefs.widgetTransparency(this),
            Prefs.widgetCorner(this),
            Prefs.widgetTextSize(this),
            Prefs.showWidgetCta(this)
        )
    }

    /** Mini widget tema selalu ditampilkan pekat agar warnanya jelas. */
    private fun renderThemeCards() {
        val selected = Prefs.widgetTheme(this)
        themeCards.forEachIndexed { index, card ->
            WidgetStyle.styleView(
                card.preview,
                index,
                0,
                Prefs.widgetCorner(this),
                Prefs.widgetTextSize(this),
                Prefs.showWidgetCta(this)
            )
            val isSelected = index == selected
            card.container.setBackgroundResource(
                if (isSelected) R.drawable.bg_theme_selected else R.drawable.bg_theme_unselected
            )
            card.label.setTextColor(
                getColor(if (isSelected) R.color.brand else R.color.text_primary)
            )
            card.label.setTypeface(null, if (isSelected) Typeface.BOLD else Typeface.NORMAL)
        }
    }

    private fun resetStyle() {
        Prefs.resetWidgetStyle(this)
        loadCurrentValues()
        renderAll()
        SearchWidgetProvider.refreshAll(this)
        Toast.makeText(this, R.string.widget_style_reset_done, Toast.LENGTH_SHORT).show()
    }

    private fun RadioGroup.onSelect(action: (Int) -> Unit) {
        setOnCheckedChangeListener { group, checkedId ->
            val child = group.findViewById<RadioButton>(checkedId) ?: return@setOnCheckedChangeListener
            action(group.indexOfChild(child))
        }
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()
}
