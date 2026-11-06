package com.caricepat.shopee

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
}
