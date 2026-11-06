package com.caricepat.shopee

import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MainActivityTest {

    @Test
    fun mainActivityOpensWithoutCrash() {
        val controller = Robolectric.buildActivity(MainActivity::class.java).setup()
        assertNotNull(controller.get())
    }

    @Test
    fun searchActivityOpensWithoutCrash() {
        val controller = Robolectric.buildActivity(SearchActivity::class.java).setup()
        assertNotNull(controller.get())
    }

    @Test
    fun widgetCardIsVerticalAndShowsAddWidgetButton() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).setup().get()
        val button = activity.findViewById<Button>(R.id.btnPinWidget)

        assertEquals(View.VISIBLE, button.visibility)
        assertEquals(LinearLayout.VERTICAL, (button.parent as LinearLayout).orientation)
    }

    @Test
    fun widgetStyleActivityOpensWithoutCrash() {
        val controller = Robolectric.buildActivity(WidgetStyleActivity::class.java).setup()
        assertNotNull(controller.get())
    }

    @Test
    fun widgetCardShowsStyleButton() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).setup().get()
        val button = activity.findViewById<Button>(R.id.btnWidgetStyle)

        assertEquals(View.VISIBLE, button.visibility)
        assertEquals(
            activity.getString(R.string.widget_style_button),
            button.text.toString()
        )
    }
}
