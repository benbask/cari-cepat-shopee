package com.caricepat.shopee

import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SearchWidgetProviderTest {

    @Test
    fun appliesSavedThemeAndHidesCta() {
        val context = RuntimeEnvironment.getApplication()
        Prefs.setWidgetTheme(context, 2)
        Prefs.setWidgetTransparency(context, 3)
        Prefs.setWidgetCorner(context, 2)
        Prefs.setWidgetTextSize(context, 0)
        Prefs.setShowWidgetCta(context, false)

        val view = SearchWidgetProvider.buildViews(context)
            .apply(context, FrameLayout(context))

        val theme = WidgetStyle.theme(2)
        assertEquals(theme.title, view.findViewById<TextView>(R.id.widgetTitle).currentTextColor)
        assertEquals(theme.subtitle, view.findViewById<TextView>(R.id.widgetSubtitle).currentTextColor)
        assertEquals(View.GONE, view.findViewById<View>(R.id.widgetCta).visibility)
    }

    @Test
    fun showsCtaByDefault() {
        val context = RuntimeEnvironment.getApplication()

        val view = SearchWidgetProvider.buildViews(context)
            .apply(context, FrameLayout(context))

        assertEquals(View.VISIBLE, view.findViewById<View>(R.id.widgetCta).visibility)
        assertEquals(
            WidgetStyle.theme(0).title,
            view.findViewById<TextView>(R.id.widgetTitle).currentTextColor
        )
    }

    @Test
    fun cardIsShorterThanWidgetCell() {
        val context = RuntimeEnvironment.getApplication()
        val density = context.resources.displayMetrics.density
        val cellWidth = (340 * density).toInt()
        val cellHeight = (90 * density).toInt()

        val parent = FrameLayout(context)
        val view = SearchWidgetProvider.buildViews(context).apply(context, parent)
        parent.addView(view)
        parent.measure(
            View.MeasureSpec.makeMeasureSpec(cellWidth, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(cellHeight, View.MeasureSpec.EXACTLY)
        )
        parent.layout(0, 0, cellWidth, cellHeight)

        val background = view.findViewById<View>(R.id.widgetBg)
        assertEquals(Math.round(54 * density), background.height)
        assertTrue(background.top > 0)
        assertNotNull(view.findViewById<View>(R.id.widgetCta).background)
    }
}
