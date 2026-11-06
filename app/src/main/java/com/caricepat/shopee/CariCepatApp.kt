package com.caricepat.shopee

import android.app.Application

class CariCepatApp : Application() {
    override fun onCreate() {
        super.onCreate()
        CrashLog.install(this)
    }
}
