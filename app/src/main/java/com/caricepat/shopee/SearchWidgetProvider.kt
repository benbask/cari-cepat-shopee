package com.caricepat.shopee

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews

class SearchWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            appWidgetManager.updateAppWidget(appWidgetId, buildViews(context))
        }
    }

    companion object {

        fun buildViews(context: Context): RemoteViews {
            val views = RemoteViews(context.packageName, R.layout.widget_search)
            applyStyle(context, views)

            val intent = Intent(context, SearchActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, flags)
            views.setOnClickPendingIntent(R.id.widgetRoot, pendingIntent)
            return views
        }

        /** Menerapkan tema, transparansi, sudut, ukuran teks, dan tombol CTA. */
        private fun applyStyle(context: Context, views: RemoteViews) {
            val theme = WidgetStyle.theme(Prefs.widgetTheme(context))
            val bgAlpha = WidgetStyle.alphaFor(Prefs.widgetTransparency(context))

            views.setImageViewResource(
                R.id.widgetBg,
                WidgetStyle.cornerDrawable(Prefs.widgetCorner(context))
            )
            views.setInt(R.id.widgetBg, "setColorFilter", theme.bg)
            views.setInt(R.id.widgetBg, "setAlpha", bgAlpha)

            views.setInt(R.id.widgetIcon, "setColorFilter", theme.icon)
            views.setTextColor(R.id.widgetTitle, theme.title)
            views.setTextViewTextSize(
                R.id.widgetTitle,
                TypedValue.COMPLEX_UNIT_SP,
                WidgetStyle.titleSize(Prefs.widgetTextSize(context))
            )
            views.setTextColor(R.id.widgetSubtitle, theme.subtitle)

            views.setInt(R.id.widgetCtaBg, "setColorFilter", theme.ctaBg)
            views.setTextColor(R.id.widgetCta, theme.ctaText)
            views.setViewVisibility(
                R.id.widgetCtaWrap,
                if (Prefs.showWidgetCta(context)) View.VISIBLE else View.GONE
            )
        }

        fun refreshAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(
                ComponentName(context, SearchWidgetProvider::class.java)
            )
            for (id in ids) {
                manager.updateAppWidget(id, buildViews(context))
            }
        }
    }
}
