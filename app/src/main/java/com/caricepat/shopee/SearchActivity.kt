package com.caricepat.shopee

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

/**
 * Dialog pencarian ringan yang muncul langsung dari widget.
 * Fokus ke kolom input dan keyboard sudah terbuka saat layar tampil.
 */
class SearchActivity : Activity() {

    private lateinit var input: EditText
    private lateinit var recentRow: LinearLayout
    private lateinit var recentHeader: View
    private lateinit var recentScroll: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        setFinishOnTouchOutside(true)

        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        window.setDimAmount(0.45f)
        window.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE or
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        )

        input = findViewById(R.id.input)
        recentRow = findViewById(R.id.recentRow)
        recentHeader = findViewById(R.id.recentHeader)
        recentScroll = findViewById(R.id.recentScroll)

        findViewById<ImageButton>(R.id.btnSearch).setOnClickListener { submit() }
        findViewById<TextView>(R.id.btnClear).setOnClickListener {
            Prefs.clearRecent(this)
            renderRecent()
        }

        input.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                submit()
                true
            } else {
                false
            }
        }

        input.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN) {
                submit()
                true
            } else {
                false
            }
        }

        renderRecent()

        input.requestFocus()
        input.postDelayed({
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT)
        }, 180)
    }

    private fun renderRecent() {
        recentRow.removeAllViews()
        val recent = Prefs.recent(this)
        if (recent.isEmpty()) {
            recentHeader.visibility = View.GONE
            recentScroll.visibility = View.GONE
            return
        }

        recentHeader.visibility = View.VISIBLE
        recentScroll.visibility = View.VISIBLE

        for (query in recent) {
            val chip = TextView(this).apply {
                text = query
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
                setTextColor(getColor(R.color.text_primary))
                background = getDrawable(R.drawable.bg_chip)
                setPadding(dp(14), dp(8), dp(14), dp(8))
                maxLines = 1
                ellipsize = TextUtils.TruncateAt.END
                maxWidth = dp(220)
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { marginEnd = dp(8) }
                setOnClickListener {
                    input.setText(query)
                    input.setSelection(query.length)
                    submit()
                }
            }
            recentRow.addView(chip)
        }
    }

    private fun submit() {
        val query = input.text.toString().trim()
        if (query.isEmpty()) {
            Toast.makeText(this, R.string.type_first, Toast.LENGTH_SHORT).show()
            return
        }

        Prefs.addRecent(this, query)
        when (val result = ShopeeLauncher.launch(this, query)) {
            is ShopeeLauncher.Result.NotInstalled -> {
                val message = if (result.openedStore) {
                    getString(R.string.not_installed_store)
                } else {
                    getString(R.string.not_installed)
                }
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            }
            is ShopeeLauncher.Result.Success -> Unit
            ShopeeLauncher.Result.Failed -> {
                Toast.makeText(this, R.string.failed_open, Toast.LENGTH_LONG).show()
            }
        }

        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(input.windowToken, 0)
        finish()
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    companion object {
        fun intent(context: Context) = android.content.Intent(context, SearchActivity::class.java)
    }
}
