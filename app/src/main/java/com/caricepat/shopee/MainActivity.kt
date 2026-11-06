package com.caricepat.shopee

import android.app.Activity
import android.app.AlertDialog
import android.appwidget.AppWidgetManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private lateinit var statusText: TextView
    private lateinit var historySwitch: Switch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Jika proses sebelumnya crash, tampilkan dulu penyebabnya agar bisa dilaporkan.
        val previous = CrashLog.read(this)
        if (previous != null) {
            showCrashScreen(previous)
            return
        }

        try {
            showMainScreen()
        } catch (t: Throwable) {
            CrashLog.write(this, t)
            showCrashScreen(CrashLog.read(this) ?: t.toString())
        }
    }

    private fun showMainScreen() {
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.widgetStatus)
        historySwitch = findViewById(R.id.historySwitch)

        findViewById<Button>(R.id.btnPinWidget).setOnClickListener { pinWidget() }
        findViewById<Button>(R.id.btnWidgetStyle).setOnClickListener {
            startActivity(Intent(this, WidgetStyleActivity::class.java))
        }
        findViewById<Button>(R.id.btnTry).setOnClickListener {
            startActivity(SearchActivity.intent(this))
            overridePendingTransition(0, 0)
        }
        findViewById<Button>(R.id.btnClearHistory).setOnClickListener { clearHistory() }
        findViewById<TextView>(R.id.linkDiagnostics).setOnClickListener { showLinkTester() }

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
        if (::statusText.isInitialized) {
            updateWidgetStatus()
        }
        if (::statusText.isInitialized) {
            updateShopeeStatus()
        }
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

    /**
     * Menampilkan daftar format tautan Shopee yang bisa dicoba satu per satu.
     * Berguna saat tautan bawaan hanya membuka beranda Shopee.
     */
    private fun showLinkTester() {
        val sample = Prefs.recent(this).firstOrNull() ?: "sepatu"
        val methods = ShopeeLauncher.methods(sample)
        val preferred = Prefs.preferredMethod(this)
        val labels = methods.mapIndexed { index, method ->
            (if (index == preferred) "★ " else "") + method.label
        }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle(R.string.link_tester_title)
            .setMessage(getString(R.string.link_tester_message, sample))
            .setItems(labels) { _, which ->
                val method = methods[which]
                val ok = ShopeeLauncher.launchMethod(this, method)
                val message = if (ok) {
                    getString(R.string.link_tester_opened, method.label)
                } else {
                    getString(R.string.link_tester_failed, method.label)
                }
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()

                if (ok) {
                    confirmPreferredMethod(which, method)
                }
            }
            .setNegativeButton(R.string.link_tester_forget) { _, _ ->
                Prefs.setPreferredMethod(this, -1)
                Toast.makeText(this, R.string.link_tester_forgotten, Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun confirmPreferredMethod(index: Int, method: ShopeeLauncher.Method) {
        AlertDialog.Builder(this)
            .setTitle(R.string.link_tester_confirm_title)
            .setMessage(getString(R.string.link_tester_confirm_message, method.label))
            .setPositiveButton(R.string.link_tester_yes) { _, _ ->
                Prefs.setPreferredMethod(this, index)
                Toast.makeText(this, R.string.link_tester_saved, Toast.LENGTH_LONG).show()
            }
            .setNegativeButton(R.string.link_tester_no, null)
            .show()
    }

    private fun showCrashScreen(detail: String) {
        val scroll = ScrollView(this).apply {
            setBackgroundColor(getColor(R.color.bg))
        }
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(32), dp(20), dp(28))
        }

        box.addView(TextView(this).apply {
            text = getString(R.string.crash_title)
            setTextColor(getColor(R.color.text_primary))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f)
            setTypeface(typeface, Typeface.BOLD)
        })
        box.addView(TextView(this).apply {
            text = getString(R.string.crash_explanation)
            setTextColor(getColor(R.color.text_muted))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            setPadding(0, dp(10), 0, dp(14))
        })
        box.addView(TextView(this).apply {
            text = detail
            setTextIsSelectable(true)
            setTextColor(getColor(R.color.text_primary))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
            typeface = Typeface.MONOSPACE
            setBackgroundResource(R.drawable.bg_card)
            setPadding(dp(14), dp(14), dp(14), dp(14))
        })
        box.addView(actionButton(R.string.crash_copy) { copyDetail(detail) })
        box.addView(actionButton(R.string.crash_retry) {
            CrashLog.clear(this)
            recreate()
        })
        box.addView(actionButton(R.string.close) { finish() })

        scroll.addView(box)
        setContentView(scroll)
    }

    private fun actionButton(labelRes: Int, action: () -> Unit): Button =
        Button(this).apply {
            text = getString(labelRes)
            isAllCaps = false
            setOnClickListener { action() }
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.topMargin = dp(14)
            layoutParams = params
        }

    private fun copyDetail(detail: String) {
        try {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("Crash Cari Cepat", detail))
            Toast.makeText(this, R.string.crash_copied, Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, R.string.crash_copy_failed, Toast.LENGTH_LONG).show()
        }
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()
}
