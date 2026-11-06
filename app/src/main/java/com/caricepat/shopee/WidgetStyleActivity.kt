package com.caricepat.shopee

import android.app.Activity
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast

/**
 * Pengaturan tampilan widget. Semua perubahan langsung disimpan
 * dan diterapkan ke widget yang sedang terpasang.
 */
class WidgetStyleActivity : Activity() {

    private lateinit var preview: View
    private lateinit var themeGroup: RadioGroup
    private lateinit var transparencyGroup: RadioGroup
    private lateinit var cornerGroup: RadioGroup
    private lateinit var textSizeGroup: RadioGroup
    private lateinit var ctaSwitch: Switch

    private val themeIds = intArrayOf(
        R.id.themeWhite, R.id.themeShopee, R.id.themeDark, R.id.themeMint
    )
    private val transparencyIds = intArrayOf(
        R.id.alpha100, R.id.alpha85, R.id.alpha70, R.id.alpha50
    )
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

        themeGroup = findViewById(R.id.themeGroup)
        transparencyGroup = findViewById(R.id.transparencyGroup)
        cornerGroup = findViewById(R.id.cornerGroup)
        textSizeGroup = findViewById(R.id.textSizeGroup)
        ctaSwitch = findViewById(R.id.ctaSwitch)

        loadCurrentValues()
        attachListeners()
        renderPreview()

        findViewById<View>(R.id.btnResetStyle).setOnClickListener { resetStyle() }
    }

    private fun loadCurrentValues() {
        themeGroup.check(themeIds[Prefs.widgetTheme(this)])
        transparencyGroup.check(transparencyIds[Prefs.widgetTransparency(this)])
        cornerGroup.check(cornerIds[Prefs.widgetCorner(this)])
        textSizeGroup.check(textSizeIds[Prefs.widgetTextSize(this)])
        ctaSwitch.isChecked = Prefs.showWidgetCta(this)
    }

    private fun attachListeners() {
        themeGroup.onSelect { index ->
            Prefs.setWidgetTheme(this, index)
            onStyleChanged()
        }
        transparencyGroup.onSelect { index ->
            Prefs.setWidgetTransparency(this, index)
            onStyleChanged()
        }
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

    private fun onStyleChanged() {
        renderPreview()
        SearchWidgetProvider.refreshAll(this)
    }

    private fun renderPreview() {
        val theme = WidgetStyle.theme(Prefs.widgetTheme(this))

        preview.findViewById<ImageView>(R.id.widgetBg).apply {
            setImageResource(WidgetStyle.cornerDrawable(Prefs.widgetCorner(this@WidgetStyleActivity)))
            setColorFilter(theme.bg)
            alpha = Prefs.widgetTransparency(this@WidgetStyleActivity) / 100f
        }

        preview.findViewById<ImageView>(R.id.widgetIcon).setColorFilter(theme.icon)

        preview.findViewById<TextView>(R.id.widgetTitle).apply {
            setTextColor(theme.title)
            setTextSize(
                TypedValue.COMPLEX_UNIT_SP,
                WidgetStyle.titleSize(Prefs.widgetTextSize(this@WidgetStyleActivity))
            )
        }
        preview.findViewById<TextView>(R.id.widgetSubtitle).setTextColor(theme.subtitle)

        preview.findViewById<ImageView>(R.id.widgetCtaBg).setColorFilter(theme.ctaBg)
        preview.findViewById<TextView>(R.id.widgetCta).setTextColor(theme.ctaText)
        preview.findViewById<View>(R.id.widgetCtaWrap).visibility =
            if (Prefs.showWidgetCta(this)) View.VISIBLE else View.GONE
    }

    private fun resetStyle() {
        Prefs.resetWidgetStyle(this)
        loadCurrentValues()
        renderPreview()
        SearchWidgetProvider.refreshAll(this)
        Toast.makeText(this, R.string.widget_style_reset_done, Toast.LENGTH_SHORT).show()
    }

    private fun RadioGroup.onSelect(action: (Int) -> Unit) {
        setOnCheckedChangeListener { group, checkedId ->
            val child = group.findViewById<RadioButton>(checkedId) ?: return@setOnCheckedChangeListener
            action(group.indexOfChild(child))
        }
    }
}
