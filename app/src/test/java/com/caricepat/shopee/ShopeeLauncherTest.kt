package com.caricepat.shopee

import android.net.Uri
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ShopeeLauncherTest {

    @Test
    fun firstMethodUsesShopeeAppLink() {
        val methods = ShopeeLauncher.methods("sepatu")

        assertEquals(10, methods.size)
        assertEquals(ShopeeLauncher.SHOPEE_PACKAGE, methods[0].forcePackage)
        assertTrue(methods[0].uri.startsWith("https://shopee.co.id/search?keyword="))
    }

    @Test
    fun internalRoutesUseSearchResultPage() {
        val methods = ShopeeLauncher.methods("sepatu")
        val route = Uri.parse(methods[1].uri).getQueryParameter("apprl")

        assertTrue(route!!.startsWith("rn/@shopee-rn/search/SEARCH_RESULT?"))
        assertEquals("GLOBAL", Uri.parse(route).getQueryParameter("searchPageType"))
        assertEquals("sepatu", Uri.parse(route).getQueryParameter("keyword"))
    }

    @Test
    fun specialCharactersSurviveNestedEncoding() {
        val methods = ShopeeLauncher.methods("kaos & celana")
        val route = Uri.parse(methods[1].uri).getQueryParameter("apprl")

        assertEquals("kaos & celana", Uri.parse(route).getQueryParameter("keyword"))
    }

    @Test
    fun onlyLastMethodIsControl() {
        val methods = ShopeeLauncher.methods("sepatu")

        assertEquals(listOf(9), methods.indices.filter { methods[it].isControl })
    }

    @Test
    fun cartMethodsStartWithShopeeAppLink() {
        val methods = ShopeeLauncher.cartMethods()

        assertEquals(5, methods.size)
        assertEquals(ShopeeLauncher.SHOPEE_PACKAGE, methods[0].forcePackage)
        assertEquals("https://shopee.co.id/cart", methods[0].uri)
        assertTrue(methods.drop(1).all { it.uri.startsWith("shopee") })
    }
}
