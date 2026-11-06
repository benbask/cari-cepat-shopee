package com.caricepat.shopee

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class WidgetStyleTest {

    @Test
    fun hasFourThemesWithLabelAndAccent() {
        assertEquals(4, WidgetStyle.themes.size)
        assertTrue(WidgetStyle.themes.all { it.labelRes != 0 })
        assertTrue(WidgetStyle.themes.all { it.bg != 0 && it.ctaBg != 0 && it.ctaText != 0 })
    }

    @Test
    fun invalidIndexesFallBackSafely() {
        assertEquals(WidgetStyle.themes[0], WidgetStyle.theme(-1))
        assertEquals(WidgetStyle.themes[0], WidgetStyle.theme(99))
        assertEquals(WidgetStyle.titleSize(1), WidgetStyle.titleSize(99))
    }

    @Test
    fun transparencyDefaultsTo25Percent() {
        assertEquals(25, WidgetStyle.DEFAULT_TRANSPARENCY)
    }

    @Test
    fun transparencyClampsBetween0And100() {
        assertEquals(0, WidgetStyle.clampTransparency(-1))
        assertEquals(0, WidgetStyle.clampTransparency(0))
        assertEquals(100, WidgetStyle.clampTransparency(100))
        assertEquals(100, WidgetStyle.clampTransparency(150))
    }

    @Test
    fun transparencyMapsToBackgroundAlpha() {
        assertEquals(255, WidgetStyle.alphaFor(0))
        assertEquals(191, WidgetStyle.alphaFor(25))
        assertEquals(0, WidgetStyle.alphaFor(100))
    }

    @Test
    fun clampsKeepValuesInRange() {
        assertEquals(0, WidgetStyle.clampTheme(-5))
        assertEquals(3, WidgetStyle.clampTheme(50))
        assertEquals(0, WidgetStyle.clampCorner(-1))
        assertEquals(2, WidgetStyle.clampCorner(9))
        assertEquals(0, WidgetStyle.clampTitleSize(-1))
        assertEquals(2, WidgetStyle.clampTitleSize(9))
    }
}
