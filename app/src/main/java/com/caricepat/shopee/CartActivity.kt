package com.caricepat.shopee

import android.app.Activity
import android.os.Bundle

/**
 * Aktivitas tak terlihat yang dipakai tombol keranjang di widget.
 * Langsung membuka halaman keranjang Shopee lalu menutup diri.
 */
class CartActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ShopeeLauncher.openCart(this)
        finish()
        overridePendingTransition(0, 0)
    }
}
