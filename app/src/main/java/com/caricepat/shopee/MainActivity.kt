package com.caricepat.shopee

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private lateinit var statusText: TextView
    private lateinit var historySwitch: Switch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.widgetStatus)
        historySwitch = findViewById(R.id.historySwitch)

        findViewById<Button>(R.id.btnPinWidget).setOnClickListener { pinWidget() }
        findViewById<Button>(R.id.btnTry).setOnClickListener {
            startActivity(SearchActivity.intent(this))
            overridePendingTransition(0, 0)
        }
        findViewById<Button>(R.id.btnClearHistory).setOnClickListener { clearHistory() }

        historySwitch.isChecked = Prefs.isHistoryEnabled(this)
        historySwitch.setOnCheckedChangeListener { _, checked ->
            Prefs.setHistoryEnabled(this, checked)
            if (!checked) {
                Toast.makeText(this, R.string.history_off_toast, Toast.LENGTH_SHORT).show()
            }
        }

        updateShopeeStatus()
    }

    override fun onResume() {
        super.onResume()
        updateWidgetStatus()
        updateShopeeStatus()
    }

    private fun pinWidget() {
        val manager = getSystemService(AppWidgetManager::class.java)
        val provider = ComponentName(this, SearchWidgetProvider::class.java)
        if (manager.isRequestPinAppWidgetSupported) {
            manager.requestPinAppWidget(provider, null, null)
        } else {
            Toast.makeText(this, R.string.pin_manual, Toast.LENGTH_LONG).show()
        }
    }

    private fun updateWidgetStatus() {
        val manager = AppWidgetManager.getInstance(this)
        val ids = manager.getAppWidgetIds(ComponentName(this, SearchWidgetProvider::class.java))
        statusText.text = if (ids.isNotEmpty()) {
            getString(R.string.widget_installed)
        } else {
            getString(R.string.widget_not_installed)
        }
    }

    private fun updateShopeeStatus() {
        val installed = ShopeeLauncher.isShopeeInstalled(this)
        val indicator = findViewById<TextView>(R.id.shopeeStatus)
        indicator.text = if (installed) {
            getString(R.string.shopee_found)
        } else {
            getString(R.string.shopee_missing)
        }
        indicator.setTextColor(
            getColor(if (installed) R.color.ok_green else R.color.warn_red)
        )
    }

    private fun clearHistory() {
        if (Prefs.recent(this).isEmpty()) {
            Toast.makeText(this, R.string.history_empty, Toast.LENGTH_SHORT).show()
            return
        }
        Prefs.clearRecent(this)
        Toast.makeText(this, R.string.history_cleared, Toast.LENGTH_SHORT).show()
    }
}
